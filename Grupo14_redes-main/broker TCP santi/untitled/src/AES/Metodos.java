package AES;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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
    //cifra mensaje simetrico con asimetrico
    public static String cifrarMensaje(String mensaje, PrivateKey clavePrivadaEmisor, SecretKey secretKey) throws Exception {
        String mensajeHasheado = cifrarPrivadaRSA(clavePrivadaEmisor, hashear(mensaje));
        String mensajeCifrado = cifararAES(mensaje, secretKey);
        return mensajeHasheado + delimitadorCodificado + mensajeCifrado;
    }

   // Descifra mensaje simetrico con asimetrico
    public static String descifrarMensaje(String mensaje, SecretKey secretKey, PublicKey clavePublicaEmisor) throws Exception {
        String[] partes = mensaje.split(delimitadorCodificado);
        String mensajeDesencriptado = decifrarAES(partes[1], secretKey);
        String mensajeHashDesencriptado= descifrarPublicaRSA(clavePublicaEmisor, partes[0]);
        if (mensajeHashDesencriptado.equals(hashear(mensajeDesencriptado))) return mensajeDesencriptado;
        return "";
    }
// cifra con asimetrico
    public static String cifrarClaveAES(SecretKey secretKey, PrivateKey clavePrivadaEmisor, PublicKey clavePublicaReceptor) throws Exception {
        String mensajeHasheado = cifrarPrivadaRSA(clavePrivadaEmisor, hashear(secretKeyBase64(secretKey)));
        String mensajeCifrado = cifrarPublicaRSA(clavePublicaReceptor, secretKeyBase64(secretKey));
        return mensajeHasheado + delimitadorCodificado + mensajeCifrado;
    }
//decifra con asimetrico
    public static SecretKey descifrarClaveAES(String mensaje, PrivateKey clavePrivadaReceptor, PublicKey clavePublicaEmisor) throws Exception {
        String[] partes = mensaje.split(delimitadorCodificado);
        String mensajeDesencriptado = descifrarPrivadaRSA(clavePrivadaReceptor, partes[1]);
        String mensajeHashDesencriptado= descifrarPublicaRSA(clavePublicaEmisor, partes[0]);
        if (mensajeHashDesencriptado.equals(hashear(mensajeDesencriptado))) return base64SecretKey(mensajeDesencriptado);
        return null;
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
    public final static String delimitadorAES = "glikmantermitolpalpalpalpalppalpalaplaplaplosdelrojosontodossjajjajajajasjsadasdaasdadBBBBBBBBB";
    public final static String delimitadorAESCodificado = Base64.getEncoder().encodeToString(delimitadorAES.getBytes());
    public static String cifararAES(String mensaje, SecretKey clave) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, clave);
        return Base64.getEncoder().encodeToString(cipher.doFinal(mensaje.getBytes())) + delimitadorAESCodificado + Base64.getEncoder().encodeToString(cipher.getIV());
    }

    public static String decifrarAES(String mensajeEncriptado, SecretKey clave) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String[] partes = mensajeEncriptado.split(delimitadorAESCodificado);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(128, Base64.getDecoder().decode(partes[1])));
        return new String(cipher.doFinal(Base64.getDecoder().decode(partes[0])));
    }

    public static SecretKey generarClaveAES() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    public static String secretKeyBase64(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static SecretKey base64SecretKey(String base64Key) {
        return new SecretKeySpec(Base64.getDecoder().decode(base64Key), 0, Base64.getDecoder().decode(base64Key).length, "AES");
    }

    /*public static byte[] firmarRSA(PrivateKey clavePrivada, String datos) throws Exception {
        Signature firma = Signature.getInstance("SHA256withRSA");
        firma.initSign(clavePrivada);
        firma.update(datos.getBytes());
        return firma.sign();
    }*/

   /* public static <firma> boolean verificarFirmaRSA(PublicKey clavePublica, String datos, byte[firma] ) throws Exception {
        Signature firma = Signature.getInstance("SHA256withRSA");
        firma.initVerify(clavePublica);
        firma.update(datos.getBytes());
        return firma.verify(firma.sign());
    }*/

    public static void main(String[] args) throws Exception {
        // Generar un par de claves RSA
        KeyPair parDeClavesEmisor = generarClaveRSA();
        PublicKey clavePublicaEmisor = base64ClavePublica(clavePublicaBase64(parDeClavesEmisor.getPublic()));
        PrivateKey clavePrivadaEmisor = parDeClavesEmisor.getPrivate();

        KeyPair parDeClavesReceptor = generarClaveRSA();
        PublicKey clavePublicaReceptor = base64ClavePublica(clavePublicaBase64(parDeClavesReceptor.getPublic()));
        PrivateKey clavePrivadaReceptor = parDeClavesReceptor.getPrivate();

        SecretKey claveAES = generarClaveAES();

        // Mensaje a cifrar
        String mensajeOriginal = "Hola, este es un mensaje secreto.";

        // Cifrar el mensaje
        String mensajeCifrado = cifrarMensaje(mensajeOriginal, clavePrivadaEmisor, claveAES);

        // Descifrar el mensaje
        String mensajeDescifrado = descifrarMensaje(mensajeCifrado, claveAES, clavePublicaEmisor);

        System.out.println("Mensaje descifrado: " + mensajeDescifrado);


    }
}

        // Firmar y verificar datos
       /* String datos = "Estos son algunos datos para firmar y verificar.";
        byte[] firma = firmarRSA(clavePrivada, datos);
        boolean verificado = verificarFirmaRSA(clavePublica, datos, firma);
        System.out.println("Firma verificada: " + verificado);
    }*/

