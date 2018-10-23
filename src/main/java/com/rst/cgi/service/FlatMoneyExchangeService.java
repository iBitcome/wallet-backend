package com.rst.cgi.service;

import java.util.List;

/**
 * @author hujia
 */
public interface FlatMoneyExchangeService {
    /**
     * 获取base兑换quote的汇率
     * @param base eg: USD
     * @param quote eg: CNY
     * @return 汇率值
     */
    String getRate(String base, String quote);

    /**
     * 获取base兑换支持的币种列表
     * @param base
     * @return
     */
    List<String> getSupportQuotes(String base);
}
