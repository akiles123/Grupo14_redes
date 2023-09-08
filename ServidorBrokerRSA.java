import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ServidorBrokerRSA {
    private static final int PUERTO = 8888;
    private Map<String, List<PrintWriter>> topicos = new HashMap<>();
    private final Object bloqueo = new Object();
    private PrivateKey clavePrivadaServidor;
    private PublicKey clavePublicaCliente;
    private SecretKey claveSimetrica;

    public void iniciar() {
        try {
            KeyPairGenerator generadorClave = KeyPairGenerator.getInstance("RSA");
            generadorClave.initialize(2048);
            KeyPair parClaves = generadorClave.generateKeyPair();
            clavePrivadaServidor = parClaves.getPrivate();

            ServerSocket servidor = new ServerSocket(PUERTO);
            System.out.println("Servidor Broker escuchando en el puerto " + PUERTO);

            while (true) {
                Socket socketCliente = servidor.accept(); // Aceptar conexiones entrantes
                Thread hiloCliente = new Thread(() -> manipularCliente(socketCliente));
                hiloCliente.start();
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void manipularCliente(Socket socketCliente) {
        try {
            ObjectOutputStream escritorObj = new ObjectOutputStream(socketCliente.getOutputStream());
            ObjectInputStream lectorObj = new ObjectInputStream(socketCliente.getInputStream());

            InetAddress direccionCliente = socketCliente.getInetAddress();
            System.out.println("Nuevo cliente conectado: " + direccionCliente.getHostAddress());

            // Recibir clave pública del cliente
            clavePublicaCliente = (PublicKey) lectorObj.readObject();

            // Generar una clave de cifrado simétrico
            KeyGenerator generadorClaveSimetrica = KeyGenerator.getInstance("RSA");
            generadorClaveSimetrica.init(128);
            claveSimetrica = generadorClaveSimetrica.generateKey();

            // Enviar clave pública del servidor al cliente
            escritorObj.writeObject(clavePrivadaServidor);
            escritorObj.flush();

            String mensajeCifrado;
            Cipher cifrador = Cipher.getInstance("RSA");

            while ((mensajeCifrado = (String) lectorObj.readObject()) != null) {
                byte[] mensajeCifradoBytes = Base64.getDecoder().decode(mensajeCifrado);
                cifrador.init(Cipher.DECRYPT_MODE, claveSimetrica);
                byte[] mensajeDescifradoBytes = cifrador.doFinal(mensajeCifradoBytes);
                String mensajeDescifrado = new String(mensajeDescifradoBytes);

                String[] partes = mensajeDescifrado.split(",");
                String comando = partes[0];

                if (comando.equals("SUSCRIBIR")) {
                    String topico = partes[1];
                    suscribir(escritorObj, topico); // Suscribir al tópico
                } else if (comando.equals("PUBLICAR")) {
                    String topico = partes[1];
                    String mensaje = partes[2];
                    publicar(topico, mensaje); // Publicar un mensaje en el tópico
                } else if (comando.equals("ACK")) {
                    System.out.println("Confirmación de entrega del mensaje recibida");
                }
            }
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    private void suscribir(ObjectOutputStream escritorObj, String topico) {
        synchronized (bloqueo) {
            topicos.putIfAbsent(topico, new ArrayList<>());
            topicos.get(topico).add(new PrintWriter(escritorObj));
            System.out.println("Cliente suscrito al tópico " + topico);
        }
    }

    private void publicar(String topico, String mensaje) {
        synchronized (bloqueo) {
            List<PrintWriter> suscriptores = topicos.get(topico);
            if (suscriptores != null) {
                for (PrintWriter suscriptor : suscriptores) {
                    try {
                        Cipher cifrador = Cipher.getInstance("RSA");
                        cifrador.init(Cipher.ENCRYPT_MODE, claveSimetrica);
                        byte[] mensajeCifradoBytes = cifrador.doFinal(("MENSAJE," + topico + "," + mensaje).getBytes());
                        String mensajeCifrado = Base64.getEncoder().encodeToString(mensajeCifradoBytes);

                        suscriptor.println(mensajeCifrado);
                        suscriptor.flush();
                        System.out.println("Enviado mensaje al cliente suscrito al tópico " + topico);
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("No hay clientes suscritos al tópico " + topico);
            }
        }
    }

    public static void main(String[] args) {
        ServidorBroker broker = new ServidorBroker();
        broker.iniciar(); // Iniciar el servidor Broker
    }
}
