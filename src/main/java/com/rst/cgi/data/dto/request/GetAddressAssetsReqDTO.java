package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class GetAddressAssetsReqDTO extends Readable {
    @ApiModelProperty(value = "钱包公钥Hash列表",required = true)
    private List<String> pubkHashList;
    @ApiModelProperty(value = "公钥Hash所属coinType",required = true)
    private Integer coinType = 0;
    @ApiModelProperty(value = "代币简称",required = true)
    private String tokenName = "BTC";
    @ApiModelProperty("地址类型:0-pkh,1-sh")
    private int type = 0;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<String> getPubkHashList() {
        return pubkHashList;
    }

    public void setPubkHashList(List<String> pubkHashList) {
        this.pubkHashList = pubkHashList;
    }

    public Integer getCoinType() {
        return coinType;
    }

    public void setCoinType(Integer coinType) {
        this.coinType = coinType;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }
}
