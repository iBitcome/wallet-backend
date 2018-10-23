package com.rst.cgi.data.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class TokenDexBalance {
    @ApiModelProperty("token的简称")
    private String tokenId;
    @ApiModelProperty("总额")
    private String total;
    @ApiModelProperty("可用总额")
    private String active;
    @ApiModelProperty("交易锁定总额")
    private String locked;
    @ApiModelProperty("提现中总额")
    private String withdrawing;
}
