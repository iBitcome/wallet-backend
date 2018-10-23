package com.rst.cgi.common.enums;

public enum CoinType {
    BTC (0),
    BCH (145),
    ETH (60),
    EOS (194),
    WHC (145),
    usdt (0),
    ZEC (133),
    GATEWAY (null);

    private final Integer code;

    CoinType(Integer code){
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return this.toString();
    }
}
