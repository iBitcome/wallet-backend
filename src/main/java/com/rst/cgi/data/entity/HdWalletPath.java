package com.rst.cgi.data.entity;

import lombok.Data;

@Data
public class HdWalletPath implements Entity{
    private Integer id;
    private Integer depth;
    private Integer walletId;
    private String path;
    private String token;
}