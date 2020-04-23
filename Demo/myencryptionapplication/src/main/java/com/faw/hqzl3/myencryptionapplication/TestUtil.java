package com.faw.hqzl3.myencryptionapplication;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TestUtil {

    private static final String ALGORITHM = "AES";

    private static Key toKey(byte[] key) {

        SecretKey secretKey = new SecretKeySpec(key, "AES");

        return secretKey;

    }

    public static String encrypt(String data, String key)

            throws GeneralSecurityException, UnsupportedEncodingException {

        Key k = toKey(Base64.decodeBase64(key));

        byte[] raw = k.getEncoded();

        SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");

        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(1, secretKeySpec);

        byte[] bytes = cipher.doFinal(data.getBytes("UTF-8"));

        return Base64.encodeBase64String(bytes);

    }

    public static String decrypt(String data, String key)

            throws GeneralSecurityException, UnsupportedEncodingException {

        Key k = toKey(Base64.decodeBase64(key));

        byte[] raw = k.getEncoded();

        SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");

        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(2, secretKeySpec);

        byte[] bytes = cipher.doFinal(Base64.decodeBase64(data));

        return new String(bytes, "UTF-8");

    }

    public static String getSecrtKey(String encrypted) throws Throwable {

        byte[] bytes = encrypted.getBytes("ISO8859-1");

        bytes = Arrays.copyOf(bytes, 16);

        return Base64.encodeBase64String(bytes);

    }

    public static String decrypt(String data) {

        String decryptData = "";

        try {

            String secrtKey = getSecrtKey(System.class.getName());

            decryptData = decrypt(data, secrtKey);

        } catch (Throwable e) {

            e.printStackTrace();

        }

        return decryptData;

    }

    public static String encrypt(String data) throws Throwable {

        String secrtKey = getSecrtKey(System.class.getName());

        return encrypt(data, secrtKey);

    }

    public static void main(String[] args) throws Throwable {

        String data = "Abl20171026";

        String encryptData = encrypt(data);

        System.out.println("加密后数据" + encryptData);

        System.out.println("解密后数据" + decrypt(encryptData));

    }
}
