package com.dbms.utils;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DesensitizationUtil {

    private String regEx;

    private String param;

    // 识别数据
    public static boolean sensitiveData(String str, String regEx){


        return true;
    }

    public static String desensitizationData(String str, String regEx, String param){

        if (StringUtils.isEmpty(str)){
            return null;
        }else {
            return str.replaceAll(regEx, param);
        }
    }

    private static final String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int RANDOM_STRING_LENGTH = 10;
    /**
     * 随机化算法
     * @param data
     * @return
     */
    public static String randomize(String data) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (Character.isLetterOrDigit(c)) { // 如果是字母或数字则替换为随机字符
                sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
            } else {
                sb.append(c); // 否则保持原字符不变
            }
        }
        return sb.toString();
    }

    private static final String SECRET_KEY = "0123456789abcdef"; // 密钥必须为16、24或32位字符串

    /**
     * AES对称加密算法
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }


    /**
     * SHA-256算法
     * @param data
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String hash256(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b)); // 将每个字节转换成两位十六进制数，并拼接成哈希值
        }
        return sb.toString();
    }


    private static final int MAX_LENGTH = 5; // 截断后的最大长度

    /**
     * 字符串剪切算法
     * @param data
     * @return
     */
    public static String cut(String data) {
        StringBuilder sb = new StringBuilder();
        String[] words = data.split("\\s+"); // 按空格分隔单词
        for (String word : words) {
            if (word.length() > MAX_LENGTH) {
                word = word.substring(0, MAX_LENGTH) + "..."; // 超过最大长度时截断，并在末尾添加省略号
            }
            sb.append(word).append(" ");
        }
        return sb.toString().trim();
    }

    private static final int PADDING_LENGTH = 5; // 填充的长度
    private static final char PADDING_CHAR = '*'; // 填充的字符

    /**
     * 字符串填充算法
     * @param data
     * @return
     */
    public static String pad(String data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PADDING_LENGTH; i++) {
            sb.append(PADDING_CHAR); // 在前面添加填充字符
        }
        sb.append(data);
        for (int i = 0; i < PADDING_LENGTH; i++) {
            sb.append(PADDING_CHAR); // 在后面添加填充字符
        }
        return sb.toString();
    }

    /**
     * 字符串随机打乱算法
     * @param data
     * @return
     */
    public static String shuffle(String data) {
        List<Character> charList = new ArrayList<>();
        for (char c : data.toCharArray()) {
            charList.add(c);
        }
        Collections.shuffle(charList); // 随机打乱字符顺序
        StringBuilder sb = new StringBuilder();
        for (char c : charList) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static final int ADD_LENGTH = 5; // 添加的长度

    /**
     * 字符串随机添加算法
     * @param data
     * @return
     */
    public static String add(String data) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ADD_LENGTH; i++) {
            char c = (char) (random.nextInt(26) + 'a'); // 随机生成小写字母
            sb.append(c);
        }
        sb.append(data);
        for (int i = 0; i < ADD_LENGTH; i++) {
            char c = (char) (random.nextInt(26) + 'a'); // 随机生成小写字母
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 字符串转十六进制编码算法
     * @param data
     * @return
     */
    public static String toHex(String data) {
        StringBuilder sb = new StringBuilder();
        for (char c : data.toCharArray()) {
            String hex = Integer.toHexString(c);
            sb.append(hex.length() == 1 ? "0" + hex : hex); // 补齐两位数
        }
        return sb.toString().toUpperCase(); // 转为大写字母
    }

    /**
     * Base64编码算法
     * @param data
     * @return
     */
    public static String toBase64(String data) {
        byte[] bytes = data.getBytes();
        byte[] encodedBytes = Base64.getEncoder().encode(bytes);
        return new String(encodedBytes);
    }

    /**
     *
     * 身份证号脱敏,替换前面保留6位，后面保留4位，其余用指定字符代替
     * @param idCardNumber
     * @return
     */
    public static String maskIDCardNumber(String idCardNumber) {
        if (idCardNumber.length() < 10) {
            return idCardNumber;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(idCardNumber.substring(0, 6));
        for (int i = 6; i < idCardNumber.length() - 4; i++) {
            sb.append("*");
        }
        sb.append(idCardNumber.substring(idCardNumber.length() - 4));
        return sb.toString();
    }

    /**
     * 银行卡号脱敏 - 替换前面保留4位，其余用指定字符代替
     * @param bankCardNumber
     * @return
     */
    public static String maskBankCardNumber(String bankCardNumber) {
        if (bankCardNumber.length() < 8) {
            return bankCardNumber;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(bankCardNumber.substring(0, 4));
        for (int i = 4; i < bankCardNumber.length(); i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    /**
     * 手机号脱敏 - 乱序
     * @param phoneNumber
     * @return
     */
    public static String scramblePhoneNumber(String phoneNumber) {
        if (phoneNumber.length() != 11) {
            return phoneNumber;
        }
        List<Character> digits = new ArrayList<>();
        for (char c : phoneNumber.toCharArray()) {
            digits.add(c);
        }
        Collections.shuffle(digits.subList(3, 7));

        StringBuilder sb = new StringBuilder();
        for (Character digit : digits) {
            sb.append(digit);
        }
        return sb.toString();
    }

    /**
     * 姓名脱敏 - 只保留姓氏，其余用指定字符代替
     * @param name
     * @return
     */
    public static String maskNameOnlySurname(String name) {
        if (name.length() == 0) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length(); i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    /**
     * 银行卡号脱敏 - 后四位保留，前面用指定字符代替
     * @param bankCardNumber
     * @return
     */
    public static String maskBankCardNumberKeepLastFour(String bankCardNumber) {
        if (bankCardNumber.length() <= 4) {
            return bankCardNumber;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bankCardNumber.length() - 4; i++) {
            sb.append("*");
        }
        sb.append(bankCardNumber.substring(bankCardNumber.length() - 4));
        return sb.toString();
    }

    /**
     * 姓名脱敏 - 随机生成虚拟姓名
     * @return
     */
    public static String generateVirtualName() {
        String[] firstName = { "张", "王", "李", "赵", "刘", "陈", "杨", "黄", "吴", "周" };
        String[] lastName = { "伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "洋" };

        Random random = new Random();
        String randomFirstName = firstName[random.nextInt(firstName.length)];
        String randomLastName = lastName[random.nextInt(lastName.length)];

        return randomFirstName + randomLastName;
    }

    /**
     * 邮箱脱敏 - 将邮箱地址隐藏为前两位和后缀保留
     * @param email
     * @return
     */
    public static String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(email.charAt(0));
        sb.append(email.charAt(1));
        sb.append("****");
        sb.append(email.substring(atIndex));
        return sb.toString();
    }

    /**
     * 地址脱敏 - 隐藏详细地址信息，只显示省份和城市
     * @param address
     * @return
     */
    public static String maskAddress(String address) {
        String[] parts = address.split(" ");
        if (parts.length < 2) {
            return address;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(parts[0]);
        sb.append(" ");
        sb.append(parts[1]);
        for (int i = 2; i < parts.length; i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    /**
     * 密码脱敏 - 将密码替换为指定长度的星号
     * @param password
     * @return
     */
    public static String maskPassword(String password) {
        StringBuilder sb = new StringBuilder();
        int starCount = sb.length();
        for (int i = 0; i < starCount; i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    /**
     * IP地址脱敏 - 将IP地址的最后一位替换为指定字符
     * @param ipAddress
     * @return
     */
    public static String maskIPAddress(String ipAddress) {
        char replacement = '*';
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return ipAddress;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(parts[i]);
            sb.append(".");
        }
        sb.append(replacement);
        return sb.toString();
    }

    /**
     * 日期脱敏 - 将日期隐藏为年份和月份
     * @param date
     * @return
     */
    public static String maskDate(String date) {
        String[] parts = date.split("-");
        if (parts.length != 3) {
            return date;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(parts[0]);
        sb.append("-");
        sb.append(parts[1]);
        for (int i = 2; i < parts.length; i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    /**
     * 职位脱敏 - 隐藏职位名称
     * @param jobTitle
     * @return
     */
    public static String maskJobTitle(String jobTitle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jobTitle.length(); i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    /**
     * 车牌号脱敏 - 隐藏车牌号的后四位字符
     * @param licensePlate
     * @return
     */
    public static String maskLicensePlate(String licensePlate) {
        if (licensePlate.length() <= 4) {
            return licensePlate;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < licensePlate.length() - 4; i++) {
            sb.append("*");
        }
        sb.append(licensePlate.substring(licensePlate.length() - 4));
        return sb.toString();
    }

    /**
     * URL脱敏 - 隐藏URL的指定部分
     * @param url
     * @return
     */
    public static String maskUrl(String url) {
        int startIndex = 5;
        int endIndex = 10;
        char replacement = ' ';
        StringBuilder sb = new StringBuilder(url);
        for (int i = startIndex; i <= endIndex; i++) {
            sb.setCharAt(i, replacement);
        }
        return sb.toString();
    }

    /**
     * 姓名拼音脱敏 - 将姓名拼音隐藏为指定长度的星号
     * @param pinyinName
     * @return
     */
    public static String maskPinyinName(String pinyinName) {
        StringBuilder sb = new StringBuilder();
        int starCount = sb.length();
        for (int i = 0; i < starCount; i++) {
            sb.append("*");
        }
        sb.append(pinyinName.substring(starCount));
        return sb.toString();
    }


    /**
     * 驾驶证号脱敏 - 隐藏驾驶证号的中间部分
     * @param licenseNumber
     * @return
     */
    public static String maskDriverLicenseNumber(String licenseNumber) {
        if (licenseNumber.length() < 6) {
            return licenseNumber;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(licenseNumber.substring(0, 2));
        for (int i = 0; i < licenseNumber.length() - 6; i++) {
            sb.append("*");
        }
        sb.append(licenseNumber.substring(licenseNumber.length() - 4));
        return sb.toString();
    }

    /**
     * 健康卡号脱敏 - 隐藏健康卡号的前几位和后几位
     * @param cardNumber
     * @return
     */
    public static String maskHealthCardNumber(String cardNumber) {
        if (cardNumber.length() < 8) {
            return cardNumber;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(cardNumber.substring(0, 4));
        for (int i = 0; i < cardNumber.length() - 8; i++) {
            sb.append("*");
        }
        sb.append(cardNumber.substring(cardNumber.length() - 4));
        return sb.toString();
    }

    /**
     * 年龄范围脱敏
     * @param age
     * @return
     */
    public static String maskAgeRange(String age) {
        int ageValue = Integer.parseInt(age);
        if (ageValue >= 0 && ageValue <= 10) {
            return "0-10";
        } else if (ageValue >= 11 && ageValue <= 20) {
            return "11-20";
        } else if (ageValue >= 21 && ageValue <= 30) {
            return "21-30";
        } else if (ageValue >= 31 && ageValue <= 40) {
            return "31-40";
        } else if (ageValue >= 41 && ageValue <= 50) {
            return "41-50";
        } else if (ageValue >= 51 && ageValue <= 60) {
            return "51-60";
        } else if (ageValue >= 61 && ageValue <= 70) {
            return "61-70";
        } else {
            return "Unknown";
        }
    }

    /**
     * 时间偏移脱敏，随机提前延后天数和时间
     * @param dateTimeStr
     * @return
     */
    public static String maskDateTime(String dateTimeStr) {
        // 生成随机数生成器
        Random random = new Random();

        // 生成 -24 到 24 之间的随机偏移天数和小时数
        int daysOffset = random.nextInt(49) - 24;  // 生成 -24 到 24 之间的整数
        int hoursOffset = random.nextInt(49) - 24; // 生成 -24 到 24 之间的整数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        LocalDateTime maskedDateTime = dateTime.plusDays(daysOffset).plusHours(hoursOffset);
        return maskedDateTime.format(formatter);
    }

    /**
     * 缩短字符串（将字符串缩短为指定长度，例如将"abcdefghijklmno"变为"abc..."）
     * @param str
     * @return
     */
    public static String shortenString(String str) {
        int maxLength = 3;
        if (str.length() <= maxLength) {
            return str;
        }

        return str.substring(0, maxLength) + "...";
    }

    /**
     * 异或加密
     * @param input
     * @return
     */
    public static String xorEncrypt(String input) {
        String key = "secret";
        StringBuilder encrypted = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            char k = key.charAt(i % key.length());
            char encryptedChar = (char) (c ^ k); // Perform XOR operation

            encrypted.append(encryptedChar);
        }

        return encrypted.toString();
    }

    /**
     * 文件路径脱敏
     * @param path
     * @return
     */
    public static String hidePath(String path) {
        int lastSeparatorIndex = path.lastIndexOf('/');
        String directoryPart = path.substring(0, lastSeparatorIndex + 1);
        String filePart = path.substring(lastSeparatorIndex + 1);
        int extensionIndex = filePart.lastIndexOf('.');
        String fileName = filePart.substring(0, extensionIndex);
        String fileExtension = filePart.substring(extensionIndex);

        StringBuilder hiddenPath = new StringBuilder();
        hiddenPath.append(directoryPart);
        for (int i = 0; i < fileName.length(); i++) {
            hiddenPath.append('*');
        }
        hiddenPath.append(fileExtension);

        return hiddenPath.toString();
    }

    /**
     * 隐藏用户名：对电子邮件地址或用户名中的用户名部分进行隐藏，例如将"john@domain.com"变为"****@domain.com"。
     * @param email
     * @return
     */
    public static String hideUsername(String email) {
        int separatorIndex = email.indexOf('@');
        String username = email.substring(0, separatorIndex);
        String domain = email.substring(separatorIndex);

        StringBuilder hiddenEmail = new StringBuilder();
        for (int i = 0; i < username.length(); i++) {
            hiddenEmail.append('*');
        }
        hiddenEmail.append(domain);

        return hiddenEmail.toString();
    }

    /**
     * 隐藏域名：对电子邮件地址或URL中的域名部分进行隐藏，例如将"john@domain.com"变为"john@******.com"。
     * @param email
     * @return
     */
    public static String hideDomain(String email) {
        int separatorIndex = email.indexOf('@');
        String username = email.substring(0, separatorIndex);
        String domain = email.substring(separatorIndex);
        int dotIndex = domain.lastIndexOf('.');
        String maskedDomain = domain.substring(0, dotIndex);

        StringBuilder hiddenEmail = new StringBuilder(username);
        hiddenEmail.append('@');
        for (int i = 0; i < maskedDomain.length(); i++) {
            hiddenEmail.append('*');
        }
        hiddenEmail.append(domain.substring(dotIndex));

        return hiddenEmail.toString();
    }


    private static final int MAX_WHITESPACE_COUNT = 5; // 最大空格数量，可根据需求修改

    /**
     * 随机增减空格：在字符串中增加或减少随机数量的空格，改变文本的格式。
     * @param text
     * @return
     */
    public static String addRandomWhitespace(String text) {
        Random random = new Random();
        int whitespaceCount = random.nextInt(MAX_WHITESPACE_COUNT + 1); // 随机生成空格数量

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < whitespaceCount; i++) {
            sb.append(' ');
        }
        sb.append(text);

        return sb.toString();
    }

    private static final double DISTORTION_RANGE = 0.001; // 调整范围，可根据需求修改

    public static String doubleArrayToString(double[] array) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            builder.append(String.valueOf(array[i]));
            if (i != array.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    /**
     * 位置扰动：对地理坐标进行随机扰动，例如通过加减一个随机小数来改变经纬度。
     * @param coordinates
     * @return
     */
    public static String distortCoordinates(String coordinates) {
        String[] parts = coordinates.split(" ");
        double latitude = Double.parseDouble(parts[0]);
        double longitude = Double.parseDouble(parts[1]);

        Random random = new Random();
        double latDistortion = random.nextDouble() * 2 * DISTORTION_RANGE - DISTORTION_RANGE;
        double lonDistortion = random.nextDouble() * 2 * DISTORTION_RANGE - DISTORTION_RANGE;

        double distortedLatitude = latitude + latDistortion;
        double distortedLongitude = longitude + lonDistortion;
        return doubleArrayToString(new double[]{distortedLatitude, distortedLongitude});
    }

    // 生成 DSA 密钥对
    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
        keyGen.initialize(1024);
        return keyGen.generateKeyPair();
    }

    /**
     * DSA（Digital Signature Algorithm）是一种基于整数有限域离散对数难题的数字签名算法。
     * @param plaintext
     * @return
     * @throws Exception
     */
    // 使用私钥对明文进行签名，并将签名结果转换为字符串
    public static String signAndConvertToString(String plaintext) throws Exception {
        // 生成密钥对
        KeyPair keyPair = generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        Signature signature = Signature.getInstance("SHA256withDSA");
        signature.initSign(privateKey);
        signature.update(plaintext.getBytes());
        byte[] signBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signBytes);
    }


    // 生成 RSA 密钥对
    private static KeyPair generateKeyPairRSA() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom();
        keyGen.initialize(2048, secureRandom);
        return keyGen.generateKeyPair();
    }

    /**
     * RSA（Rivest-Shamir-Adleman）是一种非对称加密算法，其中涉及到公钥和私钥。
     * @param plaintext
     * @return
     * @throws Exception
     */
    // 使用公钥加密明文为密文
    public static String encryptRSA(String plaintext) throws Exception {
        // 生成密钥对
        KeyPair keyPair = generateKeyPairRSA();
        PublicKey publicKey =  keyPair.getPublic();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}

