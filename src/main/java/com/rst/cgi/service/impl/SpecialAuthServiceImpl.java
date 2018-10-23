package com.rst.cgi.service.impl;

import com.rst.cgi.service.SpecialAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by hujia on 2017/3/30.
 */
@Service
public class SpecialAuthServiceImpl implements SpecialAuthService {

    @Override
    public boolean isInnerServer(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        if (token.equalsIgnoreCase(magicToken)) {
            return true;
        }

        return token.equalsIgnoreCase(currentToken)
                || token.equalsIgnoreCase(lastToken)
                || token.equalsIgnoreCase(pickToken());
    }

    @Override
    public String getToken() {
        return pickToken();
    }

    /**
     * 以下为inner-server token刷新与管理相关逻辑
     * 同时部署多个cgi-server时只能允许一个server刷新token
     */
    private Boolean enableRefreshToken = null;

    private final Logger logger = LoggerFactory.getLogger(SpecialAuthService.class);

    private String serviceUUID;

    @Value("${special-auth.key:com.rst.server.token}")
    private String tokenId;
    private String lastToken = "";
    private String currentToken = "";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        serviceUUID = UUID.randomUUID().toString();
        tryToBeTokenHost();
    }

    @PreDestroy
    public void destroy() {
        if (enableRefreshToken != null && enableRefreshToken) {
            logger.info("SpecialAuthServiceImpl: Unregister Host[{}]", serviceUUID);
            stringRedisTemplate.delete(tokenHostKey());
        }
    }

    private void tryToBeTokenHost() {
        Boolean tokenHasHost = stringRedisTemplate.hasKey(tokenHostKey());
        if (tokenHasHost == null || !tokenHasHost) {
            logger.info("SpecialAuthServiceImpl: Register Host[{}]", serviceUUID);
            stringRedisTemplate.opsForValue().set(tokenHostKey(), serviceUUID, 65, TimeUnit.MINUTES);
        }
    }

    private boolean isTokenHost() {
        if (enableRefreshToken == null) {
            String hostUUID = stringRedisTemplate.opsForValue().get(tokenHostKey());
            if (hostUUID == null) {
                tryToBeTokenHost();
                hostUUID = stringRedisTemplate.opsForValue().get(tokenHostKey());
            }

            enableRefreshToken = serviceUUID.equalsIgnoreCase(hostUUID);
        }

        return enableRefreshToken;
    }

    private String tokenHostKey() {
        return tokenId + ".host-port";
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void refreshTokenForInnerServer() {
        if (!isTokenHost()) {
            return;
        }
        //刷新一次token, 则可延长host地位65分钟
        stringRedisTemplate.opsForValue().set(tokenHostKey(), serviceUUID, 65, TimeUnit.MINUTES);
        lastToken = currentToken;
        currentToken = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(tokenId, currentToken);
        logger.info("refresh token for inner server.[lastToken:{}|currentToken:{}]",
                lastToken, currentToken);
    }

    private String pickToken() {
        String token = stringRedisTemplate.opsForValue().get(tokenId);
        if (token != currentToken) {
            lastToken = currentToken;
            currentToken = token;
        }
        return currentToken;
    }
}
