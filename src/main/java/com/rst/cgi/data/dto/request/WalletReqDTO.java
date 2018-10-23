package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import org.web3j.crypto.Keys;

import java.util.List;

/**
 * Created by mtb on 2018/4/12.
 */
public class WalletReqDTO extends Readable{
    @ApiModelProperty(value = "公钥Hash列表", required = true)
    private List<String> walletAddress;
    @ApiModelProperty(value = "币种列表（不传查询所有币种）")
    private List<String> tokens;

    public WalletReqDTO() {
    }

    public WalletReqDTO(List<String> walletAddress, List<String> tokens) {
        this.walletAddress = walletAddress;
        this.tokens = tokens;
    }


    public List<String> getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(List<String> walletAddress) {
        this.walletAddress = walletAddress;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

}
