package com.rst.cgi.data.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class DeleteWalletReq {
    @ApiModelProperty("需要从设备移除的钱包")
    private String walletId;
    @ApiModelProperty("设备")
    private String device;
}
