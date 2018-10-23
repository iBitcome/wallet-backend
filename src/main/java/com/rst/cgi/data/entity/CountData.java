package com.rst.cgi.data.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CountData implements Entity, Serializable {
    private int id;
    private int userId;
    private int childType;
    private Date createTime;
    private int parentType;
    private String channel;
    private String title;
    private int ptype;
}
