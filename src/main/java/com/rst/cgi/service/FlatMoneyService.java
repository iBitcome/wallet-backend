package com.rst.cgi.service;

import com.rst.cgi.data.entity.TradePoint;

import java.util.List;

/**
 * @author hujia
 */
public interface FlatMoneyService {
    /**
     * 获取一个token当前时刻的法币价格
     * @param token eg:ETH,BTC,EOS
     * @param moneyType eg:USD,CNY
     * @return
     */
    Double getCurrentPrice(String token, String moneyType);

    /**
     * 获取一个token的法币价格趋势数据
     * @param token eg:ETH,BTC,EOS
     * @param startTime
     * @param endTime
     * @param moneyType eg:"USD","CNY"
     * @return
     */
    List<TradePoint> getAggTrades(
            String token, Long startTime, Long endTime, String moneyType);

    /**
     * 获取汇率
     * @param srcMoneyType
     * @param destMoneyType
     * @return
     */
    Double getRate(String srcMoneyType, String destMoneyType);


    /**
     * 获取一个token当前时刻的美元价格
     * @param token
     * @return
     */
    Double tokenPriceAdaptation(String token);
}
