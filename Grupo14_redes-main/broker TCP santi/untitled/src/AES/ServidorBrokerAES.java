package AES;
//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
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServidorBrokerAES {
    private static final int PUERTO = 9000;
    private Map<String, List<PrintWriter>> topicos = new HashMap<>();
    public static Map<PrintWriter, PublicKey> clavesRSAClientes = new HashMap<>();
    public static Map<PrintWriter, SecretKey> clavesAESClientes = new HashMap<>();
    private final Object bloqueo = new Object();
    public static PrivateKey privateKey = null;
    public static PublicKey publicKey = null;

    // Método principal para iniciar
    public void iniciar() {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor Broker escuchando en el puerto " + PUERTO);

            // Genera un par de claves RSA para el servidor
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

    // Método para manipular el intercambio con el cliente
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
                    // Al recibir la clave pública del cliente, la almacena en el mapa de claves RSA de clientes
                    clavesRSAClientes.put(escritor, Metodos.base64ClavePublica(linea));

                    // Envía la clave pública del servidor al cliente
                    escritor.println(Metodos.clavePublicaBase64(publicKey));

                    // Genera y envía la clave AES al cliente cifrada con su clave pública RSA
                    clavesAESClientes.put(escritor, Metodos.generarClaveAES());
                    escritor.println(Metodos.cifrarClaveAES(clavesAESClientes.get(escritor), privateKey, clavesRSAClientes.get(escritor)));

                    primera = false;
                    continue; // Finaliza la lectura de mensajes, en el caso de que la primera linea sea false, el while se cierra
                }
                String msg = Metodos.descifrarMensaje(linea, clavesAESClientes.get(escritor), clavesRSAClientes.get(escritor));
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

    // Método para suscribir a un tópico
    private void suscribir(PrintWriter escritor, String topico) {
        synchronized (bloqueo) {
            topicos.putIfAbsent(topico, new ArrayList<>());
            topicos.get(topico).add(escritor);
            System.out.println("Cliente suscrito al tópico " + topico);
        }
    }

    // Método para publicar un mensaje en un tópico
    private void publicar(String topico, String mensaje) throws Exception {
        synchronized (bloqueo) {
            List<PrintWriter> suscriptores = topicos.get(topico);
            if (suscriptores != null) {
                for (PrintWriter suscriptor : suscriptores) {
                    // Envia el mensaje cifrado utilizando AES y RSA al cliente suscrito
                    suscriptor.println(Metodos.cifrarMensaje(("MENSAJE," + topico + "," + mensaje), privateKey, clavesAESClientes.get(suscriptor)));
                    suscriptor.flush();
                    System.out.println("Enviado mensaje al cliente suscrito al tópico " + topico);
                }
            } else {
                System.out.println("No hay clientes suscritos al tópico " + topico);
            }
        }
    }

    // Método principal para ejecutar el servidor Broker
    public static void main(String[] args) throws Exception {
        ServidorBrokerAES broker = new ServidorBrokerAES();
        broker.iniciar(); // Iniciar el servidor Broker
    }
}