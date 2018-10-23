package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户信息
 */
@Data
public class UserInfo extends Readable {
    @ApiModelProperty(value = "用户邮箱")
    private String email;
    @ApiModelProperty(value = "登录id")
    private String loginName;
    @ApiModelProperty(value = "昵称")
    private String nickName;
    @ApiModelProperty(value = "电话号码")
    private String phone;
    private String headImgUrl;
    private String channel;
    private Integer hand;
    private Integer fingerPrint;
    private String handWord;
    private Integer handStatus;
    private Integer fingerStatus;
    private Integer handPath;
    @ApiModelProperty(value = "邀请码")
    private String invitationCode;
}
