package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.entity.KlinePoint;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

/**
 * @author hujia
 */
@Data
public class GetKlineRes {
    @ApiModelProperty("交易对")
    private Symbol symbol;
    @ApiModelProperty("kline数据")
    private Set<KlinePoint> klinePoints;

    public GetKlineRes() {}

    public GetKlineRes(Symbol symbol, Set<KlinePoint> klinePoints) {
        this.symbol = symbol;
        this.klinePoints = klinePoints;
    }
}
