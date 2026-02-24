package com.dbms.utils;

 import java.nio.charset.StandardCharsets;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;


public class Sha512Utils {
    /**
     * 传入文本内容，返回 SHA-1 串
     *
     * @param strText
     * @return
     */
    public static String SHA1(final String strText) { return SHA(strText, "SHA-1"); }

    /**
     * 传入文本内容，返回 SHA-1 串
     *
     * @param strText
     * @return
     */
    public static String SHA224(final String strText) { return SHA(strText, "SHA-224"); }

    /**
     * 传入文本内容，返回 SHA-256 串
     *
     * @param strText
     * @return
     */
    public static String SHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }

    /**
     * 传入文本内容，返回 SHA-512 串
     *
     * @param strText
     * @return
     */
    public static String SHA512(final String strText) {
        return SHA(strText, "SHA-512");
    }

    /**
     * md5加密
     * @param strText
     * @return
     */
    public static String SHAMD5(String strText) {
        return SHA(strText, "MD5");
    }

    /**
     * 字符串 SHA 加密
     *
     * @return
     */
    private static String SHA(final String strText, final String strType) {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (strText != null && strText.length() > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象 并傳入加密類型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
//                messageDigest.update(strText.getBytes());
                messageDigest.update(strText.getBytes());
                // 得到 byte 類型结果
                byte byteBuffer[] = messageDigest.digest();

                // 將 byte 轉換爲 string
                StringBuffer strHexString = new StringBuffer();
                // 遍歷 byte buffer
                for (int i = 0; i < byteBuffer.length; i++) {
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回結果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return strResult;
    }
    /**
     * 字符串 MD5 加密
     *
     * @return
     */


//    public static void main(String[] args) {
//        Sha512Utils sha = new Sha512Utils();
////        System.out.println("SHA256加密== " + sha.SHA256("123"));
//        System.out.println("SHA512加密== " + sha.SHA512("123"));
////        System.out.println("SHAMD5加密== " + sha.SHAMD5("123"));
//    }

}
