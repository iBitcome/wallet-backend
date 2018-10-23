package com.rst.cgi.data.entity;

import com.rst.cgi.data.dao.mysql.sql.Ignore;

import java.util.Date;

/**
 * 代币价格历史
 * @author huangxiaolin
 * @date 2018-05-17 下午5:25
 */
public class TokenPriceHistory implements Entity {

    @Ignore
    private Integer id;
    private String tokenFrom;
    private String tokenTo;
    private double tokenPrice;
    private String tradeMarket;//交易市场
    private Date timeUtc;//获取价格对应的时间，utc格式
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTokenFrom() {
        return tokenFrom;
    }

    public void setTokenFrom(String tokenFrom) {
        this.tokenFrom = tokenFrom;
    }

    public String getTokenTo() {
        return tokenTo;
    }

    public void setTokenTo(String tokenTo) {
        this.tokenTo = tokenTo;
    }

    public double getTokenPrice() {
        return tokenPrice;
    }

    public void setTokenPrice(double tokenPrice) {
        this.tokenPrice = tokenPrice;
    }

    public String getTradeMarket() {
        return tradeMarket;
    }

    public void setTradeMarket(String tradeMarket) {
        this.tradeMarket = tradeMarket;
    }

    public Date getTimeUtc() {
        return timeUtc;
    }

    public void setTimeUtc(Date timeUtc) {
        this.timeUtc = timeUtc;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
