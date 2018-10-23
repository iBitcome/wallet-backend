package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class GetTxFeeReqDTO extends Readable {
    @ApiModelProperty(value = "代币名称列表", required = true)
    private List<String> tokenNames;

    public List<String> getTokenNames() {
        return tokenNames;
    }

    public void setTokenNames(List<String> tokenName) {
        this.tokenNames = tokenName;
    }
}
