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
public class PageReq extends Readable{
    @ApiModelProperty("当前页码(默认1)")
    private Integer pageNo = 1;
    @ApiModelProperty("每页大小（默认10）")
    private Integer pageSize = 10;
}
