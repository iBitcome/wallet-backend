package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class CheckTranxReqDTO extends Readable {
    @ApiModelProperty(value = "公钥Hash列表", required = true)
    private List<CheckAddress> addressList;
    @ApiModelProperty(value = "代币简称", required = true)
    private String tokenType = "BTC";

    public static class CheckAddress{
        @ApiModelProperty(value = "公钥Hash", required = true)
        private String pubkHash;
        @ApiModelProperty(value = "cointype", required = true)
        private Integer coinType;
        @ApiModelProperty("地址类型:0-pkh,1-sh")
        private int type = 0;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getPubkHash() {
            return pubkHash;
        }

        public void setPubkHash(String pubkHash) {
            this.pubkHash = pubkHash;
        }

        public Integer getCoinType() {
            return coinType;
        }

        public void setCoinType(Integer coinType) {
            this.coinType = coinType;
        }
    }

    public List<CheckAddress> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<CheckAddress> addressList) {
        this.addressList = addressList;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
