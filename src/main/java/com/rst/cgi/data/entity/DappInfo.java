package com.rst.cgi.data.entity;

import lombok.Data;

import java.util.Date;

/**
* @Description:
* @Author:  mtb
* @Date:  2018/9/18 下午5:23
*/
@Data
public class DappInfo implements Entity {
    private Integer id;
    private Date createTime;
    private Integer createById;
    private Integer isDelete;
    private String cover;//封面连接
    private Integer lang;//0:中文 1:英文
    private String title;
    private String link;//dapp地址
    private Integer sortInfo; //排序
    private Integer inMarket; //是否上架 0：未上架，1：已上架
    private Integer needToPay;
    private String content;//正文内容
}
