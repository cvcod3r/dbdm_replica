package com.dbms.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class ScramSha256Utils {

    public static String calculateStoredKey(byte[] passwordSeed, byte[] salt, int iterations) throws Exception {
        byte[] saltedPassword = Hi(passwordSeed, salt, iterations);
        byte[] clientKey = HMAC(saltedPassword, "Client Key".getBytes());
        byte[] storedKey = H(clientKey);
        return Base64.getEncoder().encodeToString(storedKey);
    }

    private static byte[] HMAC(byte[] key, byte[] message) throws Exception {
        Mac hasher = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
        hasher.init(keySpec);
        return hasher.doFinal(message);
    }

    private static byte[] Hi(byte[] password, byte[] salt, int iterations) throws Exception {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(password, "HmacSHA256");
        hmacSha256.init(keySpec);

        byte[] uPrev = hmacSha256.doFinal(concatBytes(salt, intToBytes(1)));
        byte[] u = Arrays.copyOf(uPrev, uPrev.length);
        for (int i = 2; i <= iterations; i++) {
            uPrev = hmacSha256.doFinal(uPrev);
            xorBytes(u, uPrev);
        }
        return u;
    }

    private static byte[] H(byte[] data) throws Exception {
        // 这里使用SHA-256作为哈希函数
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        digest.reset();
        return digest.digest(data);
    }

    private static byte[] concatBytes(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    private static void xorBytes(byte[] dest, byte[] src) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] ^= src[i];
        }
    }

    private static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    private static byte[] intToBytes(int value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((value >> 24) & 0xFF);
        bytes[1] = (byte) ((value >> 16) & 0xFF);
        bytes[2] = (byte) ((value >> 8) & 0xFF);
        bytes[3] = (byte) (value & 0xFF);
        return bytes;
    }

}
