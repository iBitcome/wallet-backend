package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SendtranxBReqDTO extends Readable {
    @ApiModelProperty(value = "加密后的交易信息（encoded as hex）", required = true)
    private String transaction;
    @ApiModelProperty(value = "coinType", required = true)
    private Integer coinType;
    @ApiModelProperty(value = "代币名称")
    private String tokenName;
    @ApiModelProperty(value = "兑换信息")
    private ExInfoReqDTO exInfo;
}
