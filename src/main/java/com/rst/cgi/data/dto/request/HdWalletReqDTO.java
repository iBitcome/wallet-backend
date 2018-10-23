package com.rst.cgi.data.dto.request;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created by mtb on 2018/4/12.
 */
public class HdWalletReqDTO {
    @ApiModelProperty(value = "公钥Hash列表", required = true)
    private List<HdWalletReq> walleList;
    @ApiModelProperty(value = "币种列表（不传查询所有币种）")
    private List<String> tokens;

    public static class HdWalletReq {
        @ApiModelProperty(value = "公钥Hash", required = true)
        private String walletAddress;
        @ApiModelProperty(value = "该钱包所持币种的所属链(代币传其所属链)", required = true)
        private Integer coinType;
        @ApiModelProperty("地址类型:0-pkh,1-sh")
        private int type = 0;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getWalletAddress() {
            return walletAddress;
        }

        public void setWalletAddress(String walletAddress) {
            this.walletAddress = walletAddress;
        }

        public Integer getCoinType() {
            return coinType;
        }

        public void setCoinType(Integer coinType) {
            this.coinType = coinType;
        }
    }


    public List<HdWalletReq> getWalleList() {
        return walleList;
    }

    public void setWalleList(List<HdWalletReq> walleList) {
        this.walleList = walleList;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

}
