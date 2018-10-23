package com.rst.cgi.service.impl;

import com.rst.cgi.data.dao.mongo.TokenRepository;
import com.rst.cgi.data.entity.Token;
import com.rst.cgi.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    private TokenRepository tokenRepository;

    @Override
    public Token getOriginalToken(Token aliasToken) {
        if (aliasToken != null && aliasToken.getAliasCode() != null) {
            return tokenRepository.findByTokenCode(aliasToken.getAliasCode());
        }
        return null;
    }
}
