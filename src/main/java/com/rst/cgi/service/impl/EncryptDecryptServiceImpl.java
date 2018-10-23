package com.rst.cgi.service.impl;

import com.rst.cgi.common.utils.AESUtil;
import com.rst.cgi.common.utils.StringUtil;
import com.rst.cgi.service.EncryptDecryptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by hujia on 2017/4/18.
 */
@Service
public class EncryptDecryptServiceImpl implements EncryptDecryptService {
    private final Logger logger = LoggerFactory.getLogger(EncryptDecryptService.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public byte[] encrypt(byte[] data, String keyIndex) {
        String key = aesKeyFrom(keyIndex);
        return AESUtil.aesEncrypt(data, key);
    }

    @Override
    public byte[] decrypt(byte[] data, String keyIndex) {
        String key = aesKeyFrom(keyIndex);
        logger.info("TOKEN-CODE:{},keyValue:{}",keyIndex,key);
        return AESUtil.aesDecrypt(data, key);
    }

    @Override
    public String getKey(String keyIndex, boolean create) {
        if (create) {
            stringRedisTemplate.opsForValue().set(keyIndex, generateAesKey(), 1, TimeUnit.HOURS);
        }

        String key = aesKeyFrom(keyIndex);
        return key;
    }

    @Override
    public void saveKey(String keyIndex, String key) {
        stringRedisTemplate.opsForValue().set(keyIndex, key, 1, TimeUnit.HOURS);
    }

    private String generateAesKey() {
        try {
            return StringUtil.Bit16(UUID.randomUUID().toString());
        } catch (Exception e) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                builder.append("" + (int) (Math.random() * 10));
            }
            return builder.toString();
        }
    }

    private String aesKeyFrom(String index) {
        String key = "";
        if (!StringUtils.isEmpty(index) && stringRedisTemplate.hasKey(index)) {
            key = stringRedisTemplate.opsForValue().get(index);

            if (!StringUtils.isEmpty(key)) {
                stringRedisTemplate.opsForValue().set(index, key, 1, TimeUnit.HOURS);
            }
        }

        return key;
    }
}
