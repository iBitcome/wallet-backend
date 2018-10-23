package com.rst.cgi.data.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Setter
@Getter
public class AppVersionRepDTO extends Readable {
    @ApiModelProperty(value = "版本号")
    private String buildNo;
    @ApiModelProperty(value = "发布时间")
    private String publishedTime;
    @ApiModelProperty(value = "发布内容（中文）")
    private String contentCn;
    @ApiModelProperty(value = "发布内容（英文）")
    private String contentEn;
}
