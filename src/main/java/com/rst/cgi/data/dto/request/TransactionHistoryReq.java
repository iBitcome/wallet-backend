package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.TokenData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class TransactionHistoryReq {

    @ApiModelProperty(value = "代币地址列表，按需填充", required = true)
    private List<TokenData> tokenData;
    @ApiModelProperty(value = "页码, 不传不分页码")
    private int pageNo = -1;
    @ApiModelProperty(value = "页面大小")
    private int pageSize = 10;
    @ApiModelProperty(value = "起始")
    private long beginTime = Long.MIN_VALUE;
    @ApiModelProperty(value = "结束")
    private long endTime = Long.MAX_VALUE;
    @ApiModelProperty(value = "时间区间类型：0-完成时间，1-首次pending时间，2-打包进区块时间")
    private int timeType = 0;
    @ApiModelProperty(value = "交易类型（0查询所有，1:收款，-1:付款）")
    private int txType = 0;

    public static final int TIME_TYPE_CONFIRMED = 0;
    public static final int TIME_TYPE_PENDING = 1;
    public static final int TIME_TYPE_BLOCK = 2;

    public static final int TX_ROLL_IN = 1;
    public static final int TX_ROLL_OUT = -1;
    public static final int TX_ALL = 0;
}
