package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Symbol;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class PlaceOrderReq {
    @ApiModelProperty("交易对")
    private Symbol symbol;
    @ApiModelProperty("交易价格")
    private String price;
    @ApiModelProperty("交易动作：0-买入，1-卖出")
    private int action;
    @ApiModelProperty("交易数量")
    private String amount;
    @ApiModelProperty("交易到期时间戳")
    private Long expireTimeSec;
    @ApiModelProperty("签名:私钥签名（+表示连接）：keccak256(Ethereum Signed Message:\\n70DEx2 Order: tradeAddr+nonce(64)" +
            "+expireTimeSec(64)+amountE8(64)+priceE8(64)+0x00(8)+action(8)+pairId(32))")
    private String sig;
    @ApiModelProperty("交易Nonce")
    private Long nonce;
    @ApiModelProperty("交易钱包地址")
    private String tradeAddr;
}
