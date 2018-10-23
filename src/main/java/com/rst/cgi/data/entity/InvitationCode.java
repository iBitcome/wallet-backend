package com.rst.cgi.data.entity;

import com.rst.cgi.common.enums.Ignore;

/**
 * Created by hujia on 2017/8/29.
 */
public class InvitationCode {
    @Ignore
    private Integer id;
    private Integer ownerId;
    private String code;
    private Integer status;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
