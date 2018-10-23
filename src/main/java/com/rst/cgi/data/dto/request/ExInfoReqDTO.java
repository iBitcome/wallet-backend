package com.rst.cgi.data.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ExInfoReqDTO extends Readable {
    @ApiModelProperty(value = "当前币种")
    private String fromName;
    @ApiModelProperty(value = "目的币种")
    private String toName;
    @ApiModelProperty(value = "出账金额（最小值）")
    private String fromValue;
    @ApiModelProperty(value = "换回金额（最小值）")
    private String toValue;
    @ApiModelProperty(value = "钱包hash")
    private String walletHash;
    @ApiModelProperty(value = "矿工费")
    private String txFee;


    @JsonIgnore
    public static final String BEECOIN_CHANNEL = "beecoin";
    @JsonIgnore
    public static final String OTHER_CHANNEL = "OTHER";

    @JsonIgnore
    public static final Integer SUCCESS = 1;
    @JsonIgnore
    public static final Integer FAIL= -1;
    @JsonIgnore
    public static final Integer EXCHANGE = 0;
}
