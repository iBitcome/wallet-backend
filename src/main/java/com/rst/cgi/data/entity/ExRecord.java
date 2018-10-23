package com.rst.cgi.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

/**
* @Description:
* @Author:  mtb
* @Date:  2018/9/6 上午10:00
*/
@Getter
@Setter
public class ExRecord implements Entity{
    private Integer id;
    private String fromHash;
    private String fromName;
    private String toName;
    private String fromValue;
    private String toValue;
    private String walletHash;
    private Date createTime;
    private String txFee;
    private Integer status;//1:成功,0:兑换中,-1:失败
    private String channel;//兑换渠道（beecoin/other）
}
