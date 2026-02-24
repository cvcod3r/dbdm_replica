import com.kingbase8.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class KingBasePassword {

    private static final int ITERATIONS = 4096;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static void main(String[] args) {
//        SCRAM-SHA-256$4096:Q6IaEieIOMnFhsdE9rl75g==$z7YiMKMLX/06gKwwARmwEa97mJly23Mp+dUaL1gneWQ=:lKb5ljs+KsWfVBZ47/ucZ5cuFLJV6ehb2RioxlJTCiQ=
        String password = "root"; // Replace with your password
        String encrypted = encryptPassword(password);
        System.out.println("Encrypted password: " + encrypted);
    }

    public static String encryptPassword(String password) {
        try {
//            SecureRandom random = new SecureRandom();
//            byte[] salt = new byte[16];
//            random.nextBytes(salt);

            String saltBase64 = "Q6IaEieIOMnFhsdE9rl75g==";
            byte[] salt = Base64.decode(saltBase64);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            String hashBase64 = Base64.encodeBytes(hash);
//            StringBuilder sb = new StringBuilder();
//            sb.append("pbkdf2_sha256$");
//            sb.append(ITERATIONS).append("$");
//            sb.append(toHex(salt)).append("$");
//            sb.append(toHex(hash));
            return hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String toHex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
