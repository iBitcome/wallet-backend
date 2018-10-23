package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import com.rst.cgi.data.entity.Rate;
import com.rst.cgi.data.entity.TokenPrice;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created by mtb on 2018/4/2.
 */
public class GetPriceAndRateRepDTO extends Readable{
    @ApiModelProperty(value = "代币价格")
    List<TokenPrice> tokenPrices;
    @ApiModelProperty(value = "当前所有汇率")
    List<Rate> rates;

    public List<TokenPrice> getTokenPrices() {
        return tokenPrices;
    }

    public void setTokenPrices(List<TokenPrice> tokenPrices) {
        this.tokenPrices = tokenPrices;
    }

    public List<Rate> getRates() {
        return rates;
    }

    public void setRates(List<Rate> rates) {
        this.rates = rates;
    }
}
