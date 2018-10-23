package com.rst.cgi.service.impl;

import com.rst.cgi.common.utils.Web3jClient;
import com.rst.cgi.service.ETHNonceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ETHNonceServiceImpl implements ETHNonceService {
    private final Logger logger = LoggerFactory.getLogger(ETHNonceServiceImpl.class);
    public static final String NONCE_KEY_PREFIX = "nonce.";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private Web3jClient web3jClient;

    private long nonceOnChain;

    @Override
    public void saveNonce(String address, Long nonce, String txid) {
        stringRedisTemplate.opsForHash().put(NONCE_KEY_PREFIX+address, nonce + "", txid);
        stringRedisTemplate.expire(NONCE_KEY_PREFIX+address, 24, TimeUnit.HOURS);
    }

    @Override
    public long nextNonce(String address) {
        long nonceOnChain = getNonceFromChain(address);
        String redisKey = NONCE_KEY_PREFIX + address;

        Map<Object, Object> localData = stringRedisTemplate.opsForHash().entries(redisKey);
        List<Long> localNonces = localData.keySet().stream().map(item -> Long.parseLong((String) item)).sorted().collect(Collectors.toList());
        if (localNonces.isEmpty()) {
            return nonceOnChain;
        }

        long maxNonce = localNonces.get(localNonces.size() - 1) + 1;
        logger.info("localNonces:{}, nonceOnChain:{}, maxNonce:{}",localNonces, nonceOnChain, maxNonce);
        if (nonceOnChain >= maxNonce) {
            return nonceOnChain;
        }

        for (long i = nonceOnChain; i < maxNonce; i++) {
            long nonce = i;
            String sNonce = String.valueOf(nonce);
            if (localData.containsKey(sNonce)) {
                logger.info("find nonce:{}", nonce);
                if (check((String)localData.get(sNonce))) {
                    continue;
                } else {
                    stringRedisTemplate.opsForHash().delete(redisKey, nonce + "");
                    return nonce;
                }
            } else {
                return nonce;
            }
        }

        return maxNonce;
    }

    private boolean check(String txId) {
        boolean isExist = true;
        Web3j web3j = web3jClient.getWeb3j();
        try {
            Optional<Transaction> transactionOptional = web3j.ethGetTransactionByHash(txId).send().getTransaction();
            if (!transactionOptional.isPresent()) {
                isExist = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isExist;
    }

    private long getNonceFromChain(String address) {
        Web3j web3j = web3jClient.getWeb3j();
        BigInteger nonce = BigInteger.valueOf(nonceOnChain);
        try {
            EthGetTransactionCount ethTranxCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send();
            if (ethTranxCount.getError() == null) {
                nonce =ethTranxCount.getTransactionCount();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nonce.longValue();
    }
}
