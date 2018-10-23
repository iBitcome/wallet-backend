package com.rst.cgi.service;

import com.rst.cgi.data.dto.response.VersionConfigResDTO;
import com.rst.cgi.data.entity.VersionConfigure;

/**
 *
 * @author mtb
 * @date 2018/4/14
 */
public interface ClientVersionService {

    /**
     * 更新版本信息
     * @param body
     */
    void updateConfigure(VersionConfigure body);

    /**
     * 获取版本配置信息
     * @return
     */
    VersionConfigResDTO getVersionConfig();

    /**
     * 是否合法的客户端版本
     * @return
     */
    boolean isValidClient();

    /**
     * 是否最新的客户端版本
     * @return
     */
    boolean isLatestClient();
}
