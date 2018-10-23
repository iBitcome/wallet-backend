package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * Created by mtb on 2018/3/26.
 */
public class TrackTransactionReqDTO extends Readable implements Serializable{
    @ApiModelProperty(value = "交易Hash",required = true)
    private String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
