package com.rst.cgi.data.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rst.cgi.common.enums.Ignore;
import com.rst.cgi.common.utils.DateUtil;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * 联系人地址
 * @author hxl
 * @date 2018/5/29 下午3:01
 */
public class UserContactsAddress implements Entity {

    @Ignore
    private Integer id;
    @ApiModelProperty("代币")
    private String token;
    @ApiModelProperty("地址")
    private String address;
    @ApiModelProperty("联系人ID")
    private Integer contactsId;
    @JsonFormat(pattern = DateUtil.DATE_TIME_PATTERN)
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getContactsId() {
        return contactsId;
    }

    public void setContactsId(Integer contactsId) {
        this.contactsId = contactsId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
