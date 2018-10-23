package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GetGasLimitReqDto extends Readable {
    @ApiModelProperty("转出方地址")
    private String fromAddress;
    @ApiModelProperty("收账方地址")
    private String toAddress;
    @ApiModelProperty("币种名称")
    private String tokenName = "ETH";
}
