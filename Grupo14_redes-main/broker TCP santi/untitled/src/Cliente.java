import java.io.*;
import java.net.*;
import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;

public class Cliente {
    private static final String HOST = "127.0.0.1";
    private static final int PUERTO = 8080;
    private Cliente socket;

    public void iniciar() {
        try (Socket socketCliente = new Socket(HOST, PUERTO);
             PrintWriter escritor = new PrintWriter(socketCliente.getOutputStream(), true);
             BufferedReader lector = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
             Scanner scanner = new Scanner(in)) {

            Thread hiloEscucha = new Thread(() -> escucharMensajes(lector));
            hiloEscucha.start(); // Iniciar hilo de escucha de mensajes

            while (true) {
                out.print("Ingresa 'S' para suscribirte o 'P' para publicar: ");
                String accion = scanner.nextLine();

                if (accion.equals("S")) {
                    out.print("Ingresa el tópico al que deseas suscribirte: ");
                    String topico = scanner.nextLine();
                    suscribir(escritor, topico); // Suscribir al tópico
                } else if (accion.equals("P")) {
                    out.print("Ingresa el tópico en el que deseas publicar: ");
                    String topico = scanner.nextLine();
                    out.print("Ingresa el mensaje que deseas publicar: ");
                    String mensaje = scanner.nextLine();
                    publicar(escritor, topico, mensaje); // Publicar un mensaje en el tópico
                }else if(accion.equals("Salir")){
                    break;
                }
                else {
                    out.println("Acción no válida. Ingresa 'S' para suscribirte o 'P' para publicar.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void escucharMensajes(BufferedReader lector) {
        try {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(",");
                String comando = partes[0];
                if (comando.equals("MENSAJE")) {
                    String topico = partes[1];
                    String mensaje = partes[2];
                    out.println("\n Mensaje recibido en el tópico '" + topico + "': " + mensaje);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void suscribir(PrintWriter escritor, String topico) {
        escritor.println("SUSCRIBIR," + topico); // Enviar solicitud de suscripción al servidor
        escritor.flush();
    }

    private void publicar(PrintWriter escritor, String topico, String mensaje) {
        escritor.println("PUBLICAR," + topico + "," + mensaje); // Enviar mensaje al servidor
        escritor.flush();
    }
    public static void close() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        cliente.iniciar(); // Iniciar el cliente
        cliente.close();
    }
}