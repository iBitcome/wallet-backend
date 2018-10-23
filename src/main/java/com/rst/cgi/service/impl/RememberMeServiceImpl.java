package com.rst.cgi.service.impl;

import com.rst.cgi.data.dao.mongo.RememberMeRepository;
import com.rst.cgi.data.entity.RememberMe;
import com.rst.cgi.service.RememberMeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 基于mongo实现登陆记忆
 * @author hujia
 * @date 2017/6/15
 */
@Service
public class RememberMeServiceImpl implements RememberMeService {

    @Autowired
    private RememberMeRepository rememberMeRepository;

    @Override
    public String set(Long userId) {
        String token = UUID.randomUUID().toString();
        RememberMe rememberMe = new RememberMe();
        rememberMe.setToken(token);
        rememberMe.setUserId(userId);
        rememberMe.setRememberTime(System.currentTimeMillis());
        rememberMeRepository.save(rememberMe);
        return token;
    }

    @Override
    public RememberMe get(String token) {
        RememberMe rememberMe = rememberMeRepository.findByToken(token);
        if (rememberMe != null) {
            if (rememberMe.getRememberTime() + REMEMBER_MAX_TIME < System.currentTimeMillis()) {
                rememberMeRepository.delete(rememberMe.getUserId());
            } else {
                return rememberMe;
            }
        }

        return null;
    }
}
