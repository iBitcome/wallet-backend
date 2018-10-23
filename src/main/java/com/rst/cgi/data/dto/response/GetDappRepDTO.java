package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
* @Description:
* @Author:  mtb
* @Date:  2018/9/18 下午5:21
*/
@Getter
@Setter
public class GetDappRepDTO extends Readable {
    private Integer id;
    @ApiModelProperty("创建时间")
    private String createTime;
    @ApiModelProperty("封面连接")
    private String cover;//封面连接
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("dapp地址")
    private String link;
    @ApiModelProperty("是否需要付款（0：不需要，1：需要）")
    private Integer needToPay;
    @ApiModelProperty("正文内容")
    private String content;
}
