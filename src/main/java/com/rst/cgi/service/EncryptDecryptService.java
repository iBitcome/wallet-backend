package com.rst.cgi.service;

/**
 * Created by hujia on 2017/4/18.
 */
public interface EncryptDecryptService {
    String KEY_INDEX_HEADER = "TOKEN-CODE";
//    String KEY_INDEX_HEADER = "ED_UUID";

//    String KEY_INDEX_HEADER_BACKUP = "token-code";//H5微信浏览器使用ed_uuid不存在时使用改字段

    /**
     * AES加密数据
     * @param data 待加密的数据
     * @param keyIndex 密钥索引
     * @return 加密后的数据
     */
    byte[] encrypt(byte[] data, String keyIndex);

    /**
     * AES解密
     * @param data 待解密数据
     * @param keyIndex 密钥索引
     * @return 解密后的数据
     */
    byte[] decrypt(byte[] data, String keyIndex);

    /**
     * 获取#{keyIndex}对应的AES密钥，#{create=true}时，则会新建一个AES密钥映射到#{keyIndex}
     * @param keyIndex AES密钥的索引
     * @param create 是否重新创建加密密钥
     * @return AES密钥
     */
    String getKey(String keyIndex, boolean create);

    /**
     * 保存aes密钥
     * @param keyIndex
     * @param key
     */
    void saveKey(String keyIndex, String key);
}
