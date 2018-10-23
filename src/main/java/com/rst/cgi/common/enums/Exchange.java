package com.rst.cgi.common.enums;

/**
 * 目前接入的交易所定义，注意这里定义的顺序即为遍历交易所的顺序
 * @author huangxiaolin
 * @date 2018-04-20 下午2:47
 */
public enum Exchange {

    binance("币安"),
    huobipro("火币"),
    okex("okex"),
    liqui("liqui"),
    hitbtc("hitbtc"),
    gateio("gateio"),

    end("");

    /**代币交易对分隔符*/
    public static final String PAIR_SEPARATOR = "_";

    private final String exchangeName;

    Exchange(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getExchangeName() {
        return exchangeName;
    }


    public static String okexUrl(String baseUrl) {
        return baseUrl + "/api/v1/ticker.do";
    }

    public static String binanceUrl(String baseUrl) {
        return "";
    }

}
