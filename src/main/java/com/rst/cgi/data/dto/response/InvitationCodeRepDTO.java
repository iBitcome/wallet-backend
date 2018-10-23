package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InvitationCodeRepDTO extends Readable {
    @ApiModelProperty(value = "邀请码")
    private String invitationCode;
    @ApiModelProperty(value = "用户编号")
    private String userNo;
}
