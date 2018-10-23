package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ActivityAddressReqDTO extends Readable {
    @ApiModelProperty("地址")
    private String address;
    @ApiModelProperty("地址所属的代币的code")
    private Long tokenCode;
    @ApiModelProperty("活动名称")
    private String activityName;
}
