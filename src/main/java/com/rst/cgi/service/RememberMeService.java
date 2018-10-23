package com.rst.cgi.service;

import com.rst.cgi.data.entity.RememberMe;

/**
 *
 * @author hujia
 * @date 2017/6/15
 */
public interface RememberMeService {
    long REMEMBER_MAX_TIME = 72 * 60 * 60 * 1000;
    String KEY_NAME = "x-auto-auth-token";

    /**
     * 设置指定用户的登陆记忆信息
     * @param userId
     * @return auto login token
     */
    String set(Long userId);

    /**
     * 根据token获取登陆记忆信息
     * @param token for auto login
     * @return
     */
    RememberMe get(String token);
}
