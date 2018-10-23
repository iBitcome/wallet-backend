package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import com.rst.cgi.data.entity.Token;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

/**
 * Created by mtb on 2018/4/12.
 * 代币类
 */
@Getter
@Setter
public class TokensRepDTO extends Readable {
    @ApiModelProperty(value = "代币英文简称")
    private String name;
    @ApiModelProperty(value = "代币全称称")
    private String fullName;
    @ApiModelProperty(value = "代币的合约地址")
    private String address;
    @ApiModelProperty(value = "代币的合约地址2")
    private String checksumAddress;
    @ApiModelProperty(value = "进制（10的幂值）")
    private Integer decimal;
    @ApiModelProperty(value = "coinType")
    private Integer coinType;

    //对token进行序列排序
    public static Comparator<Token> order = (TokensRepDTO1, TokensRepDTO2) -> {
        if (TokensRepDTO1.getOrder() == null || TokensRepDTO2.getOrder() == null) {
            if (TokensRepDTO1.getOrder() == null && TokensRepDTO2.getOrder() !=null) {
                return 1;
            }else if (TokensRepDTO1.getOrder() != null && TokensRepDTO2.getOrder() ==null) {
                return -1;
            }else {
                return 0;
            }
        } else {
            if (TokensRepDTO1.getOrder() > TokensRepDTO2.getOrder()) {
                return 1;
            }else if (TokensRepDTO1.getOrder() < TokensRepDTO2.getOrder()) {
                return  -1;
            }
            return 0;
        }

    };
}
