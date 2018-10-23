package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Symbol;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class CancelOrderReq {
    @ApiModelProperty("与交易所绑定的地址")
    private String bindAddress;
    @ApiModelProperty("交易对")
    private Symbol symbol;
    @ApiModelProperty("订单id，可选，没有时取消所有")
    private String orderId;
}
