import java.io.*;
import java.net.*;
import java.util.*;

public class BrokerServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clientes;
    private Map<String, List<ClientHandler>> Subscripcion;

    public BrokerServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientes = new ArrayList<>();
            Subscripcion = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Broker Server iniciado en el puerto " + serverSocket.getLocalPort());

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientes.add(clientHandler);
                clientHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribir(String topic, ClientHandler clientHandler) {
        List<ClientHandler> sub = Subscripcion.getOrDefault(topic, new ArrayList<>());
        sub.add(clientHandler);
        Subscripcion.put(topic, sub);
    }

    public void publish(String topic, String mensaje) {
        List<ClientHandler> Subscrito = Subscripcion.getOrDefault(topic, new ArrayList<>());

        for (ClientHandler subscriber : Subscrito) {
            if (subscriber.isConnected()) {
                subscriber.sendMessage(mensaje);
            }
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Procesar el mensaje recibido desde el cliente
                    // Aquí se puede implementar la lógica de suscripción, publicación y entrega de ACK

                    // Ejemplo: Suscripción a un tópico
                    if (inputLine.startsWith("Suscripto")) {
                        String topic = inputLine.substring(10);
                        subscribir(topic, this);
                    }
                    //Publicar en un tópico
                    else if (inputLine.startsWith("Publicado")) {
                        String[] parts = inputLine.split(" ", 3);
                        String topic = parts[1];
                        String message = parts[2];
                        publish(topic, message);
                    }
                    //Envío de ACK desde el cliente
                    else if (inputLine.equals("ACK")) {
                        // Realizar acciones correspondientes al recibir el ACK
                    }
                }

                in.close();
                out.close();
                clientSocket.close();
                clientes.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isConnected() {
            return !clientSocket.isClosed();
        }

        public void sendMessage(String mensaje1) {
            out.println(mensaje1);
        }
    }

    public static void main(String[] args) {
        int port = 8080; // Puerto en el que el servidor escuchará las conexiones

        BrokerServer brokerServer = new BrokerServer(port);
        brokerServer.start();
    }
}
