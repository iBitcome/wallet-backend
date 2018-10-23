package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Getter
@Setter
public class GetExInfoRepDTO extends Readable {
    @ApiModelProperty("交易块高度")
    private Long txHeight;
    @ApiModelProperty("确认数")
    private Long confirmNum;
    @ApiModelProperty("发起时间")
    private String sndTime;
    @ApiModelProperty("手续费")
    private String serviceFee;
    @ApiModelProperty("网关交易hash")
    private String gateHash;
    @ApiModelProperty("源链交易信息")
    private TxInfo fromTx;
    @ApiModelProperty("目的交易信息")
    private TxInfo toTx;


    @Data
    public static class TxInfo {
        @ApiModelProperty("链的名称")
        private String chain;
        @ApiModelProperty("源链交易hash")
        private String hash;
        @ApiModelProperty("发起币种")
        private String tokeName;
        @ApiModelProperty("发起金额")
        private String value;
        @ApiModelProperty("发起币种的进制")
        private Integer tokenDecimal;
        @ApiModelProperty("地址")
        private List<String> address;
        @ApiModelProperty("矿工费")
        private String txFee;
        @ApiModelProperty("三方网页地址")
        private String webUrl;
    }


}
