package com.faw.hqzl3.myencryptionapplication;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class HQActivationCertGenerateUtil {

    public static void main(String[] args) {
        System.out.println(start(args));
    }

    public static void test() {
        try {
            writeToFile(encrypt("cjm123..", "This is test"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public static String start(String[] args) {
        String result = "Unknown Mistake";
        if (args.length > 0) {
            String arg1 = args[0];
            if (null != arg1 && !"".equals(arg1)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                try {
                    Date date = simpleDateFormat.parse(arg1);
                    if (date != null) {
                        long current = System.currentTimeMillis();
                        long deadline = date.getTime();
                        String password = "";
                        if (args.length > 1) {
                            password = args[1];
                            if (password.contains(DELIMITER)) {
                                result = "Invalid Parameter Password can not contains '" + DELIMITER + "' !";
                                return result;
                            }
                        }
                        String content = current + DELIMITER + deadline;
                        byte[] data = encrypt(password, content);

                        writeToFile(data);

                        result = "Certificate created successfully !";
                    } else {
                        result = "Invalid Parameter Date";
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    result = "Invalid Parameter ParseException";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    result = "Invalid Parameter UnsupportedEncodingException";
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    result = "Invalid Parameter GeneralSecurityException";
                } catch (IOException e) {
                    e.printStackTrace();
                    result = "Invalid Parameter IOException";
                }
            } else {
                result = "Invalid Parameter arg1";
            }
        } else {
            result = "Invalid Parameter";
        }
        return result;
    }


    private static final String DELIMITER = "&";
    private static final String FileName = "activation.cert";
    private static final String AES = "AES";
    private static final String UTF = "UTF-8";
    private static final String ISO = "ISO8859-1";

    private static byte[] encrypt(String data) throws UnsupportedEncodingException, GeneralSecurityException {
        return encrypt(Object.class.getName(), data);
    }

    public static byte[] encrypt(String password, String data) throws UnsupportedEncodingException, GeneralSecurityException {
        if (password == null || "".equals(password))
            return encrypt(data);
        return encryptData(data, getSecrtKey(password));
    }


    private static byte[] getSecrtKey(String encrypted) throws UnsupportedEncodingException {
        byte[] bytes = encrypted.getBytes(ISO);
        return Arrays.copyOf(bytes, 16);
    }

    private static byte[] encryptData(String data, byte[] key) throws GeneralSecurityException, UnsupportedEncodingException {
        Key k = toKey(key);
        byte[] raw = k.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(data.getBytes(UTF));
    }

    private static Key toKey(byte[] key) {
        return new SecretKeySpec(key, AES);
    }

    public static void writeToFile(byte[] data) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + FileName);
        if (file.exists()) file.delete();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(data);
        fileOutputStream.flush();
        fileOutputStream.close();
    }
}
