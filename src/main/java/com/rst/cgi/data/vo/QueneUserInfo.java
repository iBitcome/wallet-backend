package com.rst.cgi.data.vo;

import java.util.Date;

/**
 * @author hxl
 * @date 2018/5/30 下午5:43
 */
public class QueneUserInfo {

    //用户ID
    private String email;
    //冻结时间，毫秒数
    private Date frozenTime;

    public QueneUserInfo(){}

    public QueneUserInfo(String email, Date frozenTime) {
        this.email = email;
        this.frozenTime = frozenTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getFrozenTime() {
        return frozenTime;
    }

    public void setFrozenTime(Date frozenTime) {
        this.frozenTime = frozenTime;
    }
}
