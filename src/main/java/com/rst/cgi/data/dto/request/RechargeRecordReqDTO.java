package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 保存充值记录请求类
 */
@Data
public class RechargeRecordReqDTO extends Readable {
    @ApiModelProperty(value = "交易hash",required = true)
    private String txId;
    @ApiModelProperty(value = "充值金额",required = true)
    private String value;
    @ApiModelProperty(value = "代币名称（eg:ETH/BTC/BCH）",required = true)
    private String tokenName;
    @ApiModelProperty(value = "充值时间",required = true)
    private Long RechargeTime = new Date().getTime();

}
