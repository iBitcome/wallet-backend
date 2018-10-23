package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetInvitedListRepDTO extends Readable {
    @ApiModelProperty("邀请时间")
    private String invitedTime;
    @ApiModelProperty("用户编号")
    private String loginName;
}
