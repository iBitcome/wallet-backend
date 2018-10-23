package com.rst.cgi.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by hujia on 2017/2/28.
 */
public class AESUtil {

    public static final String IV_STRING = "rst@123456--java";

    public static byte[] aesEncrypt(byte[] data, String key) {
        try {
            byte[] enCodeFormat = key.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
            byte[] initParam = IV_STRING.getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);

            // 指定加密的算法、工作模式和填充方式
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            return cipher.doFinal(data);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] aesDecrypt(byte[] data, String key) {
        return aesDecrypt(data, key.getBytes());
    }

    public static byte[] aesDecrypt(byte[] data, byte[] key) {
        try {
            //key
            byte[] enCodeFormat = key;
            SecretKeySpec secretKey = new SecretKeySpec(enCodeFormat, "AES");
            //iv
            byte[] initParam = IV_STRING.getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            //加密器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //thrift
    public static void main(String[] args) throws Exception {

    }
}
