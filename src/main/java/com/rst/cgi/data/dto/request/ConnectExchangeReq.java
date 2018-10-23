package com.rst.cgi.data.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class ConnectExchangeReq {
    @ApiModelProperty("交易地址")
    private String traderAddr;
    @ApiModelProperty("当前时间戳，秒")
    private long timestampSec;
    @ApiModelProperty("请求签名")
    private String sig;
}
