package com.rst.cgi.service.impl;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.conf.ExchangeConfig;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.dto.SymbolBrief;
import com.rst.cgi.data.dto.response.GetSymbolDepthRes;
import com.rst.cgi.data.entity.KlinePoint;
import com.rst.cgi.data.entity.TradePoint;
import com.rst.cgi.service.MarketCacheService;
import com.rst.thrift.tools.SpringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hujia
 */
public class CompositeDataFetcher implements MarketCacheService.DataFetcher {
    private Map<String, MarketCacheService.DataFetcher> delegates;

    private void init() {
        delegates = new HashMap<>(1);
        delegates.put(ExchangeConfig.DEX_TOP.getName(),
                SpringUtil.getBean("DexDotTopFetcher", MarketCacheService.DataFetcher.class));
    }

    @Override
    public List<TradePoint> fetchTradePoints(Symbol symbol, String exchange, Long startTime, Long endTime) {
        return dataFetcher(exchange).fetchTradePoints(symbol, exchange, startTime, endTime);
    }

    @Override
    public List<TradePoint> fetchTradePoints(Symbol symbol, String exchange, int size) {
        return dataFetcher(exchange).fetchTradePoints(symbol, exchange, size);
    }

    @Override
    public List<KlinePoint> fetchKlinePoints(Symbol symbol, String exchange,
                                             String kInterval, Long startTime, Long endTime) {
        return dataFetcher(exchange).fetchKlinePoints(symbol, exchange,
                kInterval, startTime, endTime);
    }

    @Override
    public GetSymbolDepthRes fetchDepthPoints(Symbol symbol, String exchange, int limit) {
        return dataFetcher(exchange).fetchDepthPoints(symbol, exchange, limit);
    }

    @Override
    public List<SymbolBrief> fetchSymbolData(String exchange) {
        return dataFetcher(exchange).fetchSymbolData(exchange);
    }

    @Override
    public Object fetchConfig(String exchange) {
        return dataFetcher(exchange).fetchConfig(exchange);
    }

    private MarketCacheService.DataFetcher dataFetcher(String exchange) {
        if (delegates == null) {
            init();
        }

        MarketCacheService.DataFetcher dataFetcher = delegates.get(exchange);
        if (dataFetcher == null) {
            CustomException.response(Error.EXCHANGE_NOT_EXIST);
        }
        return dataFetcher;
    }
}
