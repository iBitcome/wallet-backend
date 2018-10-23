package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class GetBlockHeightReqDTO extends Readable {
    @ApiModelProperty(value = "coinType", required = true)
    private Integer coinType;

    public GetBlockHeightReqDTO(Integer coinType) {
        this.coinType = coinType;
    }

    public GetBlockHeightReqDTO() {
    }

    public Integer getCoinType() {
        return coinType;
    }

    public void setCoinType(Integer coinType) {
        this.coinType = coinType;
    }
}
