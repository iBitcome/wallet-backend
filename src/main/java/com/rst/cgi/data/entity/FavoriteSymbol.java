package com.rst.cgi.data.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author hujia
 */
@Data
public class FavoriteSymbol implements Entity {
    private Integer id;
    private Integer userId;
    private Integer status;
    private String exchange;
    private String baseAsset;
    private String quoteAsset;
    private Date createDate;
}
