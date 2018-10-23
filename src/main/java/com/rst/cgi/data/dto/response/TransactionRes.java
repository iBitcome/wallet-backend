package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.entity.BlockTransaction;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author mtb
 */
@Data
public class TransactionRes {
        @ApiModelProperty(value = "代币名称")
        private String name;
        @ApiModelProperty(value = "代币价格（单位：美元）")
        private String price;
        @ApiModelProperty(value = "代币最小单位的幂")
        private int decimal;
        @ApiModelProperty(value = "当前最高块高度")
        private int currentHeight;
        @ApiModelProperty(value = "交易信息")
        private BlockTransaction transaction;
}
