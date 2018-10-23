package com.rst.cgi.data.entity;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ExTxInfo extends Readable implements Entity {
    private Long id;
    private String fromTxHash;
    private String toTxHash;
    private String dgwHash;
    private String fromChain;
    private String toChain;
    private Long time;
    private String block;
    private Long blockHeight;
    private String amount;
    private String fromAddrs;
    private String toAddrs;
    private String fromFee;
    private String dgwFee;
    private String toFee;
    private Integer tokenCode;
    private Integer appCode;
    private String tokenSymbol;
    private Integer tokenDecimals;
    private String finalAmount;
    private String toTokenSymbol;
    private Integer toTokenDecimals;

    @Override
    public String table() {
        return "dgateway_tx";
    }
}
