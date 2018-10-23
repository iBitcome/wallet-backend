package com.rst.cgi.service.exchange.impl;

import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.entity.TradePoint;
import com.rst.cgi.service.exchange.MarketDataProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hujia
 */
public abstract class AbstractMarketDataProvider implements MarketDataProvider,
        SymbolPriceCache.SymbolPriceMaker, TradeDataCache.TradeDataMaker {

    @Autowired
    private TradeDataCache tradeDataCache;
    @Autowired
    private SymbolPriceCache symbolPriceCache;

    @Override
    public List<Symbol> getSupportedSymbol() {
        return symbolPriceCache.getSymbols();
    }

    @Override
    public List<TradePoint> getAggTrades(Symbol symbol, Long startTime, Long endTime) {
        return tradeDataCache.getTrades(symbol, startTime, endTime);
    }

    @Override
    public Map<Symbol, String> getCurrentPrice() {
        return symbolPriceCache.currentAllPrice();
    }

    @Override
    public String getCurrentPrice(Symbol symbol) {
        return symbolPriceCache.getPrice(symbol);
    }

//    @PostConstruct
    public void init() {
        Set<Symbol> symbols = fetchSymbols();
        symbolPriceCache.init(symbols,this);
        tradeDataCache.init(getExchangeName(), symbols, this);
    }

    /**
     * 数据源名称
     * @return
     */
    protected abstract String getExchangeName();
}
