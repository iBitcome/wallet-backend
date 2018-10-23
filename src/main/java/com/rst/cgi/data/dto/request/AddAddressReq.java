package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.TokenData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class AddAddressReq {
    @ApiModelProperty("钱包唯一标识别：公钥或主公钥hash得来")
    private String walletId;
    @ApiModelProperty("资产名称")
    private String name;
    @ApiModelProperty("增加的地址")
    private List<TokenData.Address> addresses;
}
