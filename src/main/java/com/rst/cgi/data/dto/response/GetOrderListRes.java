package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.entity.TradeOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class GetOrderListRes {
    @ApiModelProperty("页码")
    private Integer page;
    @ApiModelProperty("总数")
    private Integer total;
    @ApiModelProperty("返回的订单详情列表")
    private List<TradeOrder> orders;
}
