package com.faw.hqzl3.myencryptionapplication;

import android.util.Log;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {


//    private static final String MiddleKey = "123456";//生成key需要的密码

    public static byte[] encrypt(String password, String content) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(password));
            return cipher.doFinal(content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String decrypt(String password, byte[] encryptedPassword) {
        try {
            // 解密
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(password));
            return new String(cipher.doFinal(encryptedPassword));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //    private static Key key;
    static byte[] byteKey;

//    public static byte[]generateSecretKey(){
//
//    }

    public static Key getKey(String password) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(password.getBytes());
            keyGenerator.init(256, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            if (byteKey == null)
                byteKey = secretKey.getEncoded();

            Log.w("CJM", "getKey:" + byteKey.length);

            Key key = new SecretKeySpec(byteKey, "AES");
            return key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
