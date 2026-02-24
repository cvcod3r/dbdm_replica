package com.dbms.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Encryption {
    public static String encrypt(String username, String password) {
        String encryptedText = "";

        try {
            // 将用户名和密码拼接起来
            String input = password + username;

            // 创建MD5加密算法的实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 计算MD5哈希值
            byte[] mdBytes = md.digest(input.getBytes());

            // 将字节数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : mdBytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

            // 拼接加密后的字符串
            encryptedText = "md5" + sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return encryptedText;
    }

//        public static void main(String[] args) {
//            String username = "example_username";
//            String password = "example_password";
//
//            String encrypted = encrypt(username, password);
//            System.out.println("加密后的字符串: " + encrypted);
//        }
}
