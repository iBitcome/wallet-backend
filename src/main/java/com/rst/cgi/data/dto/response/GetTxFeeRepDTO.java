package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class GetTxFeeRepDTO extends Readable {
    @ApiModelProperty(value = "代币名称")
    private String tokenName;
    @ApiModelProperty(value = "矿工费与时间的映射关系列表")
    private List<TxFeeMap> txFeeMap;
    @ApiModelProperty(value = "最低矿工费")
    private Long low;
    @ApiModelProperty(value = "最高矿工费")
    private Long high;
    public static class TxFeeMap {
        @ApiModelProperty(value = "时间点（单位秒）")
        private Integer latency;
        @ApiModelProperty(value = "矿工费(最小单位)")
        private Long txFee;

        public Integer getLatency() {
            return latency;
        }

        public void setLatency(Integer latency) {
            this.latency = latency;
        }

        public Long getTxFee() {
            return txFee;
        }

        public void setTxFee(Long txFee) {
            this.txFee = txFee;
        }
    }

    public List<TxFeeMap> getTxFeeMap() {
        return txFeeMap;
    }

    public void setTxFeeMap(List<TxFeeMap> txFeeMap) {
        this.txFeeMap = txFeeMap;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public Long getLow() {
        return low;
    }

    public void setLow(Long low) {
        this.low = low;
    }

    public Long getHigh() {
        return high;
    }

    public void setHigh(Long high) {
        this.high = high;
    }
}
