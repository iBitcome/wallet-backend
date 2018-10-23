package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.entity.TradePoint;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class GetTradesRes {
    @ApiModelProperty("交易对")
    private Symbol symbol;
    @ApiModelProperty("分时交易数据")
    private List<TradePoint> tradePoints;

    public GetTradesRes() {}

    public GetTradesRes(Symbol symbol, List<TradePoint> tradePoints) {
        this.symbol = symbol;
        this.tradePoints = tradePoints;
    }
}
