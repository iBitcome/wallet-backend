package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class UTXOReqDTO extends Readable {
    @ApiModelProperty(value = "公钥Hash列表")
    private List<UTXOAddress> addressList;
    @ApiModelProperty(value = "需要查询的代码简称（BTC/BCH）")
    private List<String> tokenType;

    public static class UTXOAddress{
        @ApiModelProperty(value = "公钥Hash")
        private String pubkHash;
        @ApiModelProperty(value = "coinType")
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

    public List<UTXOAddress> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<UTXOAddress> addressList) {
        this.addressList = addressList;
    }

    public List<String> getTokenType() {
        return tokenType;
    }

    public void setTokenType(List<String> tokenType) {
        this.tokenType = tokenType;
    }
}
