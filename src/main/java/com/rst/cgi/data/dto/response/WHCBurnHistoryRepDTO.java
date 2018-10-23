package com.rst.cgi.data.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class WHCBurnHistoryRepDTO extends Readable {
    @ApiModelProperty("交易id")
    private String txId;
    @ApiModelProperty("生成数量(成功时返回)")
    private String getTokenNum;
    @ApiModelProperty("燃烧数量(失败时返回)")
    private String burnTokenNum;
    @ApiModelProperty("确认块数量")
    private Long confirmNum;
    @ApiModelProperty("期望确认的块数量")
    private Long expectConfirmNum;
    @ApiModelProperty("钱包地址")
    private String address;
    @ApiModelProperty("代币名称")
    private String tokenName;
    @ApiModelProperty("代币进制")
    private Integer decimal;
    @ApiModelProperty("区块时间")
    private String blockTime;
    @ApiModelProperty("交易状态（success:1, burning:0, fail:-1）")
    private Integer success;
    @ApiModelProperty("交易失败原因")
    private String failReason;


    public static final Integer SUCCESS = 1;
    public static final Integer BURNING = 0;
    public static final Integer FAIL = -1;
}
