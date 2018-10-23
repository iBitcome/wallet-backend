package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.WalletData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hujia
 */
@Getter
@Setter
public class SyncWalletDataReq {
    public static final int OP_UPDATE = 2;
    public static final int OP_IMPORT = 1;
    public static final int OP_CREATE = 0;

    @ApiModelProperty("待操作的钱包数据")
    private WalletData walletData;
    @ApiModelProperty("同步操作：0-创建, 1-导入, 2-更新")
    private int operation;
    @ApiModelProperty("操作者")
    private String operator;
}
