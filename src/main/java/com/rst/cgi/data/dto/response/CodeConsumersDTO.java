package com.rst.cgi.data.dto.response;

import java.io.Serializable;

/**
 * Created by matianbao on 2017/9/21.
 */
public class CodeConsumersDTO implements Serializable{
    private Integer id;
    private String phoneNo;
    private String status;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        if (phoneNo != null) {
            this.phoneNo = phoneNo.replace(phoneNo.substring(3, 7), "****");
        } else {
            this.phoneNo = phoneNo;
        }

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CodeConsumersDTO{" +
                "id=" + id +
                ", phoneNo='" + phoneNo + '\'' +
                ", status='" + status + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
