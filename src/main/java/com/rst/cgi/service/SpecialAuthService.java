package com.rst.cgi.service;

/**
 * Created by hujia on 2017/3/30.
 */
public interface SpecialAuthService {
    String magicToken = "com.rst.magic-token";

    boolean isInnerServer(String token);

    String getToken();
}
