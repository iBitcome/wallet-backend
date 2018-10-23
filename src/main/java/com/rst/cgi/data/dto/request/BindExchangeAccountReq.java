package com.rst.cgi.data.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class BindExchangeAccountReq {
    @ApiModelProperty("登陆账号")
    private String email;
    @ApiModelProperty("交易所账号")
    private String exchangeAccount;
    @ApiModelProperty("交易所账号的密码")
    private String exchangePassword;
    @ApiModelProperty("交易所账号的绑定的交易地址")
    private String addressBindWithExchange;
    @ApiModelProperty("ETH")
    private String baseToken = "ETH";
}
