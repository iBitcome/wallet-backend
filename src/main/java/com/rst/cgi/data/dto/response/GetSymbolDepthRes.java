package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.DepthPoint;
import com.rst.cgi.data.dto.Symbol;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class GetSymbolDepthRes {
    @ApiModelProperty("交易对")
    private Symbol symbol;
    @ApiModelProperty("数据更新时间")
    private Long timestamp;
    @ApiModelProperty("卖出方")
    private List<DepthPoint> asks;
    @ApiModelProperty("买入方")
    private List<DepthPoint> bids;

    public GetSymbolDepthRes() {}

    public GetSymbolDepthRes(Symbol symbol, Long timestamp,
                             List<DepthPoint> asks, List<DepthPoint> bids) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.asks = asks;
        this.bids = bids;
    }
}
