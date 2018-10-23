package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GetExRecordRepDTO extends Readable {
    @ApiModelProperty("发起链交易hash")
    private String fromHash;
    @ApiModelProperty("发起链币种")
    private String fromToken;
    @ApiModelProperty("发起币种的进制")
    private Integer fromTokenDecimal;
    @ApiModelProperty("目的链币种")
    private String toToken;
    @ApiModelProperty("接受币种的进制")
    private Integer toTokenDecimal;
    @ApiModelProperty("发起转出链金额")
    private String fromValue;
    @ApiModelProperty("目的接受链金额")
    private String toValue;
    private String createTime;
    @ApiModelProperty("矿工费")
    private String txFee;
    @ApiModelProperty("1:成功,0:兑换中,-1:失败")
    private Integer status;
    @ApiModelProperty("兑换渠道（beecoin/other）")
    private String channel;
}
