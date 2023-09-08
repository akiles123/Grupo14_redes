import java.security.*;
import javax.crypto.Cipher;
import java.util.Base64;

public class PruebaDeRSA {

    public static void main(String[] args) throws Exception {
        // Genera un par de claves RSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Tamaño de clave, puedes ajustarlo según tus necesidades
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Mensaje a cifrar
        String mensaje = "Hola, este es un mensaje secreto.";

        // Cifra el mensaje con la clave pública
        byte[] mensajeCifrado = cifrarMensaje(mensaje, publicKey);

        // Convierte el mensaje cifrado a una representación en base64 para su fácil transporte
        String mensajeCifradoBase64 = Base64.getEncoder().encodeToString(mensajeCifrado);

        System.out.println("Mensaje cifrado: " + mensajeCifradoBase64);

        // Descifra el mensaje con la clave privada
        String mensajeDescifrado = descifrarMensaje(Base64.getDecoder().decode(mensajeCifradoBase64), privateKey);

        System.out.println("Mensaje descifrado: " + mensajeDescifrado);
    }

    // Cifra un mensaje con la clave pública RSA
    public static byte[] cifrarMensaje(String mensaje, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(mensaje.getBytes());
    }

    // Descifra un mensaje con la clave privada RSA
    public static String descifrarMensaje(byte[] mensajeCifrado, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] mensajeDescifrado = cipher.doFinal(mensajeCifrado);
        return new String(mensajeDescifrado);
    }
}
