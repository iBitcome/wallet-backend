package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GetTransactionByIdReqDTO extends Readable {
    @ApiModelProperty(value = "交易id",required = true)
    private String txId;
    @ApiModelProperty(value = "代币所属链（btc：0；bch：145：以太坊链：60）",required = true)
    private Integer coinType;
    @ApiModelProperty(value = "代币名称")
    private String  tokenName;

}
