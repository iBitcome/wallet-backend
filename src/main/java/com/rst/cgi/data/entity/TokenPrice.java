package com.rst.cgi.data.entity;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by mtb on 2018/4/14.
 */
public class TokenPrice extends Readable{
    @ApiModelProperty(value = "代币名称")
    private String tokenName;
    @ApiModelProperty(value = "代币价格（单位：美元）")
    private Double price;

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
