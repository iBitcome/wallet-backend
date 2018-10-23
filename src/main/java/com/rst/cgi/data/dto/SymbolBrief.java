package com.rst.cgi.data.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author hujia
 */
@Data
public class SymbolBrief {
    @ApiModelProperty("交易对")
    private Symbol symbol;
    @ApiModelProperty("交易对的唯一数字标识")
    private Long symbolId;
    @ApiModelProperty("开盘价")
    private String openPrice;
    @ApiModelProperty("最后成交价")
    private String lastPrice;
    @ApiModelProperty("24小时成交量")
    private String volume24;
    @ApiModelProperty("24小时价格变化量")
    private String change24;
    @ApiModelProperty("24小时价格变化百分比")
    private String changePercent24;
    @ApiModelProperty("24小时最高价")
    private String high24;
    @ApiModelProperty("24小时最低价")
    private String low24;

    private double minPlaceOrderValue;

    public static final String separator = "&";

    public String toRedisString() {
        return symbol.name() + separator + symbolId + separator + openPrice + separator +
                lastPrice + separator + volume24 + separator + change24 + separator +
                changePercent24 + separator + high24 + separator + low24;
    }

    public static SymbolBrief fromRedisString(String redisString) {
        if (StringUtils.isEmpty(redisString)) {
            return null;
        }

        String[] values = redisString.split(separator);
        if (values.length != 9) {
            return null;
        }

        SymbolBrief symbolBrief = new SymbolBrief();
        int index = 0;
        symbolBrief.setSymbol(Symbol.from(values[index++]));
        symbolBrief.setSymbolId(Long.parseLong(values[index++]));
        symbolBrief.setOpenPrice(values[index++]);
        symbolBrief.setLastPrice(values[index++]);
        symbolBrief.setVolume24(values[index++]);
        symbolBrief.setChange24(values[index++]);
        symbolBrief.setChangePercent24(values[index++]);
        symbolBrief.setHigh24(values[index++]);
        symbolBrief.setLow24(values[index++]);

        return symbolBrief;
    }
}
