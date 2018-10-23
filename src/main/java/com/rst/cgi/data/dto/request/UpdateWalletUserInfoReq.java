package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.response.UserInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class UpdateWalletUserInfoReq {
    @ApiModelProperty("钱包标识别")
    private String walletId;
    @ApiModelProperty("用户信息")
    private UserInfo info;
}
