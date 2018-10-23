package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by mtb on 2018/3/29.
 */
@Data
public class NumbersManageWalletReqDTO extends Readable{
    @ApiModelProperty(value = "设备唯一标识", required = true)
    private String equipmentNo;
    @ApiModelProperty(value = "操作类型（1:增加，0:删除）", required = true)
    private Integer type;
    @ApiModelProperty(value = "HD钱包主公钥（HD钱包需此参数）")
    private String publicKey;
    @ApiModelProperty(value = "是否是HD钱包(1:是HD钱包，其他：普通钱包)", required = true)
    private Integer isHd = 1;
    @ApiModelProperty(value = "钱包地址列表（新增钱包需此参数）")
    private List<ManageWalletInfo> walletInfoList;

    @Data
    public static class ManageWalletInfo {
        @ApiModelProperty(value = "钱包地址", required = true)
        private String walletAddress;
        @ApiModelProperty("地址类型:0-pkh,1-sh,3-eos")
        private int type = 0;
        @ApiModelProperty(value = "钱包路径深度(HD钱包新增时需此参数)")
        private String walletPathDepth;
        @ApiModelProperty(value = "EOS account", required = false)
        private List<String> account;
    }

}
