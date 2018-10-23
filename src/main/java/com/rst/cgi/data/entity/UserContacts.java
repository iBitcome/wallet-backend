package com.rst.cgi.data.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.rst.cgi.common.enums.Ignore;
import com.rst.cgi.common.utils.DateUtil;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

/**
 * 用户联系人
 * @author hxl
 * @date 2018/5/29 下午2:58
 */
public class UserContacts implements Entity{

    private Integer id;
    @ApiModelProperty("联系人名称")
    private String contactsName;
    private String contactsPinyin;
    @ApiModelProperty("联系人所属用户ID")
    private Integer userId;
    @JsonFormat(pattern = DateUtil.DATE_TIME_PATTERN)
    private Date createTime;

    @Ignore
    private List<UserContactsAddress> addressList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContactsName() {
        return contactsName;
    }

    public void setContactsName(String contactsName) {
        this.contactsName = contactsName;
    }

    public String getContactsPinyin() {
        return contactsPinyin;
    }

    public void setContactsPinyin(String contactsPinyin) {
        this.contactsPinyin = contactsPinyin;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public List<UserContactsAddress> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<UserContactsAddress> addressList) {
        this.addressList = addressList;
    }

}
