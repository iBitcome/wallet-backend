package com.rst.cgi.data.dto;

import com.google.gson.annotations.SerializedName;
import com.rst.cgi.data.dto.response.GetAllTransactionRepDTO;
import com.rst.cgi.data.entity.BlockTransaction;
import com.rst.cgi.data.entity.Token;
import com.rst.cgi.data.entity.VIn;
import com.rst.cgi.data.entity.VOut;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

/**
* @Description:
* @Author:  mtb
* @Date:  2018/9/6 下午1:19
*/
@Data
public class GatewayMessage {
    @SerializedName("from_tx_hash")
    private String fromTxHash;
    @SerializedName("to_tx_hash")
    private String toTxHash;
    @SerializedName("dgw_hash")
    private String dgwHash;
    @SerializedName("from_chain")
    private String fromChain;
    @SerializedName("to_chain")
    private String toChain;
    @SerializedName("time")
    private Long time;
    @SerializedName("block")
    private String blockHash;
    @SerializedName("block_height")
    private String blockHeight;
    @SerializedName("amount")
    private String amount;
    @SerializedName("from_addrs")
    private String fromAddr;
    @SerializedName("to_addrs")
    private String toAddrs;
    @SerializedName("from_fee")
    private String fromFee;
    @SerializedName("dgw_fee")
    private String dgwFee;
    @SerializedName("to_fee")
    private String toFee;
    @SerializedName("token_code")
    private String tokenCode;
    @SerializedName("app_code")
    private String appCode;
    @SerializedName("token_symbol")
    private String tokenSymbol;
    @SerializedName("token_decimals")
    private String tokenDecimals;
    @SerializedName("final_amount")
    private String finalAmount;
    @SerializedName("to_token_symbol")
    private String toTokenSymbol;
    @SerializedName("to_token_decimals")
    private Integer toTokenDecimals;
}

