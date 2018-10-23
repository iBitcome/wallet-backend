package com.rst.cgi.service.impl;

import com.rst.cgi.data.dao.mysql.WalletDao;
import com.rst.cgi.data.dto.request.InitWalletDTO;
import com.rst.cgi.service.PushDeviceService;
import com.rst.cgi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Service
public class PushDeviceServiceImpl implements PushDeviceService {
    private final Logger logger = LoggerFactory.getLogger(PushDeviceServiceImpl.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserService userService;

    private static final String REDIS_PREFIX = "PDS";
    @Autowired
    private WalletDao walletDao;

    @Override
    public List<String> getAvailableDevices(int... walletIds) {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < walletIds.length; i++) {
            String keyPrefix = REDIS_PREFIX + walletIds[i] + "-";
            stringRedisTemplate.keys(keyPrefix + "*")
                               .stream()
                               .forEach(key -> {
                                   result.add(key.replaceFirst(keyPrefix, ""));
                               });
        }
        logger.info("walletIds:{}, devices:{}", walletIds, result);
        return result.stream().collect(Collectors.toList());
    }

    @Override
    public void updateAvailableDevices(String device, List<String> wallets) {
        stringRedisTemplate.delete(stringRedisTemplate.keys(REDIS_PREFIX+"*"+device));
        addAvailableDevices(device, wallets);
    }

    @Override
    public void addAvailableDevices(String device, List<String> wallets) {
        addAvailableDeviceIds(device, walletDao.queryByWallets(wallets));
    }

    @Override
    public void deleteAvailableDevices(String device, List<String> wallets) {
        deleteAvailableDeviceIds(device, walletDao.queryByWallets(wallets));
    }

    @Override
    public void addAvailableDeviceIds(String device, List<Integer> walletIds) {
        if (walletIds == null || walletIds.isEmpty()) {
            return;
        }

        walletIds.forEach(wallet ->
                stringRedisTemplate.opsForValue()
                                   .set(REDIS_PREFIX+wallet+"-"+device, ""));
    }

    @Override
    public void deleteAvailableDeviceIds(String device, List<Integer> walletIds) {
        if (walletIds == null || walletIds.isEmpty()) {
            return;
        }

        walletIds.forEach(wallet ->
                stringRedisTemplate.delete(REDIS_PREFIX+wallet+"-"+device));
    }

    @Override
    public void initAvailableDevices(InitWalletDTO body) {
        Set<String> deviceskeys = stringRedisTemplate.keys(REDIS_PREFIX + "*" + body.getDevice());
        Set<String> haveKeys = new HashSet<>();
        if (body.getWalletHash() != null) {
            List<Integer> walletIds =  walletDao.queryByWallets(body.getWalletHash());
            walletIds.forEach(walletId -> {
                String key = REDIS_PREFIX + walletId + "-" + body.getDevice();
                haveKeys.add(key);
            });
        }
        deviceskeys.removeAll(haveKeys);

        deviceskeys.forEach(key -> {
            stringRedisTemplate.delete(key);
        });
    }
}
