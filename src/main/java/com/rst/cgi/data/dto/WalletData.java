package com.rst.cgi.data.dto;

import com.rst.cgi.data.dto.response.UserInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hujia
 */
@Getter
@Setter
public class WalletData {
    public static final int TYPE_HD = 0;
    public static final int TYPE_SINGLE = 1;
    public static final int TYPE_READONLY = 2;

    @ApiModelProperty("钱包名称")
    private String name;
    @ApiModelProperty("钱包描述")
    private String desc;
    @ApiModelProperty("钱包唯一标识别：公钥或主公钥hash得来")
    private String identify;
    @ApiModelProperty("钱包类型:0-HD,1-单地址,2-观察钱包")
    private int type = TYPE_HD;
    @ApiModelProperty("钱包头像id")
    private String faceId;
    @ApiModelProperty("HD钱包已使用的资产列表")
    private List<TokenData> tokenData;
    @ApiModelProperty("钱包归属的用户信息")
    private UserInfo owner;
    @ApiModelProperty("钱包创建者的设备id")
    private String ownerDevice;
    @ApiModelProperty("更新的时间戳")
    private Long updateTime;
}
