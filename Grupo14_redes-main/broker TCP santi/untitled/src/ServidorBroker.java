import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorBroker {
    private static final int PUERTO = 8080;
    private Map<String, List<PrintWriter>> topicos = new HashMap<>();
    private final Object bloqueo = new Object();

    public void iniciar() {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor Broker escuchando en el puerto " + PUERTO);

            while (true) {
                Socket socketCliente = servidor.accept(); // Aceptar conexiones entrantes
                Thread hiloCliente = new Thread(() -> manipularCliente(socketCliente));
                hiloCliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void manipularCliente(Socket socketCliente) {
        try {
            PrintWriter escritor = new PrintWriter(socketCliente.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

            InetAddress direccionCliente = socketCliente.getInetAddress();
            System.out.println("Nuevo cliente conectado: " + direccionCliente.getHostAddress());

            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(",");
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
        } catch (IOException e) {
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

    private void publicar(String topico, String mensaje) {
        synchronized (bloqueo) {
            List<PrintWriter> suscriptores = topicos.get(topico);
            if (suscriptores != null) {
                for (PrintWriter suscriptor : suscriptores) {
                    suscriptor.println("MENSAJE," + topico + "," + mensaje);
                    suscriptor.flush();
                    System.out.println("Enviado mensaje al cliente suscrito al tópico " + topico);
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