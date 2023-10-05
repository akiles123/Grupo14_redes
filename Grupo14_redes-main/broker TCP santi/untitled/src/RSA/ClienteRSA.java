package RSA;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;
import static java.lang.System.in;
import static java.lang.System.out;

public class ClienteRSA {
    private static final String HOST = "127.0.0.1"; // Dirección IP del servidor
    private static final int PUERTO = 9000; // Puerto del servidor
    private static PrivateKey privateKey = null; // Clave privada del cliente
    private static PublicKey publicKey = null; // Clave pública del cliente
    private static PublicKey publicKeyServer = null; // Clave pública del servidor

    // Método principal para iniciar el cliente RSA
    public void iniciar() {
        try (Socket socketClienteRSA = new Socket(HOST, PUERTO);
             PrintWriter escritor = new PrintWriter(socketClienteRSA.getOutputStream(), true);
             BufferedReader lector = new BufferedReader(new InputStreamReader(socketClienteRSA.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            // Genera un par de claves RSA para el cliente
            KeyPair claves = Metodos.generarClaveRSA();
            privateKey = claves.getPrivate();
            publicKey = claves.getPublic();

            escritor.println(Metodos.clavePublicaBase64(publicKey)); // Enviar clave pública al servidor

            // Inicia un hilo para escuchar mensajes del servidor
            Thread hiloEscucha = new Thread(() -> escucharMensajes(lector));
            hiloEscucha.start();

            while (true) {
                System.out.print("Ingresa 'S' para suscribirte o 'P' para publicar: ");
                String accion = scanner.nextLine();

                if (accion.equals("S")) {
                    System.out.print("Ingresa el tópico al que deseas suscribirte: ");
                    String topico = scanner.nextLine();
                    suscribir(escritor, topico); // Suscribir al tópico
                } else if (accion.equals("P")) {
                    System.out.print("Ingresa el tópico en el que deseas publicar: ");
                    String topico = scanner.nextLine();
                    System.out.print("Ingresa el mensaje que deseas publicar: ");
                    String mensaje = scanner.nextLine();
                    publicar(escritor, topico, mensaje); // Publicar un mensaje en el tópico
                } else if (accion.equals("Salir")) {
                    break;
                } else {
                    System.out.println("Acción no válida. Ingresa 'S' para suscribirte o 'P' para publicar.");
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
            while ((linea = lector.readLine()) != null) {
                if (primera){
                    publicKeyServer = Metodos.base64ClavePublica(linea); // Recibir clave pública del servidor
                    primera = false;
                    continue;
                }
                String msg = Metodos.descifrarMensaje(linea, privateKey, publicKeyServer);
                if (msg.equals("") || msg == null) continue;
                String[] partes = msg.split(",");
                String comando = partes[0];
                if (comando.equals("MENSAJE")) {
                    String topico = partes[1];
                    String mensaje = partes[2];
                    System.out.println("\nMensaje recibido en el tópico '" + topico + "': " + mensaje);
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
        escritor.println(Metodos.cifrarMensaje(("SUSCRIBIR," + topico), privateKey, publicKeyServer)); // Enviar solicitud de suscripción al servidor
        escritor.flush();
    }

    // Método para publicar un mensaje en un tópico
    private void publicar(PrintWriter escritor, String topico, String mensaje) throws Exception {
        escritor.println(Metodos.cifrarMensaje(("PUBLICAR," + topico + "," + mensaje), privateKey, publicKeyServer)); // Enviar mensaje al servidor
        escritor.flush();
    }

    // Método para cerrar recursos
    public static void close() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método principal para ejecutar el cliente RSA
    public static void main(String[] args)  {
        ClienteRSA cliente = new ClienteRSA();
        cliente.iniciar(); // Iniciar el cliente
        cliente.close(); // Cerrar recursos al finalizar
    }
}
