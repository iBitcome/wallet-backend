package com.rst.cgi.data.entity;

import com.rst.cgi.data.dao.mysql.sql.Ignore;
import lombok.Data;

import java.util.Date;

/**
 * 对应一个钱包地址
 * @author hujia
 */
@Data
public class WalletAddress implements Entity {
    private Integer id;
    private Integer walletId;
    private String equipmentNo;
    private String walletAddress;
    private Date creatTime;
    private Date updateTime;
    private Integer statusCode;
    private Integer type;
    private String token;
    private Integer syncStatus;
    private Long ethNonce;
    private String hdPath;
    private String balance;


    @Ignore
    public static final int STATUS_SYNCHRONIZED = 1;
    @Ignore
    public static final int STATUS_UN_SYNCHRONIZED = 0;
}