import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Scanner;
import static java.lang.System.in;
import static java.lang.System.out;
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

public class ClienteRSA {
    private static final String HOST = "127.0.0.1";
    private static final int PUERTO = 8888;
    private static final String ALGORITMO_RSA = "RSA";
    private static final String ALGORITMO_SIMETRICO = "AES";
    private static final int TAMANO_CLAVE_RSA = 2048;
    private static final int TAMANO_CLAVE_SIMETRICO = 128;

    private Cliente socket;
    private PublicKey clavePublicaServidor;
    private SecretKey claveSimetrica;
    private PrivateKey clavePrivadaCliente;

    public void iniciar() {
        try (Socket socketCliente = new Socket(HOST, PUERTO);
             ObjectOutputStream escritorObj = new ObjectOutputStream(socketCliente.getOutputStream());
             ObjectInputStream lectorObj = new ObjectInputStream(socketCliente.getInputStream());
             Scanner scanner = new Scanner(in)) {

            // Generar un par de claves RSA para el cliente
            KeyPairGenerator generadorClave = KeyPairGenerator.getInstance(ALGORITMO_RSA);
            generadorClave.initialize(TAMANO_CLAVE_RSA);
            KeyPair parClaves = generadorClave.generateKeyPair();
            PublicKey clavePublicaCliente = parClaves.getPublic();
            PrivateKey clavePrivadaCliente = parClaves.getPrivate();

            // Enviar clave pública al servidor
            escritorObj.writeObject(clavePublicaCliente);
            escritorObj.flush();

            // Recibir clave pública del servidor
            clavePublicaServidor = (PublicKey) lectorObj.readObject();

            // Generar una clave de cifrado simétrico
            KeyGenerator generadorClaveSimetrica = KeyGenerator.getInstance(ALGORITMO_SIMETRICO);
            generadorClaveSimetrica.init(TAMANO_CLAVE_SIMETRICO);
            claveSimetrica = generadorClaveSimetrica.generateKey();

            Thread hiloEscucha = new Thread(() -> escucharMensajes(lectorObj));
            hiloEscucha.start(); // Iniciar hilo de escucha de mensajes

            while (true) {
                out.print("Ingresa 'S' para suscribirte o 'P' para publicar: ");
                String accion = scanner.nextLine();

                if (accion.equals("S")) {
                    out.print("Ingresa el tópico al que deseas suscribirte: ");
                    String topico = scanner.nextLine();
                    suscribir(escritorObj, topico); // Suscribir al tópico
                } else if (accion.equals("P")) {
                    out.print("Ingresa el tópico en el que deseas publicar: ");
                    String topico = scanner.nextLine();
                    out.print("Ingresa el mensaje que deseas publicar: ");
                    String mensaje = scanner.nextLine();
                    publicar(escritorObj, topico, mensaje); // Publicar un mensaje en el tópico
                } else if (accion.equals("Salir")) {
                    break;
                } else {
                    out.println("Acción no válida. Ingresa 'S' para suscribirte o 'P' para publicar.");
                }
            }

        } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void escucharMensajes(ObjectInputStream lectorObj) {
        try {
            Cipher cifrador = Cipher.getInstance(ALGORITMO_RSA);
            cifrador.init(Cipher.DECRYPT_MODE, clavePrivadaCliente);

            String mensajeCifrado;
            while ((mensajeCifrado = (String) lectorObj.readObject()) != null) {
                byte[] mensajeDescifradoBytes = cifrador.doFinal(Base64.getDecoder().decode(mensajeCifrado));
                String mensajeDescifrado = new String(mensajeDescifradoBytes);
                out.println("\nMensaje recibido: " + mensajeDescifrado);
            }
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    private void suscribir(ObjectOutputStream escritorObj, String topico) {
        // Cifrar y enviar solicitud de suscripción al servidor
        try {
            Cipher cifrador = Cipher.getInstance(ALGORITMO_SIMETRICO);
            cifrador.init(Cipher.ENCRYPT_MODE, claveSimetrica);
            byte[] mensajeCifrado = cifrador.doFinal(("SUSCRIBIR," + topico).getBytes());
            escritorObj.writeObject(Base64.getEncoder().encodeToString(mensajeCifrado));
            escritorObj.flush();
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    private void publicar(ObjectOutputStream escritorObj, String topico, String mensaje) {
        // Cifrar y enviar mensaje al servidor
        try {
            Cipher cifrador = Cipher.getInstance(ALGORITMO_SIMETRICO);
            cifrador.init(Cipher.ENCRYPT_MODE, claveSimetrica);
            byte[] mensajeCifrado = cifrador.doFinal(("PUBLICAR," + topico + "," + mensaje).getBytes());
            escritorObj.writeObject(Base64.getEncoder().encodeToString(mensajeCifrado));
            escritorObj.flush();
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        cliente.iniciar(); // Iniciar el cliente
    }
}
