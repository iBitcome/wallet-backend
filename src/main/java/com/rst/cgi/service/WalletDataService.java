package com.rst.cgi.service;

import com.rst.cgi.data.dto.WalletData;
import com.rst.cgi.data.dto.request.AddAddressReq;
import com.rst.cgi.data.dto.request.SyncWalletDataReq;
import com.rst.cgi.data.dto.request.UpdateWalletUserInfoReq;
import com.rst.cgi.data.dto.response.UserInfo;

/**
 * @author hujia
 */
public interface WalletDataService {
    /**
     * 创建钱包
     * @param user 请求用户
     * @param request
     * @param requestIp 请求方ip
     * @return 数据的最后更新时间
     */
    WalletData syncWalletData(Integer user, SyncWalletDataReq request, String requestIp);

    /**
     * 更新钱包的用户信息
     * @param request
     */
    void updateWalletUserInfo(UpdateWalletUserInfoReq request);

    /**
     * 给钱包添加地址
     * @param req
     */
    void addWalletAddress(AddAddressReq req);

    /**
     * 获取钱包的用户信息
     * @param walletIds
     * @return
     */
    UserInfo getUserInfo(String walletIds);

    /**
     * 删除钱包
     * @param userId
     * @param publicKey
     * @return
     */
    void deleteWalletData(int userId, String publicKey);

    /**
     * 删除钱包
     * @param walletId
     * @return
     */
    void deleteWalletData(int walletId);

    /**
     * 获取钱包的所有数据
     * @param userId
     * @param publicKey
     * @return
     */
    WalletData getWalletData(int userId, String publicKey);

    /**
     * 获取钱包的所有数据
     * @param walletId
     * @return
     */
    WalletData getWalletData(int walletId);

    /**
     * 获取钱包的数据的最后更新时间
     * @param publicKey
     * @param userId
     * @return
     */
    long getLastUpdateTime(int userId, String publicKey);

    /**
     * 获取钱包的数据的最后更新时间
     * @param walletId
     * @return
     */
    long getLastUpdateTime(int walletId);

}
