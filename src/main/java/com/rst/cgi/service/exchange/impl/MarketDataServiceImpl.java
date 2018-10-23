package com.rst.cgi.service.exchange.impl;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.entity.TradePoint;
import com.rst.cgi.service.exchange.MarketDataProvider;
import com.rst.cgi.service.exchange.MarketDataService;
import com.rst.cgi.service.exchange.MarketProvider;
import com.rst.thrift.tools.ClassScaner;
import com.rst.thrift.tools.SpringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Service
public class MarketDataServiceImpl implements MarketDataService {

    private Map<String, MarketDataProvider> exchangeToProvider;

    private final Map<String, MarketDataProvider> exchangeToProvider() {
        if (exchangeToProvider == null || exchangeToProvider.isEmpty()) {
            init();
        }

        return exchangeToProvider;
    }

    public void init() {
        Set<Class> classSet = ClassScaner.scan(MarketProvider.packageName, MarketProvider.class);
        exchangeToProvider = new HashMap<>(classSet.size());
        classSet.forEach(aClass -> {
            if (aClass.isAnnotationPresent(MarketProvider.class)) {
                MarketProvider marketProvider = (MarketProvider) aClass.getAnnotation(MarketProvider.class);
                exchangeToProvider.put(marketProvider.market(),
                        SpringUtil.getBean(marketProvider.value(), MarketDataProvider.class));
            }
        });
    }

    @Override
    public List<String> getSupportExchange(Symbol symbol) {
        return exchangeToProvider().entrySet().stream()
                          .filter(entry -> entry.getValue().getSupportedSymbol().contains(symbol))
                          .map(entry -> entry.getKey()).collect(Collectors.toList());
    }

    @Override
    public List<TradePoint> getAggTrades(Symbol symbol, String exchange, Long startTime, Long endTime) {
        return getProvider(exchange).getAggTrades(symbol, startTime, endTime);
    }

    @Override
    public String getCurrentPrice(Symbol symbol, String exchange) {
        if (StringUtils.isEmpty(exchange)) {
            for (MarketDataProvider provider : exchangeToProvider().values()) {
                String price = provider.getCurrentPrice(symbol);
                if (!StringUtils.isEmpty(price)) {
                    return price;
                }
            }
        } else {
            return getProvider(exchange).getCurrentPrice(symbol);
        }

        return null;
    }

    private MarketDataProvider getProvider(String exchange) {
        MarketDataProvider provider = exchangeToProvider().get(exchange);
        if (provider == null) {
            CustomException.response(Error.EXCHANGE_NOT_EXIST);
        }

        return provider;
    }
}
