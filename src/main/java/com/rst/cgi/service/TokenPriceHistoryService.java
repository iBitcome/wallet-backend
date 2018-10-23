package com.rst.cgi.service;

import com.rst.cgi.data.entity.TokenPriceHistory;

import java.util.List;

/**
 * @author huangxiaolin
 * @date 2018-05-17 下午5:57
 */
public interface TokenPriceHistoryService {

    void batchInsert(List<TokenPriceHistory> list);

    /**
     * 根据时间查询代币价格
     * @author huangxiaolin
     * @date 2018-05-17 19:50
     */
    List<TokenPriceHistory> findByTokenAndTimeUtc(String token, String timeUtc);
}
