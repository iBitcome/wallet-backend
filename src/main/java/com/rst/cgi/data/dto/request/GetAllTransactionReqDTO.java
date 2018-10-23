package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import org.web3j.crypto.Keys;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mtb on 2018/3/30.
 */
public class GetAllTransactionReqDTO extends Readable{
    @ApiModelProperty(value = "公钥Hash列表",required = true)
    private List<TranxAddress> walletAddressList;
    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;
    @ApiModelProperty(value = "页面大小")
    private Integer pageSize = 10;
    @ApiModelProperty(value = "交易时段（current:当月， history:历史(查询以太坊链币种需要此参数)")
    private String timeType = "current";
    @ApiModelProperty(value = "交易类型（不传时：查询所有，1:收款，-1:付款）")
    private Integer transType;
    @ApiModelProperty(value = "需要查询的代币名称", required = true)
    private String tokenType = "ETH";

    public  class TranxAddress{
        @ApiModelProperty(value = "公钥Hash",required = true)
        private String pubkHash;
        @ApiModelProperty(value = "coinType",required = true)
        private Integer coinType;

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

    public Integer getTransType() {
        return transType;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setTransType(Integer transType) {
        this.transType = transType;
    }

    public String getTimeType() {
        return timeType;
    }

    public void setTimeType(String timeType) {
        this.timeType = timeType;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

//    public List<String> getWalletAddressList() {
//        List<String> newList = new ArrayList<>();
//         walletAddressList.forEach(address -> {
//            newList.add(Keys.toChecksumAddress(address));
//        });
//         return newList;
//    }


    public List<TranxAddress> getWalletAddressList() {
        return walletAddressList;
    }

    public void setWalletAddressList(List<TranxAddress> walletAddressList) {
        this.walletAddressList = walletAddressList;
    }
}
