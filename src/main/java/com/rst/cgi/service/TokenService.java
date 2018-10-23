package com.rst.cgi.service;


import com.rst.cgi.data.entity.Token;


public interface TokenService {

    /**
     * 通过副本token找到原本token
     * @param aliasToken
     */
    Token getOriginalToken(Token aliasToken);
}
