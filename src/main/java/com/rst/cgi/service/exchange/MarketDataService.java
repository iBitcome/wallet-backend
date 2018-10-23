package com.rst.cgi.service.exchange;

import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.entity.TradePoint;

import java.util.List;

/**
 * @author hujia
 */
public interface MarketDataService {
    /**
     * 获取支持某个交易对的交易所列表
     * @param symbol 交易对
     * @return 交易所id列表 比如["okex", "liqui", "binance"]
     */
    List<String> getSupportExchange(Symbol symbol);

    /**
     * 获取一段时间的交易行情数据（连续时间的相同交易合并）
     * @param symbol 交易对
     * @param exchange 交易所
     * @param startTime 开始时间（时间戳/毫秒）
     * @param endTime 结束时间（时间戳/毫秒）
     * @return 历史成交交易集合 比如[{price,quantity,tradeTime}...]
     */
    List<TradePoint> getAggTrades(Symbol symbol, String exchange, Long startTime, Long endTime);

    /**
     * 获取某个交易对的当前兑换价格。
     * 需要支持推导，比如ETH-BTC=10，BTC-CNY=20，在没有ETH-CNY的情况下返回10*20=200
     * @param symbol 交易对
     * @param exchange 交易所，为空时搜索所有支持的交易所，找到或推论出第一个结果即返回
     * @return 兑换比例 比如"13.23234"
     */
    String getCurrentPrice(Symbol symbol, String exchange);
}
