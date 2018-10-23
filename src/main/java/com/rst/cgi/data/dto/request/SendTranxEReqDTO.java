package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SendTranxEReqDTO extends Readable  {
    @ApiModelProperty(value = "加密后的交易信息（encoded as hex）", required = true)
    private String transaction;
    @ApiModelProperty(value = "代币名称")
    private String tokenName ;
    @ApiModelProperty(value = "充值金额")
    private String value;
    @ApiModelProperty(value = "合约方法名称")
    private String method;
    @ApiModelProperty(value = "交易发起方的地址")
    private String address;
    @ApiModelProperty(value = "本次交易发起方地址的nonce值")
    private long nonce;
    @ApiModelProperty(value = "兑换信息")
    private ExInfoReqDTO exInfo;
}
