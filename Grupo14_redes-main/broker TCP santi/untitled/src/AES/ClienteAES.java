package AES;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;


public class ClienteAES {
    private static final String HOST = "127.0.0.1"; // Dirección del servidor
    private static final int PUERTO = 9000; // Puerto del servidor
    private static PrivateKey privateKey = null; // Clave privada del cliente
    private static PublicKey publicKey = null; // Clave pública del cliente
    private static PublicKey publicKeyServer = null; // Clave pública del servidor
    private static SecretKey secretkey = null; // Clave secreta compartida para cifrado simétrico

    // Método principal para iniciar el cliente
    public void iniciar() {
        try (Socket socketClienteRSA = new Socket(HOST, PUERTO);
             PrintWriter escritor = new PrintWriter(socketClienteRSA.getOutputStream(), true);
             BufferedReader lector = new BufferedReader(new InputStreamReader(socketClienteRSA.getInputStream()));
             Scanner scanner = new Scanner(in)) {

            // Genera un par de claves RSA para el cliente
            KeyPair claves = Metodos.generarClaveRSA();
            privateKey = claves.getPrivate();
            publicKey = claves.getPublic();

            // Envia la clave pública del cliente al servidor
            escritor.println(Metodos.clavePublicaBase64(publicKey));

            // Inicia un hilo para escuchar mensajes del servidor
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
                } else if (accion.equals("Salir")) {
                    break;
                } else {
                    out.println("Acción no válida. Ingresa 'S' para suscribirte o 'P' para publicar.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Método para escuchar los mensajes del servidor
    private void escucharMensajes(BufferedReader lector) {
        try {
            String linea;
            boolean primera = true;
            boolean segunda = true;
            while ((linea = lector.readLine()) != null) {
                if (primera) {
                    // Lee la clave pública del servidor y almacenarla
                    publicKeyServer = Metodos.base64ClavePublica(linea);
                    primera = false;
                    continue;
                }
                else if (segunda) {
                    // Descifra la clave AES compartida con la clave privada del cliente y la clave pública del servidor
                    secretkey = Metodos.descifrarClaveAES(linea, privateKey, publicKeyServer);
                    segunda = false;
                    continue;
                }
                // Descifra y procesa mensajes cifrados con la clave AES compartida
                String msg = Metodos.descifrarMensaje(linea, secretkey, publicKeyServer);
                if (msg.equals("") || msg == null) continue;
                String[] partes = msg.split(",");
                String comando = partes[0];
                if (comando.equals("MENSAJE")) {
                    String topico = partes[1];
                    String mensaje = partes[2];
                    out.println("\n Mensaje recibido en el tópico '" + topico + "': " + mensaje);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Método para suscribirse a un tópico
    private void suscribir(PrintWriter escritor, String topico) throws Exception {
        escritor.println(Metodos.cifrarMensaje(("SUSCRIBIR," + topico), privateKey, secretkey)); // Envia solicitud de suscripción al servidor
        escritor.flush();
    }

    // Método para publicar un mensaje en un tópico
    private void publicar(PrintWriter escritor, String topico, String mensaje) throws Exception {
        escritor.println(Metodos.cifrarMensaje(("PUBLICAR," + topico + "," + mensaje), privateKey, secretkey)); // Envia mensaje al servidor
        escritor.flush();
    }

    // Método para cerrar la entrada y salida estándar
    public static void close() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método principal para ejecutar el cliente
    public static void main(String[] args)  {
        ClienteAES cliente = new ClienteAES();
        cliente.iniciar(); // Iniciar el cliente
        cliente.close();
    }
}