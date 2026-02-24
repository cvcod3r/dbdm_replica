package com.dbms.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1Hashing {
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        // 把密文转换成十六进制的字符串形式
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

    public static String getSHA1(String passwordSeed) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.update(passwordSeed.getBytes());
            byte[] b_itr1 = messageDigest.digest();
            messageDigest.update(b_itr1);
            String itr2 = getFormattedText(messageDigest.digest());
            return "*" + itr2.toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
//            System.out.println(e.getMessage());
            return "";
        }
    }

    /**
     * @param args
     */
//    public static void main(String[] args) {
//        String str = "root";
//        MessageDigest messageDigest;
//        try {
//            messageDigest = MessageDigest.getInstance("SHA1");
//            messageDigest.update(str.getBytes());
//            byte[] b_itr1 = messageDigest.digest();
//            messageDigest.update(b_itr1);
//            String itr2 = getFormattedText(messageDigest.digest());
//            System.out.println("*" + itr2.toUpperCase());
//
//        } catch (NoSuchAlgorithmException e) {
//            // TODO Auto-generated catch block
//            System.out.println(e.getMessage());
//        }
//    }
}
