package com.rst.cgi.service;


import com.rst.cgi.data.dto.request.ActivityAddressReqDTO;
import com.rst.cgi.data.dto.response.GetDappRepDTO;

import java.util.List;


public interface ActivityService {

    /**
     * 提交活动地址
     * @param body
     */
    void submitActivityInfo(ActivityAddressReqDTO body);

    /**
     * 获取Dapp信息
     * @return
     */
    List<GetDappRepDTO> GetDappInfo();
}
