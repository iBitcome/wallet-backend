package com.rst.cgi.service.exchange.impl;

import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.entity.TradePoint;
import com.rst.cgi.service.exchange.MarketProvider;

import java.util.List;
import java.util.Set;

@MarketProvider(value = "BitcomeDataProvider", market = "Bitcome")
public class BitcomeDataProvider extends AbstractMarketDataProvider {
    @Override
    protected String getExchangeName() {
        return "Bitcome";
    }

    @Override
    public String fetchPrice(Symbol symbol) {

        return null;
    }

    @Override
    public Set<Symbol> fetchSymbols() {
        return null;
    }

    @Override
    public List<TradePoint> fetchTrades(Symbol symbol, Long startTime, Long endTime) {
        return null;
    }
}
