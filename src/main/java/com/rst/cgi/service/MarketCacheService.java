package com.rst.cgi.service;


import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.dto.SymbolBrief;
import com.rst.cgi.data.dto.response.GetKlineRes;
import com.rst.cgi.data.dto.response.GetSymbolDepthRes;
import com.rst.cgi.data.dto.response.GetTradesRes;
import com.rst.cgi.data.entity.KlinePoint;
import com.rst.cgi.data.entity.TradePoint;

import java.util.List;

/**
 * @author hujia
 */
public interface MarketCacheService {
    interface DataFetcher {
        /**
         * 获取一段时间内的symbol的行情数据
         * @param symbol 交易对
         * @param exchange 交易所
         * @param startTime 起始时间戳ms
         * @param endTime 结束时间戳ms
         * @return 产生的行情数据列表
         */
        List<TradePoint> fetchTradePoints(Symbol symbol, String exchange,
                                          Long startTime, Long endTime);

        /**
         * 获取symbol最新的size条的行情数据
         * @param symbol 交易对
         * @param exchange 交易所
         * @param size 0-最大
         * @return 产生的行情数据列表
         */
        List<TradePoint> fetchTradePoints(Symbol symbol, String exchange, int size);
        /**
         * 获取一段时间内的symbol的K线数据
         * @param symbol 交易对
         * @param exchange 交易所
         * @param kInterval
         * @param startTime 起始时间戳ms
         * @param endTime 结束时间戳ms
         * @return 产生的行情数据列表
         */
        List<KlinePoint> fetchKlinePoints(Symbol symbol, String exchange,
                                          String kInterval, Long startTime, Long endTime);
        /**
         * 产生一段时间内的symbol的深度数据
         * @param symbol 交易对
         * @param exchange 交易所
         * @param limit 交易深度
         * @return 产生的行情数据列表
         */
        GetSymbolDepthRes fetchDepthPoints(Symbol symbol, String exchange, int limit);

        /**
         * 获取交易对信息
         * @param exchange
         * @return
         */
        List<SymbolBrief> fetchSymbolData(String exchange);

        /**
         * 获取交易所的市场配置信息
         * @param exchange
         * @return
         */
        Object fetchConfig(String exchange);
    }

    /**
     * 获取一段时间的交易行情数据（连续时间的相同交易合并）
     * @param symbol 交易对
     * @param exchange 交易所
     * @param startTime 开始时间（时间戳/毫秒）
     * @param endTime 结束时间（时间戳/毫秒）
     * @return 历史成交交易集合 比如[{price,quantity,tradeTime}...]
     */
    GetTradesRes getTradePoints(Symbol symbol, String exchange,
                                Long startTime, Long endTime);

    /**
     * 获取symbol最新的size条的行情数据
     * @param symbol 交易对
     * @param exchange 交易所
     * @return 产生的行情数据列表
     */
    GetTradesRes getTradePoints(Symbol symbol, String exchange);

    /**
     * 获取一段时间的k线数据
     * @param symbol 交易对
     * @param exchange "dex.top"
     * @param kInterval
     * @param startTime
     * @param endTime
     * @return
     */
    GetKlineRes getKlinePoints(Symbol symbol, String exchange,
                               String kInterval, Long startTime, Long endTime);

    /**
     * 获取一定数量的交易深度信息
     * @param symbol
     * @param exchange
     * @return
     */
    GetSymbolDepthRes getDepthPoints(Symbol symbol, String exchange);

    /**
     * 获取交易对信息
     * @param exchange
     * @param baseAsset
     * @param quoteAsset
     * @return
     */
    List<SymbolBrief> getSymbolData(String exchange, String baseAsset, String quoteAsset);

    /**
     * 获取交易所的市场配置信息
     * @param exchange
     * @return
     */
    Object getConfig(String exchange);
}
