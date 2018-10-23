package com.rst.cgi.data.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hxl
 * @date 2018/5/28 下午2:18
 */
@Data
public class UserReqDTO {
    @ApiModelProperty("邮箱地址")
    private String email;
    @ApiModelProperty("密码/邮箱验证码/自动登陆token")
    private String token;
    @ApiModelProperty("类型：1-密码，2-邮箱验证码，0-自动登陆")
    private Integer type;
}
