package com.rst.cgi.service;

/**
 * @author hujia
 */
public interface ETHNonceService {
    /**
     * 保存交易广播成功的nonce值
     * @param address 交易发送方地址
     * @param nonce 交易nonce
     * @param txid 交易hash
     * @return
     */
    void saveNonce(String address, Long nonce, String txid);

    /**
     * 获取下一个可用的nonce
     * @param address 交易发送方地址
     * @return
     */
    long nextNonce(String address);
}
