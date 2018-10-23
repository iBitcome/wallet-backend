package com.rst.cgi.service;

import com.alibaba.fastjson.JSONObject;
import com.rst.cgi.common.constant.CommonPage;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.PageRep;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.*;
import com.rst.cgi.data.entity.Rate;

import java.util.List;
import java.util.Map;

/**
 * Created by mtb on 2018/3/30.
 */
public interface TransactionService {

      Integer SEND_TOKEN = 1;
      Integer BURN_WHC = 0;

    CommonPage<GetAllTransactionRepDTO> getAllTransaction(GetAllTransactionReqDTO body);

    CommonPage<GetAllTransactionRepDTO> getHdAllTransaction(GetAllHdTransactionReqDTO body);

    /**
     * 获取接口中的汇率
     * @return
     */
    List<Rate> getRate();

    /**
     * 获取所有汇率
     * （包括配置中的）
     * @return
     */
    List<Rate> getAllRate();

    Integer getBlockHeight(GetBlockHeightReqDTO body);

    String sendTransactionB(SendtranxBReqDTO body);

    List<GetTxFeeRepDTO> getTxFee(GetTxFeeReqDTO body);

    /**
     * 更加交易id查询交易详情
     * @param body
     * @return
     */
    TransactionRes getTransactionById(GetTransactionByIdReqDTO body);


    /**
     * 发起以太坊交易
     * @param body
     * @return
     */
    Map<String,String> sendTransactionEth(SendTranxEReqDTO body);



    /**
     * 处理充值记录（保存或者更新记录）
     * @param json
     */
    void dealRechargeRecord(JSONObject json);

    /**
     * 获取gasLimit
     * @param fromAddress
     * @param toAddress
     * @return
     */
    Map<String, Long> getGasLimit(String fromAddress, String toAddress, String tokenName);

    /**
     * 获取B链载荷数据
     * @param body
     * @return
     */
    Map<String,String> getPayload(GetPayloadReqDTO body);

    /**
     * 获取WHC燃烧记录
     * @param body
     * @return
     */
    List<WHCBurnHistoryRepDTO> getWHCBurnHistory(WHCBurnHistoryReqDTO body);

    /**
     * 查询兑换记录列表
     * @param body
     * @return
     */
    PageRep<GetExRecordRepDTO> getExRecord(GetExRecordReqDTO body);

    /**
     * 查询兑换记录详情
     * @param body
     * @return
     */
    GetExInfoRepDTO getExRecordInfo(GetExInfoReqDTO body);

    /**
     * 验证zcash地址是否有效
     * @param map
     * @return
     */
    CommonResult validZcashAddress(Map map);

    CommonResult getZcashHeight();
}
