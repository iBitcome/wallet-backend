package com.rst.cgi.data.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hujia
 */
@Data
public class DHGetKeyRes {
    @ApiModelProperty("服务端生成的DH public key,客户端可以根据它和自己的DH私钥生成传输的AES密钥")
    private String pubKey;
    @ApiModelProperty("服务端对sha256(pubKey)的签名,客户端需用cgi公钥验证签名以确定信息有效性")
    private String signature;
}
