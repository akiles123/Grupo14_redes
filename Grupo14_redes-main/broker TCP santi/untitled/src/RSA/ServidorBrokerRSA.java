package RSA;

import javax.crypto.Cipher;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public class ServidorBrokerRSA {
    private static final int PUERTO = 9000; // Puerto en el que el servidor escuchará las conexiones entrantes
    private Map<String, List<PrintWriter>> topicos = new HashMap<>(); // Map que almacena los tópicos y sus suscriptores
    public static Map<PrintWriter, PublicKey> clavesClientes = new HashMap<>(); // Map que almacena las claves públicas de los clientes
    private final Object bloqueo = new Object(); // Objeto utilizado para sincronización de hilos
    public static PrivateKey privateKey = null; // Clave privada del servidor
    public static PublicKey publicKey = null; // Clave pública del servidor

    public void iniciar() {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor Broker escuchando en el puerto " + PUERTO);

            // Generar un par de claves RSA para el servidor
            KeyPair claves = Metodos.generarClaveRSA();
            privateKey = claves.getPrivate();
            publicKey = claves.getPublic();

            while (true) {
                Socket socketCliente = servidor.accept(); // Aceptar conexiones entrantes
                Thread hiloCliente = new Thread(() -> manipularCliente(socketCliente));
                hiloCliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void manipularCliente(Socket socketCliente) {
        try {
            PrintWriter escritor = new PrintWriter(socketCliente.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

            InetAddress direccionCliente = socketCliente.getInetAddress();
            System.out.println("Nuevo cliente conectado: " + direccionCliente.getHostAddress());

            String linea;
            boolean primera = true;
            while ((linea = lector.readLine()) != null) {
                if (primera) {
                    // Cuando el cliente se conecta por primera vez, se intercambia información de clave pública
                    clavesClientes.put(escritor, Metodos.base64ClavePublica(linea));
                    escritor.println(Metodos.clavePublicaBase64(publicKey)); // Envía la clave pública del servidor al cliente
                    primera = false;
                    continue;
                }
                String msg = Metodos.descifrarMensaje(linea, privateKey, clavesClientes.get(escritor));
                if (msg.equals("") || msg == null) continue;
                String[] partes = msg.split(",");
                String comando = partes[0];

                if (comando.equals("SUSCRIBIR")) {
                    String topico = partes[1];
                    suscribir(escritor, topico); // Suscribir al tópico
                } else if (comando.equals("PUBLICAR")) {
                    String topico = partes[1];
                    String mensaje = partes[2];
                    publicar(topico, mensaje); // Publicar un mensaje en el tópico
                } else if (comando.equals("ACK")) {
                    System.out.println("Confirmación de entrega del mensaje recibida");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void suscribir(PrintWriter escritor, String topico) {
        synchronized (bloqueo) {
            topicos.putIfAbsent(topico, new ArrayList<>());
            topicos.get(topico).add(escritor);
            System.out.println("Cliente suscrito al tópico " + topico);
        }
    }

    private void publicar(String topico, String mensaje) throws Exception {
        synchronized (bloqueo) {
            List<PrintWriter> suscriptores = topicos.get(topico);
            if (suscriptores != null) {
                for (PrintWriter suscriptor : suscriptores) {
                    suscriptor.println(Metodos.cifrarMensaje(("MENSAJE," + topico + "," + mensaje), privateKey, clavesClientes.get(suscriptor)));
                    suscriptor.flush();
                    System.out.println("Enviado mensaje al cliente suscrito al tópico " + topico);
                }
            } else {
                System.out.println("No hay clientes suscritos al tópico " + topico);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ServidorBrokerRSA broker = new ServidorBrokerRSA();
        broker.iniciar(); // Iniciar el servidor Broker
    }
}
