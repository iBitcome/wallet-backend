package com.rst.cgi.data.entity;

import com.google.gson.annotations.SerializedName;
import com.rst.cgi.data.dto.Readable;
import lombok.Getter;
import lombok.Setter;

/**
* @Description:
* @Author:  mtb
* @Date:  2018/9/6 下午1:12
*/
@Setter
@Getter
public class ExConfig extends Readable{
    @SerializedName("bch_multiaddr")
    private String bchMultiaddr;
    @SerializedName("btc_multiaddr")
    private String btcMultiaddr;
    @SerializedName("mint_fee_rate")
    private String mintFeeRate;
    @SerializedName("burn_fee_rate")
    private String burnFeeRate;
    @SerializedName("min_bch_mint_amount")
    private String minBchMintAmount;
    @SerializedName("min_btc_mint_amount")
    private String minBtcMintAmount;
    @SerializedName("min_burn_amount")
    private String minBurnAmount;

}
