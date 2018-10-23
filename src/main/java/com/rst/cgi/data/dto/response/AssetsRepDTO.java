package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by mtb on 2018/4/12.
 */
@Setter
@Getter
public class AssetsRepDTO extends Readable{
    @ApiModelProperty(value = "金额")
    private String value;
    @ApiModelProperty(value = "代币英文简称")
    private String name;
    @ApiModelProperty(value = "代币的合约地址")
    private String address;
    @ApiModelProperty(value = "代币的合约地址2")
    private String checksumAddress;
    @ApiModelProperty(value = "进制（10的幂值）")
    private Integer decimal;
    @ApiModelProperty(value = "代币价格（单位：美元）")
    private Double price;
    @ApiModelProperty(value = "代币排序序列")
    private Double order;
}
