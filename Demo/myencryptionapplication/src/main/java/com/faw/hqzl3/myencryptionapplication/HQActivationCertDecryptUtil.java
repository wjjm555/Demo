package com.faw.hqzl3.myencryptionapplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class HQActivationCertDecryptUtil {

    public enum Status {
        EFFECTIVE,//有效的证书
        INVALID_UNKNOWN,//不明原因的无效
        INVALID_SYSTEMTIME,//车机时间无效，车机时间未校验导致
        INVALID_OUTOFVALIDITY,//证书过期
        INVALID_CERTFORMAT,//证书格式错误，可能是加密端代码有变化导致
        INVALID_FILEREAD,//文件读取错误，可能是权限问题
        INVALID_DECRYPT,//解密错误，可能是密码错误或者其他原因导致
        INVALID_CERTCONTENT//证书内容错误，可能是加密端代码有变化导致
    }

    public static HQActivationCertDecryptUtil getInstance() {
        return InnerHolder.INSTANCE;
    }

    /**
     *分析当前证书是否在有效期内
     * @param path USB挂载路径
     * @return 解密分析状态
     */
    public Status validation(String path) {
        return validation(path, "");
    }

    /**
     * 分析当前证书是否在有效期内
     * @param path USB挂载路径
     * @param password 密码
     * @return 解密分析状态
     */
    public Status validation(String path, String password) {
        if (!path.endsWith(File.separator)) path += File.separator;
        try {
            byte[] encryptedData = readFromFile(path);
            String realData = decrypt(password, encryptedData);
            String[] interval = realData.split(DELIMITER);
            if (interval.length > 1) {
                long start = Long.valueOf(interval[0]);
                long end = Long.valueOf(interval[1]);
                long current = System.currentTimeMillis();
                if (current > start) {
                    if (current < end)
                        return Status.EFFECTIVE;
                    else
                        return Status.INVALID_OUTOFVALIDITY;
                } else {
                    return Status.INVALID_SYSTEMTIME;
                }
            } else return Status.INVALID_CERTFORMAT;
        } catch (IOException ignore) {
            return Status.INVALID_FILEREAD;
        } catch (GeneralSecurityException ignore) {
            return Status.INVALID_DECRYPT;
        } catch (NumberFormatException ignore) {
            return Status.INVALID_CERTCONTENT;
        }
    }

    private final String DELIMITER = "&";
    private final String FileName = "activation.cert";
    private final String AES = "AES";
    private final String UTF = "UTF-8";
    private final String ISO = "ISO8859-1";

    private static class InnerHolder {
        private static final HQActivationCertDecryptUtil INSTANCE = new HQActivationCertDecryptUtil();
    }

    private String decrypt(String password, byte[] data) throws UnsupportedEncodingException, GeneralSecurityException {
        if (password == null || "".equals(password))
            return decrypt(data);
        return decryptData(data, getSecrtKey(password));
    }

    private String decrypt(byte[] data) throws UnsupportedEncodingException, GeneralSecurityException {
        return decrypt(Object.class.getName(), data);
    }

    private byte[] getSecrtKey(String encrypted) throws UnsupportedEncodingException {
        byte[] bytes = encrypted.getBytes(ISO);
        return Arrays.copyOf(bytes, 16);
    }

    private String decryptData(byte[] data, byte[] key) throws GeneralSecurityException, UnsupportedEncodingException {
        Key k = toKey(key);
        byte[] raw = k.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(raw, AES);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] bytes = cipher.doFinal(data);
        return new String(bytes, UTF);
    }

    private Key toKey(byte[] key) {
        return new SecretKeySpec(key, AES);
    }

    private byte[] readFromFile(String path) throws IOException {
        File file = new File(path + FileName);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] data = new byte[1024];
        int length = fileInputStream.read(data);
        fileInputStream.close();
        return Arrays.copyOf(data, length);
    }

}
