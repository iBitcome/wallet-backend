package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by mtb on 2018/4/27.
 */
@Setter
@Getter
public class InitWalletDTO extends Readable{
    @ApiModelProperty("设备")
    private String device;
    @ApiModelProperty("当前设备存在的钱包Hash值")
    private List<String> walletHash;
}
