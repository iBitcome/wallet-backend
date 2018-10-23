package com.rst.cgi.service;

import com.rst.cgi.data.dto.request.InitWalletDTO;

import java.util.List;

/**
 * @author hujia
 */
public interface PushDeviceService {
    /**
     * 返回钱包推送设备列表
     * @param walletIds 钱包的数据库表ID列表
     * @return 待推送设备列表
     */
    List<String> getAvailableDevices(int... walletIds);

    /**
     * 覆盖更新推送设备信息
     * @param device
     * @param wallets
     */
    void updateAvailableDevices(String device, List<String> wallets);

    /**
     * 保存推送设备信息
     * @param device
     * @param wallets
     */
    void addAvailableDevices(String device, List<String> wallets);

    /**
     * 删除推送设备信息
     * @param device
     * @param wallets
     */
    void deleteAvailableDevices(String device, List<String> wallets);

    /**
     * 保存推送设备信息
     * @param device
     * @param walletIds
     */
    void addAvailableDeviceIds(String device, List<Integer> walletIds);

    /**
     * 删除推送设备信息
     * @param device
     * @param walletIds
     */
    void deleteAvailableDeviceIds(String device, List<Integer> walletIds);

    /**
     * 初始化设备的钱包的推送状态
     * @param body
     */
    void initAvailableDevices(InitWalletDTO body);
}
