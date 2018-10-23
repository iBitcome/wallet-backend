package com.rst.cgi.data.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class OperationMessage implements Entity{
    private Integer id;
    private Date createTime;
    private Date updateTime;
    private String title;
    private String cover;
    private String link;
    private String content;
    private Integer category;
    private Date validStartTime;
    private Date validEndTime;
    private Integer isDelete;
    private Integer isPushed;
    private Integer clickCount;
    private Integer messageType;
}
