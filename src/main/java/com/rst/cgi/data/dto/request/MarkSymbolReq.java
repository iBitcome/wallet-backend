package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Symbol;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class MarkSymbolReq {
    @ApiModelProperty("交易对")
    private Symbol symbol;
    @ApiModelProperty("收藏状态，0-取消，1-收藏")
    private int status;
}
