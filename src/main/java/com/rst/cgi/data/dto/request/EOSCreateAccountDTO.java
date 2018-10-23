package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EOSCreateAccountDTO extends Readable {
    @ApiModelProperty("创建者")
    private String creator;
    @ApiModelProperty("新建账户")
    private String newAccount;
    @ApiModelProperty("公钥")
    private String owner;
    @ApiModelProperty("公钥")
    private String active;
    @ApiModelProperty("ram")
    private Long buyRam;
    @ApiModelProperty("网络抵押")
    private String stakeNetQuantity;
    @ApiModelProperty("cpu抵押")
    private String takeCpuQuantity;
    @ApiModelProperty("抵押资产是否送给对方 0 自己所有 1 对方所有")
    private Long transfer;

}
