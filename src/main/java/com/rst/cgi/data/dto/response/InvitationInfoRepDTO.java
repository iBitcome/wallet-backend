package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InvitationInfoRepDTO extends Readable {
    @ApiModelProperty(value = "邀请人的邀请码")
    private String InviterCode;
    @ApiModelProperty(value = "提交地址")
    private String activeAddress;
    @ApiModelProperty(value = "邮箱")
    private String email;
    @ApiModelProperty(value = "手机号码")
    private String phone;
    @ApiModelProperty(value = "人气值")
    private int inviPerson;
}
