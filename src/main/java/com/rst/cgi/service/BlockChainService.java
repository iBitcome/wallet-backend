package com.rst.cgi.service;

import com.rst.cgi.data.dto.TokenData;
import com.rst.cgi.data.dto.request.TransactionHistoryReq;
import com.rst.cgi.data.dto.response.TransactionHistoryRes;

import java.util.List;

/**
 * @author hujia
 */
public interface BlockChainService {
    /**
     * 获取历史交易记录
     * @param request
     * @return
     */
    TransactionHistoryRes getTransactionHistory(TransactionHistoryReq request);

    /**
     * 同步更新所有地址的历史交易记录到缓存系统
     * @param tokenAddressList
     */
    void updateTxHistoryFromBlockChain(List<TokenData> tokenAddressList);

    /**
     * 异步更新所有地址的历史交易记录到缓存系统
     * @param tokenAddressList
     */
    void asyncUpdateTxHistoryFromBlockChain(List<TokenData> tokenAddressList);
}
