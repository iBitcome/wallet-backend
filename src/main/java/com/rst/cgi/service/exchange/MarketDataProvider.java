package com.rst.cgi.service.exchange;

import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.entity.TradePoint;

import java.util.List;
import java.util.Map;

/**
 * @author hujia
 */
public interface MarketDataProvider {
    /**
     * 获取支持的交易对列表
     * @return
     */
    List<Symbol> getSupportedSymbol();

    /**
     * 获取一段时间的价格以及成交数量数据（连续时间的相同交易合并）
     * 此数据可用来绘制分时走势图
     * @param symbol 交易对
     * @param startTime 开始时间（时间戳/毫秒）
     * @param endTime 结束时间（时间戳/毫秒）
     * @return 历史成交交易集合 比如[{price,quantity,tradeTime}...]
     */
    List<TradePoint> getAggTrades(Symbol symbol, Long startTime, Long endTime);


    /**
     * 获取所有交易对的当前兑换价格
     * @return
     */
    Map<Symbol, String> getCurrentPrice();

    /**
     * 获取某个交易对的兑换价格
     * @param symbol
     * @return
     */
    String getCurrentPrice(Symbol symbol);
}
