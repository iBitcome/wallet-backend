package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 保存充值记录请求类
 */
@Data
public class RechargeRecordRepDTO extends Readable {
    @ApiModelProperty(value = "交易hash")
    private String txId;
    @ApiModelProperty(value = "充值金额")
    private String value;
    @ApiModelProperty(value = "代币名称（eg:ETH/BTC/BCH）")
    private String tokenName;
    @ApiModelProperty(value = "充值时间")
    private Long RechargeTime = new Date().getTime();
    @ApiModelProperty(value = "已被确认的块数量")
    private Integer confirmNum;
    @ApiModelProperty(value = "创建时间")
    private Long createTime;

}
