package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class AddressReqDTO extends Readable {
    @ApiModelProperty(value = "地址列表",required = true)
    private List<Address> addressList;
    @ApiModelProperty(value = "查询的代币名称(默认WHC)")
    private String tokenName = "WHC";


    @Data
    public static class Address {
        @ApiModelProperty(value = "地址Hash",required = true)
        private String addressHash;
        @ApiModelProperty("地址类型:0-pkh,1-psh")
        private int type = 0;
    }
}
