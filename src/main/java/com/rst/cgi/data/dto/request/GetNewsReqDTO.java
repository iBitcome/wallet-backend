package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by mtb on 2018/3/30.
 */
public class GetNewsReqDTO extends Readable{
    @ApiModelProperty(value = "页码")
    private Integer page = 1;
    @ApiModelProperty(value = "页码")
    private Integer pageSize = 10;
    @ApiModelProperty(value = "排序（desc/asc）")
    private String orderWay = "desc";
    @ApiModelProperty(value = "新闻id（查询详情时才需要传）")
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderWay() {
        return orderWay;
    }

    public void setOrderWay(String orderWay) {
        this.orderWay = orderWay;
    }
}
