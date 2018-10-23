package com.rst.cgi.data.dto.response;

/**
 * 交易对信息封装
 * @author hxl
 * @date 2018/5/24 下午5:40
 */
public class TokenPairDTO {

    private String exchange;//交易所
    private String pair;//交易对，比如"ETH_BTC"
    private double highPrice;//最高价格
    private double lowPrice;//最低价格
    private double lastPrice;//最近一次的价格
    private double volume;//交易量
    //时间，毫秒数
    private long time;


    public TokenPairDTO(String exchange, String pair) {
        this.exchange = exchange;
        this.pair = pair;
    }

    public TokenPairDTO(String exchange, String pair, double lastPrice, long time) {
        this.exchange = exchange;
        this.pair = pair;
        this.lastPrice = lastPrice;
        this.time = time;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
