package com.rst.cgi.service.impl;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mongo.TokenRepository;
import com.rst.cgi.data.dao.mysql.CommonDao;
import com.rst.cgi.data.dao.mysql.TokenPriceHistoryDao;
import com.rst.cgi.data.entity.Token;
import com.rst.cgi.data.entity.TokenPriceHistory;
import com.rst.cgi.service.TokenPriceHistoryService;
import com.rst.cgi.service.TokenService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author huangxiaolin
 * @date 2018-05-17 下午5:57
 */
@Service
@Transactional
public class TokenPriceHistoryServiceImpl implements TokenPriceHistoryService {

    @Autowired
    private TokenPriceHistoryDao tokenPriceHistoryDao;

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private TokenService tokenService;

    @Override
    public void batchInsert(List<TokenPriceHistory> list) {
        if (!CollectionUtils.isEmpty(list)) {
            commonDao.batchInsert(list, TokenPriceHistory.class);
        }
    }

    @Override
    public List<TokenPriceHistory> findByTokenAndTimeUtc(String token, String timeUtc) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(timeUtc)) {
            CustomException.response(Error.REQUEST_PARAM_INVALID);
        }

        Token currentToken = tokenRepository.findByName(token);
        if (currentToken.getAliasCode() != null) {
            token = tokenService.getOriginalToken(currentToken).getName();
        }

        return tokenPriceHistoryDao.findByTokenAndTimeUtc(token, timeUtc);
    }
}
