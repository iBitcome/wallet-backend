
package com.rst.cgi.data.dto.request;
import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by mtb.
 */
public class SmsCodeReqDTO extends Readable{
    @ApiModelProperty(value="手机号码",required = true)
    private String phone;
    @ApiModelProperty(value="验证码类型 0-注册，3-绑定手机",required = true)
    private Integer type;

    public static final int REGISTER_TYPE = 0;
    public static final int BIND_TYPE = 3;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}

