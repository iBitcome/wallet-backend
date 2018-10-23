package com.rst.cgi.data.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class WithdrawReq {
    @ApiModelProperty("与交易所关联的地址")
    private String address;
    @ApiModelProperty("提现的代币简称")
    private String token;
    @ApiModelProperty("提现的数量(最小单位)")
    private String amount;
    @ApiModelProperty("签名数据")
    private String sig;
    @ApiModelProperty("时间")
    private long timestampSec;
}
