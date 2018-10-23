package com.rst.cgi.data.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class GetWalletDataReq {
    @ApiModelProperty("钱包标识列表")
    private List<String> walletList;
    private boolean onlyUpdateTime;
}
