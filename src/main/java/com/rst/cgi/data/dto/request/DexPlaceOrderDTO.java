package com.rst.cgi.data.dto.request;

/**
 * @author huangxiaolin
 * @date 2018-05-08 下午5:14
 */
public class DexPlaceOrderDTO {

    private String orderId;
    private String traderAddr;
    private String pairId;
    private String amount;
    private String price;
    private String action;
    private long nonce;
    //单位：秒
    private long expireTimeSec;
    private String sig;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTraderAddr() {
        return traderAddr;
    }

    public void setTraderAddr(String traderAddr) {
        this.traderAddr = traderAddr;
    }

    public String getPairId() {
        return pairId;
    }

    public void setPairId(String pairId) {
        this.pairId = pairId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public long getExpireTimeSec() {
        return expireTimeSec;
    }

    public void setExpireTimeSec(long expireTimeSec) {
        this.expireTimeSec = expireTimeSec;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }
}
