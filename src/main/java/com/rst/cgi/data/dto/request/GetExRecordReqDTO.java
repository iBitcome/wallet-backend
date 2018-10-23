package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetExRecordReqDTO extends Readable {
    @ApiModelProperty("币种名称")
    private String tokenName;
    @ApiModelProperty("钱包hash")
    private String walletHash;
    @ApiModelProperty("页码")
    private Integer pageNo = 1;
    @ApiModelProperty("页码大小")
    private Integer pageSize = 10;
//    @Data
//    public static class AddressInfo {
//        @ApiModelProperty("地址")
//        private String address;
//        @ApiModelProperty("coinType")
//        private Integer coinType;
//        @ApiModelProperty("地址类型:0-p2pkh,1-p2sh")
//        private Integer type = 0;
//    }
}
