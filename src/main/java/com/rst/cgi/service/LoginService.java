package com.rst.cgi.service;

import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author hujia
 * @date 2017/3/14
 */
public interface LoginService {
    /**
     * 此方法使用userId直接登录，userId对应的用户存在即可登录成功
     * @param userId 登录的用户id
     * @param request 登录请求
     * @param ignoreSessionStrategy 是否无视session的限制策略
     * @throws AuthenticationException
     */
    void login(Long userId, boolean ignoreSessionStrategy,
               HttpServletRequest request) throws AuthenticationException;

    /**
     * 此方法使用账号密码登录，校验账号、密码、平台的对应关系
     * @param account 登录的用户的账号（手机号）
     * @param password 登录的用户的密码
     * @param passwordType 登录的用户的平台
     * @param request 登录请求
     * @throws AuthenticationException
     */
    void login(String account, String password, int passwordType,
               HttpServletRequest request) throws AuthenticationException;
}
