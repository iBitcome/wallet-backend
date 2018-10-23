package com.rst.cgi.common.constant;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mtb on 2018/4/9.
 */
public class CommonPage<T>  extends Readable {
    @ApiModelProperty(value = "当前页码")
    private Integer pageNo = 1;
    @ApiModelProperty(value = "页码大小")
    private Integer pageSize = 0;
    @ApiModelProperty(value = "当前页面数据")
    private List<T> content = new ArrayList<>();
    @ApiModelProperty(value = "当前数据的所属时间类型（current：当月，history：历史）")
    private String dataTimeType;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public String getDataTimeType() {
        return dataTimeType;
    }

    public void setDataTimeType(String dataTimeType) {
        this.dataTimeType = dataTimeType;
    }
}
