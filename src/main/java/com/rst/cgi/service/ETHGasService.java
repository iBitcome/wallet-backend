package com.rst.cgi.service;

/**
 * @author mtb
 */
public interface ETHGasService {
    /**
     * 获取ETH的gaslimit
     * @param fromAddress
     * @param toAddress
     * @return
     */
    long getETHGasLimit(String fromAddress, String toAddress);


    /**
     * 获取代币的gaslimit
     * @param fromAddress
     * @param toAddress
     * @return
     */
    long getTokenGasLimit(String fromAddress, String toAddress, String tokenAddress);
}
