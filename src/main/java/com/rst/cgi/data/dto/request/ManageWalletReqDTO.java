package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by mtb on 2018/3/29.
 */
public class ManageWalletReqDTO extends Readable{
    @ApiModelProperty(value = "设备唯一标识", required = true)
    private String equipmentNo;
    @ApiModelProperty(value = "钱包地址", required = true)
    private String walletAddress;
    @ApiModelProperty(value = "钱包路径深度(HD钱包新增时需此参数)")
    private String walletPathDepth;
    @ApiModelProperty(value = "操作类型（1:增加，0:删除）", required = true)
    private Integer type;
    @ApiModelProperty(value = "HD钱包主公钥（HD钱包需此参数）")
    private String publicKey;
    @ApiModelProperty(value = "是否是HD钱包(1:是HD钱包，其他：普通钱包)", required = true)
    private Integer isHd;
    @ApiModelProperty("地址类型:0-pkh,1-sh")
    private int addressType = 0;

    public int getAddressType() {
        return addressType;
    }

    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }

    public String getWalletPathDepth() {
        return walletPathDepth;
    }

    public void setWalletPathDepth(String walletPathDepth) {
        this.walletPathDepth = walletPathDepth;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Integer getIsHd() {
        return isHd;
    }

    public void setIsHd(Integer isHd) {
        this.isHd = isHd;
    }

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
