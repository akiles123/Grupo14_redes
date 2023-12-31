package RSA;
 import javax.crypto.Cipher;
 import java.nio.charset.StandardCharsets;
 import java.security.*;
 import java.security.spec.X509EncodedKeySpec;
 import java.util.Base64;

public class Metodos {
    private static final int KEY_SIZE = 2048; // Tamaño de la clave RSA en bits

    public final static String delimitador = "moristeenmadridbosterojajajajajajajajajajajajajajajajajajajajajajajajajajajajajajajajajajajaja";
    public final static String delimitadorCodificado = Base64.getEncoder().encodeToString(delimitador.getBytes());
    public static KeyPair generarClaveRSA() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    public static String cifrarPublicaRSA(PublicKey clavePublica, String mensaje) throws Exception {
        Cipher cifrador = Cipher.getInstance("RSA");
        cifrador.init(Cipher.ENCRYPT_MODE, clavePublica);
        return Base64.getEncoder().encodeToString(cifrador.doFinal(mensaje.getBytes(StandardCharsets.UTF_8)));
    }

    public static String descifrarPrivadaRSA(PrivateKey clavePrivada, String mensajeCifrado) throws Exception {
        Cipher cifrador = Cipher.getInstance("RSA");
        cifrador.init(Cipher.DECRYPT_MODE, clavePrivada);
        byte[] mensajeDescifrado = cifrador.doFinal(Base64.getDecoder().decode(mensajeCifrado.getBytes(StandardCharsets.UTF_8)));
        return new String(mensajeDescifrado);
    }
    public static String cifrarPrivadaRSA(PrivateKey clavePrivada, String mensaje) throws Exception {
        Cipher cifrador = Cipher.getInstance("RSA");
        cifrador.init(Cipher.ENCRYPT_MODE, clavePrivada);
        return Base64.getEncoder().encodeToString(cifrador.doFinal(mensaje.getBytes(StandardCharsets.UTF_8)));
    }

    public static String descifrarPublicaRSA(PublicKey clavePublica, String mensajeCifrado) throws Exception {
        Cipher cifrador = Cipher.getInstance("RSA");
        cifrador.init(Cipher.DECRYPT_MODE, clavePublica);
        byte[] mensajeDescifrado = cifrador.doFinal(Base64.getDecoder().decode(mensajeCifrado.getBytes(StandardCharsets.UTF_8)));
        return new String(mensajeDescifrado);
    }
    public static String cifrarMensaje(String mensaje, PrivateKey clavePrivadaEmisor, PublicKey clavePublicaReceptor) throws Exception {
        String mensajeHasheado = cifrarPrivadaRSA(clavePrivadaEmisor, hashear(mensaje));
        String mensajeCifrado = cifrarPublicaRSA(clavePublicaReceptor, mensaje);
        return mensajeHasheado + delimitadorCodificado + mensajeCifrado;
    }

    public static String descifrarMensaje(String mensaje, PrivateKey clavePrivadaReceptor, PublicKey clavePublicaEmisor) throws Exception {
        String[] partes = mensaje.split(delimitadorCodificado);
        String mensajeDesencriptado = descifrarPrivadaRSA(clavePrivadaReceptor, partes[1]);
        String mensajeHashDesencriptado= descifrarPublicaRSA(clavePublicaEmisor, partes[0]);
        if (mensajeHashDesencriptado.equals(hashear(mensajeDesencriptado))) return mensajeDesencriptado;
        return "";
    }

    public static String hashear(String mensaje){
        try {
            // Crear una instancia del objeto MessageDigest con el algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Obtener los bytes del string de entrada
            byte[] encodedHash = digest.digest(mensaje.getBytes(StandardCharsets.UTF_8));

            // Convertir los bytes de hash en una representación hexadecimal
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String clavePublicaBase64 (PublicKey clavePublica){
        return Base64.getEncoder().encodeToString(clavePublica.getEncoded());
    }
    public static PublicKey base64ClavePublica (String clavePublica){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(clavePublica)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void main(String[] args) throws Exception {
        // Generar un par de claves RSA
        KeyPair parDeClavesEmisor = generarClaveRSA();
        PublicKey clavePublicaEmisor = base64ClavePublica(clavePublicaBase64(parDeClavesEmisor.getPublic()));
        PrivateKey clavePrivadaEmisor = parDeClavesEmisor.getPrivate();

        KeyPair parDeClavesReceptor = generarClaveRSA();
        PublicKey clavePublicaReceptor = base64ClavePublica(clavePublicaBase64(parDeClavesReceptor.getPublic()));
        PrivateKey clavePrivadaReceptor = parDeClavesReceptor.getPrivate();

        // Mensaje a cifrar
        String mensajeOriginal = "Hola, este es un mensaje secreto.";

        // Cifrar el mensaje
        String mensajeCifrado = cifrarMensaje(mensajeOriginal, clavePrivadaEmisor, clavePublicaReceptor);

        // Descifrar el mensaje
        String mensajeDescifrado = descifrarMensaje(mensajeCifrado, clavePrivadaReceptor, clavePublicaEmisor);

        System.out.println("Mensaje descifrado: " + mensajeDescifrado);


    }
}
