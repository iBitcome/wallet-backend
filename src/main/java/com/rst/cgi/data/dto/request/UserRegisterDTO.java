package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author mtb
 * @date 2018/6/10
 */
public class UserRegisterDTO extends Readable {

    @ApiModelProperty(value = "渠道")
    private String channel;
    @ApiModelProperty(value = "注册方式(邮箱:email,手机号:mobile)")
    private String registerType = "email";
    @ApiModelProperty(value = "邮箱地址")
    private String email;
    @ApiModelProperty(value = "手机号码")
    private String phone;
    @ApiModelProperty(value = "密码",required = true)
    private String password;
    @ApiModelProperty(value = "验证码",required = true)
    private String code;
    @ApiModelProperty("验证码类型（1:邮箱验证码，2：短信验证码，3 不使用验证码）")
    private Integer codeType = 1;
    @ApiModelProperty(value = "邀请码(没有可不传该参数)")
    private String invitationCode;

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getCodeType() {
        return codeType;
    }

    public void setCodeType(Integer codeType) {
        this.codeType = codeType;
    }
}
