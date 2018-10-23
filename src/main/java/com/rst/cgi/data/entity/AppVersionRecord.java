package com.rst.cgi.data.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AppVersionRecord implements Entity{
    private Integer id;
    private Date createTime;
    private Integer createById;
    private String buildNo;
    private Date publishTime;
    private String publisher;
    private Integer isDelete;
    private String contentCn;
    private String contentEn;
    private String channel;
}
