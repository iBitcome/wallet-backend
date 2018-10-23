package com.rst.cgi.data.dto;

import lombok.Data;

import java.util.List;

/**
 * @author hujia
 */
@Data
public class DexTopMarket {
    private String marketAddr;
    private String label;
    private MarketConfig config;

    @Data
    public static class MarketConfig {
        private String makerFeeRateE4;
        private String takerFeeRateE4;
        private String withdrawFeeRateE4;
        private List<DexToken> cashTokens;
        private List<DexToken> stockTokens;
        private List<DexToken> disabledTokens;
    }

    @Data
    public static class DexToken {
        private String tokenId;
        private int tokenCode;
        private String tokenAddr;
        private String scaleFactor;
        private double minDepositAmount;
        private double minWithdrawAmount;
    }
}
