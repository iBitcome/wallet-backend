package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Symbol;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class GetOrderListReq {
    @ApiModelProperty("与交易所绑定的地址")
    private String bindAddress;
    @ApiModelProperty("交易对")
    private Symbol symbol;
    @ApiModelProperty("类型：1-未完成，0-已完成")
    private int type;
    @ApiModelProperty("每页数量")
    private Integer size = 100;
    @ApiModelProperty("页数")
    private Integer page = 0;
    @ApiModelProperty("开始时间戳-秒")
    private Long from;
    @ApiModelProperty("结束时间戳-秒")
    private Long to;

    public static final int TYPE_PAST = 0;
    public static final int TYPE_ACTIVE = 1;
}
