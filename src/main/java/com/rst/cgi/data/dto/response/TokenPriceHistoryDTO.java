package com.rst.cgi.data.dto.response;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * @author hxl
 * @date 2018/5/18 下午2:37
 */
public class TokenPriceHistoryDTO {

    @ApiModelProperty("代币名称")
    private String tokenName;
    @ApiModelProperty("涨幅")
    private String increase;
    @ApiModelProperty("当前代币价格")
    private double currentTokenPrice;
    @ApiModelProperty("历史数据。tokenPrice:USDT的价格；timeUtc：对应价格的时间毫秒数")
    private List<Map<String, Object>> historyData;
    @ApiModelProperty("交易所")
    private String tradeMarket;

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getIncrease() {
        return increase;
    }

    public void setIncrease(String increase) {
        this.increase = increase;
    }

    public double getCurrentTokenPrice() {
        return currentTokenPrice;
    }

    public void setCurrentTokenPrice(double currentTokenPrice) {
        this.currentTokenPrice = currentTokenPrice;
    }

    public List<Map<String, Object>> getHistoryData() {
        return historyData;
    }

    public void setHistoryData(List<Map<String, Object>> historyData) {
        this.historyData = historyData;
    }

    public String getTradeMarket() {
        return tradeMarket;
    }

    public void setTradeMarket(String tradeMarket) {
        this.tradeMarket = tradeMarket;
    }
}
