package com.rst.cgi.service;

import com.rst.cgi.data.dto.response.AppVersionRepDTO;
import com.rst.cgi.data.dto.response.ExConfigRepDTO;
import com.rst.cgi.data.dto.response.OperationMsgRepDTO;

import java.util.List;
import java.util.Map;

public interface ConfigService {
    void registerPush(String equipmentNo, String deviceToken);

    List<AppVersionRepDTO> getAppPublishRecord(String channel);

    /**
     * messageType:'信息类型（1-活动推送，2-趣味活动，3-banner
     * "lang:'语种选择（0-中文，1-英文）'，'num':查询数量（不传查询所有）,'id':信息唯一id
     * @return
     */
    List<OperationMsgRepDTO> getOperationMsg(Map<String, Integer> body);

    /**
     * 获取铸币熔币业务的相关配置
     * @return
     */
    ExConfigRepDTO exConfig();
}
