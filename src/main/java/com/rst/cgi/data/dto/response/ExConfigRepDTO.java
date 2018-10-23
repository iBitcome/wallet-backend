package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExConfigRepDTO extends Readable{
    @ApiModelProperty(value = "bch多签地址")
    private String bchMultiaddr;
    @ApiModelProperty(value = "btc多签地址")
    private String btcMultiaddr;
    @ApiModelProperty(value = "铸币的手续费")
    private String mintFeeRate;
    @ApiModelProperty(value = "熔币的手续费")
    private String burnFeeRate;
    @ApiModelProperty(value = "bch铸币的最小金额")
    private String minBchMintAmount;
    @ApiModelProperty(value = "btc铸币的最小金额")
    private String minBtcMintAmount;
    @ApiModelProperty(value = "熔币的最小金额")
    private String minBurnAmount;
}
