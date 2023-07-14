import java.io.*;
import java.net.*;

public class BrokerCliente {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public BrokerCliente(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        out.println("SUBSCRIBE " + topic);
    }

    public void publish(String topic, String message) {
        out.println("PUBLISH " + topic + " " + message);
    }

    public void sendAck() {
        out.println("ACK");
    }

    public void startReceivingMessages() {
        try {
            String receivedMessage;
            while ((receivedMessage = in.readLine()) != null) {
                // Procesar el mensaje recibido desde el servidor
                System.out.println("Mensaje recibido: " + receivedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String serverDireccion = "localhost"; // Dirección IP o nombre de dominio del servidor
        int serverPort = 8080; // Puerto del servidor

        BrokerCliente client = new BrokerCliente(serverDireccion, serverPort);

        // Suscripción a un tópico
        client.subscribe("Deportes");

        // Publicación de un mensaje en un tópico
        client.publish("Deportes", "¡Partido emocionante esta noche!");

        // Envío de ACK al servidor
        client.sendAck();

        // Iniciar la recepción de mensajes del servidor
        client.startReceivingMessages();

        // Cerrar la conexión del cliente
        client.close();
    }
}
