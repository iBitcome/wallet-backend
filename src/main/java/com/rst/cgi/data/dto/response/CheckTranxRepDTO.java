package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class CheckTranxRepDTO extends Readable {
    @ApiModelProperty(value = "地址列表")
    private List<String> addressList;
    @ApiModelProperty(value = "结果集合")
    private List<Boolean> result;
    @ApiModelProperty(value = "代币简称")
    private String tokenType = "BTC";

    public List<Boolean> getResult() {
        return result;
    }

    public void setResult(List<Boolean> result) {
        this.result = result;
    }

    public List<String> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<String> addressList) {
        this.addressList = addressList;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
