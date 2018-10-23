package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class UTXORepDTO extends Readable {

    @ApiModelProperty(value = "代币简称")
    private String tokenType;
    @ApiModelProperty(value = "该代币下的UTXO")
    private List<UTXORep> UTXOS;

    public static class UTXORep {
        @ApiModelProperty(value = "钱包地址")
        private String walletAddress;
        @ApiModelProperty(value = "UTXO")
        private Long utxo;
        @ApiModelProperty(value = "utxo的加密脚本")
        private String voutScript;
        @ApiModelProperty(value = "产出该utxo的上一笔交易hash")
        private String tanxHash;
        @ApiModelProperty(value = "该utxo在上一笔交易的排序")
        private Integer tanxIndex;
        @ApiModelProperty(value = "该UTXO的状态（0:未确认; 1: 未使用 2:使用中）")
        private Integer status;

        public String getTanxHash() {
            return tanxHash;
        }

        public void setTanxHash(String tanxHash) {
            this.tanxHash = tanxHash;
        }

        public Integer getTanxIndex() {
            return tanxIndex;
        }

        public void setTanxIndex(Integer tanxIndex) {
            this.tanxIndex = tanxIndex;
        }

        public String getWalletAddress() {
            return walletAddress;
        }

        public void setWalletAddress(String walletAddress) {
            this.walletAddress = walletAddress;
        }

        public Long getUtxo() {
            return utxo;
        }

        public void setUtxo(Long utxo) {
            this.utxo = utxo;
        }

        public String getVoutScript() {
            return voutScript;
        }

        public void setVoutScript(String voutScript) {
            this.voutScript = voutScript;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public List<UTXORep> getUTXOS() {
        return UTXOS;
    }

    public void setUTXOS(List<UTXORep> UTXOS) {
        this.UTXOS = UTXOS;
    }
}
