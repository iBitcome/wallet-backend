package com.rst.cgi.data.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class GetWalletUserInfoReq {
    @ApiModelProperty("钱包id列表")
    private List<String> walletIds;
}
