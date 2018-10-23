package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import com.rst.cgi.data.dto.TokenDexBalance;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class GetBalanceRes extends Readable {
    @ApiModelProperty("各代币的余额情况")
    private List<TokenDexBalance> balances;
    @ApiModelProperty("余额总估值")
    private String estimatedValue;
}
