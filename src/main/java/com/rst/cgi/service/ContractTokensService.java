package com.rst.cgi.service;

import com.rst.cgi.data.dto.response.GetAllTransactionRepDTO;
import com.rst.cgi.data.entity.Token;

import java.util.List;

public interface ContractTokensService {

    /**
     * 处理并获取B链上合约代币的交易记录
     * @param addressList
     * @param transType
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<GetAllTransactionRepDTO> dealContractTokensTranB(List<String> addressList,
                                                          Token token,
                                                          Long confirmNum,
                                                          String transType,
                                                          Integer pageNo,
                                                          Integer pageSize);
}
