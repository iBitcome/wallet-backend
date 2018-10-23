package com.rst.cgi.data.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class UpdatePushInfoReq {
    @ApiModelProperty("push连接的设备")
    private String device;
    @ApiModelProperty("需要接收推送的钱包列表")
    private List<String> walletIds;
}
