package RSA;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class Hasheo {
    public static void main(String[] args) {
        String inputString = "Hola, mundo!";

        try {
            // Crear una instancia del objeto MessageDigest con el algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Obtener los bytes del string de entrada
            byte[] encodedHash = digest.digest(inputString.getBytes(StandardCharsets.UTF_8));

            // Convertir los bytes de hash en una representación hexadecimal
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String hashedString = hexString.toString();

            // Imprimir el resultado
            System.out.println("String original: " + inputString);
            System.out.println("String hasheado (SHA-256): " + hashedString);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}





