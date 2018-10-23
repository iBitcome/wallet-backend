
package com.rst.cgi.data.dto.request;
import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by mtb.
 */
public class MailCodeReqDTO extends Readable{
    @ApiModelProperty(value="邮箱",required = true)
    private String email;
    @ApiModelProperty(value="验证码类型 0-注册 1-登录 2-忘记密码 3-绑定邮箱(默认0)",required = true)
    private Integer type = 0;

    public static final int REGISTER_TYPE = 0;
    public static final int LOGIN_TYPE = 1;
    public static final int RESET_PWD_TYPE = 2;
    public static final int BIND_EMAIL_TYPE = 3;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}

