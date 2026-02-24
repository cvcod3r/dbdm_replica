package com.dbms.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.xml.bind.DatatypeConverter;

public class Sha256Hashing {
//    public static void main(String[] args) {
//        String word = "GBase_8s";
//        String salt = "773311349008911309";
//        String finalHash = "";
//        byte[] bword = word.getBytes();
//        byte[] saltByte = new BigInteger(salt).toByteArray();
//        byte[] resultSalt = bword;
//        resultSalt = concat(saltByte,bword);
//        MessageDigest ma;
//        try {
//            ma = MessageDigest.getInstance("SHA-256");
//            ma.update(resultSalt,0,resultSalt.length);
//            byte[] aux = ma.digest();
//            finalHash = getLowerCaseHash(aux);
//            System.out.println(finalHash);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        byte[] aux = DatatypeConverter.parseHexBinary(finalHash);
//        String finalHashBase64 = DatatypeConverter.printBase64Binary(aux);
//        System.out.println(finalHashBase64);
//
//        String GbaseHashBase64 = finalHashBase64.replace('+', '.').replace('/', '_');
//        System.out.println(GbaseHashBase64);
//    }

    public static String gbase8sHash(String password, String salt) {
        String finalHash = "";
        byte[] bword = password.getBytes();
        byte[] saltByte = new BigInteger(salt).toByteArray();
        byte[] resultSalt = concat(saltByte,bword);
        MessageDigest ma;
        try {
            ma = MessageDigest.getInstance("SHA-256");
            ma.update(resultSalt,0,resultSalt.length);
            byte[] aux = ma.digest();
            finalHash = getLowerCaseHash(aux);
            System.out.println(finalHash);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        byte[] aux = DatatypeConverter.parseHexBinary(finalHash);
        String finalHashBase64 = DatatypeConverter.printBase64Binary(aux);
        System.out.println(finalHashBase64);

        String GbaseHashBase64 = finalHashBase64.replace('+', '.').replace('/', '_');
        return GbaseHashBase64;
    }
    public static byte[] concat(byte[] source, byte[] append){
        byte[] result = new byte[source.length + append.length];
        for (int i = 0; i < source.length; i++){
            result[i] = source[i];
        }
        int auxIndex = source.length;
        for (int i = 0; i < append.length; i++){
            result[auxIndex] = append[i];
            auxIndex = auxIndex + 1;
        }
        return result;
    }
    public static String getLowerCaseHash(byte[] b){
        return DatatypeConverter.printHexBinary(b).toLowerCase();
    }
}
