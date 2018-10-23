package com.rst.cgi.data.entity;

import lombok.Data;

@Data
public class RechargeRecord implements Entity{
    private Integer id;
    private Integer height;
    private String txId;
    private String value;
    private String tokenName;
    private Integer type;
    private Integer status;
    private Long rechargeTime;
    private Long createTime;
    private Long updateTime;
    private String traderAddress;
    private String channel;

    public static final String RECORD_CHANNEL_BEECOIN = "beecoin";
    public static final String RECORD_CHANNEL_OTHER = "other";
}
