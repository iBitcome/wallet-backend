package com.rst.cgi.data.entity;

import lombok.Data;

import java.util.Date;

@Data
public class EosAccount implements Entity {
    private int id;
    private String walletAddress;
    private String eosAccount;
    private Date createTime;
}
