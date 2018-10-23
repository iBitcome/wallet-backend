package com.rst.cgi.service.impl;

import com.rst.cgi.common.enums.CoinType;
import com.rst.cgi.common.enums.Money;
import com.rst.cgi.common.utils.StringUtil;
import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.entity.TradePoint;
import com.rst.cgi.service.FlatMoneyExchangeService;
import com.rst.cgi.service.FlatMoneyService;
import com.rst.cgi.service.ThirdService;
import com.rst.cgi.service.exchange.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author hujia
 */
@Service
public class FlatMoneyServiceImpl implements FlatMoneyService {
    @Autowired
    private MarketDataService marketDataService;
    @Autowired
    private FlatMoneyExchangeService flatMoneyExchangeService;
    @Autowired
    private ThirdService thirdService;

    private static final String DEFAULT_EXCHANGE = "Bitcome";

    @Override
    public Double getCurrentPrice(String token, String moneyType) {
        if (CoinType.WHC.getName().equalsIgnoreCase(token)) { //WHC参考价格（根据BCH价格/100）
            token = CoinType.BCH.getName();

            String price = marketDataService.getCurrentPrice(
                    new Symbol(moneyType, token), DEFAULT_EXCHANGE);
            if (StringUtils.isEmpty(price)) {
                return 0.0;
            }

            return Double.parseDouble(price) / 100.0;
        } else {
            String price = marketDataService.getCurrentPrice(
                    new Symbol(moneyType, token), DEFAULT_EXCHANGE);
            if (StringUtils.isEmpty(price)) {
                return 0.0;
            }

            return Double.parseDouble(price);
        }

    }

    @Override
    public List<TradePoint> getAggTrades(
            String token, Long startTime, Long endTime, String moneyType) {
        return marketDataService.getAggTrades(
                new Symbol(moneyType, token), DEFAULT_EXCHANGE, startTime, endTime);
    }

    @Override
    public Double getRate(String srcMoneyType, String destMoneyType) {
        String rate = flatMoneyExchangeService.getRate(srcMoneyType, destMoneyType);
        if (StringUtils.isEmpty(rate)) {
            return 0.0;
        }

        return Double.parseDouble(rate);
    }

    @Override
    public Double tokenPriceAdaptation(String token) {
        //新接口
//        return  this.getCurrentPrice(token, Money.USD.getCode());

        //数据的旧接口
        return thirdService.getUSDByToken(token);
    }
}
