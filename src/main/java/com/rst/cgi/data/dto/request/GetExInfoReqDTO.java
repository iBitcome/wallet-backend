package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetExInfoReqDTO extends Readable {
    @ApiModelProperty(value = "发起链的交易hash",required = true)
    private String fromHash;

}
