package com.rst.cgi.common.utils;

import com.rst.cgi.common.crypto.Converter;
import lombok.Data;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author hujia
 */
public class DHUtil {
    private static final String DH_KEY_ALGORITHM = "DH";
    private static final String AES_KEY_ALGORITHM = "AES";
    private static final int DH_KEY_SIZE = 512;

    @Data
    public static class Key {
        private byte[] pub;
        private byte[] priv;

        public static Key from(DHPublicKey pub, DHPrivateKey priv) {
            Key key = new Key();
            key.priv = priv.getEncoded();
            key.pub = pub.getEncoded();
            return key;
        }

        @Override
        public String toString() {
            return "pub:" + Converter.byteArrayToHexString(pub)
                    + "\npriv:" + Converter.byteArrayToHexString(priv);
        }
    }

    public static Key initKey() throws Exception{
        //实例化密钥对生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(DH_KEY_ALGORITHM);
        //初始化密钥对生成器
        keyPairGenerator.initialize(DH_KEY_SIZE);
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //甲方公钥
        DHPublicKey publicKey = (DHPublicKey)keyPair.getPublic();
        //甲方私钥
        DHPrivateKey privateKey = (DHPrivateKey)keyPair.getPrivate();

        return Key.from(publicKey, privateKey);
    }

    public static Key initKey(byte[] key) throws Exception {
        //解析甲方公钥
        //转换公钥材料
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(DH_KEY_ALGORITHM);
        //产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        //由甲方公钥构建乙方密钥
        DHParameterSpec dhParameterSpec = ((DHPublicKey)pubKey).getParams();
        //实例化密钥对生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(DH_KEY_ALGORITHM);
        //初始化密钥对生成器
        keyPairGenerator.initialize(dhParameterSpec);
        System.out.println(dhParameterSpec.getG().toString(16));
        System.out.println(dhParameterSpec.getP().toString(16));
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //甲方公钥
        DHPublicKey publicKey = (DHPublicKey)keyPair.getPublic();
        //甲方私钥
        DHPrivateKey privateKey = (DHPrivateKey)keyPair.getPrivate();
        //将密钥对存储在Map中

        return Key.from(publicKey, privateKey);
    }

    public static byte[] aesKeyFrom(byte[] publicKey, byte[] privateKey) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(DH_KEY_ALGORITHM);
        //初始化公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
        //产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        //初始化私钥
        //密钥材料转换
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
        //产生私钥
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
        //实例化
        KeyAgreement keyAgree = KeyAgreement.getInstance(keyFactory.getAlgorithm());
        //初始化
        keyAgree.init(priKey);
        keyAgree.doPhase(pubKey, true);
        //生成本地密钥
        SecretKey secretKey = keyAgree.generateSecret(AES_KEY_ALGORITHM);

        return secretKey.getEncoded();
    }


    public static void main(String[] args) throws Exception {
        Key key1 = DHUtil.initKey();
        Key key2 = DHUtil.initKey(key1.getPub());

        System.out.println("=====key1=====");
        System.out.println(key1);
        System.out.println("=====key2=====");
        System.out.println(key2);

        byte[] aseKey1 = aesKeyFrom(key2.getPub(), key1.getPriv());
        byte[] aseKey2 = aesKeyFrom(key1.getPub(), key2.getPriv());

        System.out.println("=====aesKey1=====");
        System.out.println(Converter.byteArrayToHexString(aseKey1));
        System.out.println("=====aesKey2=====");
        System.out.println(Converter.byteArrayToHexString(aseKey2));
    }
}
