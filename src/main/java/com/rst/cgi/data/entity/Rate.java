package com.rst.cgi.data.entity;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by mtb on 2018/4/14.
 */
public class Rate extends Readable{
    @ApiModelProperty(value = "目的币种名称")
    private String name;
    @ApiModelProperty(value = "目的币种的中文/英文名称")
    private String cName;
    @ApiModelProperty(value = "目的币种的符号")
    private String symbol;
    @ApiModelProperty(value = "汇率（美元->目的币种）")
    private Double rate;

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}
