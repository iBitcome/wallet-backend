package com.rst.cgi.data.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
* @Description:
* @Author:  mtb
* @Date:  2018/9/7 下午5:27
*/
@Setter
@Getter
public class PageRep<T> extends Readable{
    @ApiModelProperty("当前页码")
    private Integer pageNo;
    @ApiModelProperty("总页码")
    private Integer pageTotal;
    @ApiModelProperty("总数据量")
    private Long total;
    @ApiModelProperty("内容")
    private List<T> content;
}
