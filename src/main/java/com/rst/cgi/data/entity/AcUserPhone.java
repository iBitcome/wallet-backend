package com.rst.cgi.data.entity;

import lombok.Data;

import java.util.Date;

@Data
public class AcUserPhone implements Entity {
    private int id;
    private String phone;
    private int ptype;
    private Date createTime;
    private String sharePhone;
}
