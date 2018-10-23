package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.entity.BlockTransaction;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class TransactionHistoryRes {
    @Data
    public static class TokenItem {
        @ApiModelProperty(value = "代币价格（单位：美元）")
        private String name;
        @ApiModelProperty(value = "代币价格（单位：美元）")
        private String price;
        @ApiModelProperty(value = "代币最小单位的幂")
        private int decimal;
        @ApiModelProperty(value = "当前最高块高度")
        private int currentHeight;
    }

    @Data
    public static class TransactionItem {
        @ApiModelProperty(value = "是否转出交易")
        private Boolean rollOut;
        @ApiModelProperty(value = "交易信息")
        private BlockTransaction transaction;
    }

    @ApiModelProperty(value = "历史交易列表")
    private List<TransactionItem> transactions;
    @ApiModelProperty(value = "历史交易列表")
    private List<TokenItem> tokens;
}
