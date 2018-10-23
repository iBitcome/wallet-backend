package com.rst.cgi.common;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 分页基础类
 * @author hxl
 * @date 2018/5/29 下午5:08
 */
public class BasePage implements Serializable {

    //分页查询的总记录数的键名称
    public static final String PAGE_TOTAL_KEY = "totalCount";

    @ApiModelProperty("查询关键字")
    private String key;
    @ApiModelProperty("第几页")
    private int page;
    @ApiModelProperty("每页展示的记录数")
    private int pageSize;
    @ApiModelProperty("排序名称")
    private String orderName;
    @ApiModelProperty("排序方式（asc/desc）")
    private String orderWay;

    //排序枚举类
    public static enum DbOrder {
        ASC, DESC;
    }

    public int getPage() {
        return (page < 1) ? 1 : page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return (pageSize < 1) ? 20 : pageSize;//默认为20
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderWay() {
        if (DbOrder.ASC.name().equalsIgnoreCase(orderWay)) {
            return DbOrder.ASC.name();
        }
        return DbOrder.DESC.name();
    }

    public void setOrderWay(String orderWay) {
        this.orderWay = orderWay;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
