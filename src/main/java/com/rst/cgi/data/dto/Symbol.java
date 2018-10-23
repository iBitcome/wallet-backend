package com.rst.cgi.data.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.Assert;

/**
 * 代币交易对
 * @author hujia
 */
@Data
public class Symbol {
    @ApiModelProperty("交易的购买代币")
    private String baseAsset;
    @ApiModelProperty("交易的商品代币")
    private String quoteAsset;

    public static final String separator = "-";

    public Symbol(String baseAsset, String quoteAsset) {
        Assert.notNull(baseAsset, "NULL baseAsset!");
        Assert.notNull(quoteAsset, "NULL quoteAsset!");
        this.baseAsset = baseAsset.toUpperCase();
        this.quoteAsset = quoteAsset.toUpperCase();
    }

    public void setBaseAsset(String baseAsset) {
        Assert.notNull(baseAsset, "NULL baseAsset!");
        this.baseAsset = baseAsset.toUpperCase();
    }

    public void setQuoteAsset(String quoteAsset) {
        Assert.notNull(quoteAsset, "NULL quoteAsset!");
        this.quoteAsset = quoteAsset.toUpperCase();
    }

    public Symbol() {}

    public String exchangeName(String separator) {
        return baseAsset + separator + quoteAsset;
    }

    public String name() {
        return baseAsset + separator + quoteAsset;
    }

    public Symbol inverse() {
        return new Symbol(quoteAsset, baseAsset);
    }

    @Override
    public String toString() {
        return baseAsset + separator + quoteAsset;
    }

    public static Symbol from(String value) {
        return Symbol.from(value, separator);
    }

    public static Symbol from(String value, String separator) {
        String[] values = value.split(separator);
        if (values.length != 2) {
            return null;
        }

        return new Symbol(values[0], values[1]);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Symbol) {
            Symbol other = (Symbol)obj;
            return baseAsset.equalsIgnoreCase(other.baseAsset)
                    && quoteAsset.equalsIgnoreCase(other.quoteAsset);
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (baseAsset + quoteAsset).hashCode();
    }
}
