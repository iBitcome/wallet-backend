package com.rst.cgi.service;

import com.rst.cgi.data.dto.SymbolBrief;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.GetBalanceRes;
import com.rst.cgi.data.dto.response.GetOrderListRes;
import com.rst.cgi.data.dto.response.RechargeRecordRepDTO;
import com.rst.cgi.data.entity.DexTopUserInfo;
import com.rst.cgi.data.entity.TradeOrder;

import java.util.List;

/**
 * @author hujia
 */
public interface TradeCenterService {
    /**
     * 委托订单
     * @param orderReq
     * @return
     */
    TradeOrder placeOrder(PlaceOrderReq orderReq);

    /**
     * 取消订单
     * @param cancelReq
     * @return
     */
    void CancelOrder(CancelOrderReq cancelReq);

    /**
     * 获取订单列表
     * @param req
     * @return
     */
    GetOrderListRes getOrderList(GetOrderListReq req);

    /**
     * 获取订单详情
     * @param req
     * @return
     */
    TradeOrder getOrderDetail(GetOrderDetailReq req);

    /**
     * 提现到绑定的地址
     * @param req
     */
    void withdraw(WithdrawReq req);

    /**
     * 改变交易对的收藏状态
     * @param userId
     * @param req
     */
    void markSymbol(int userId, MarkSymbolReq req);

    /**
     * 获取收藏列表
     * @param userId
     * @return
     */
    List<SymbolBrief> getFavoriteSymbols(int userId);

    /**
     * 获取绑定地址的余额情况
     * @param bindAddress
     * @return
     */
    GetBalanceRes getBalances(String bindAddress);

    /**
     * 用户连入交易所
     * @param req
     * @return
     */
    String connect(ConnectExchangeReq req);

    /**
     * 用户的地址是否已经连入交易所
     * @param bindAddress
     */
    void isConnected(String bindAddress);

    /**
     * 绑定用户到交易所
     * @param beaReq 与交易所绑定的信息
     * @return
     */
    DexTopUserInfo bind(BindExchangeAccountReq beaReq);

    /**
     * 查询钱包地址下的所有充值记录
     * @param
     * @return
     */
    List<RechargeRecordRepDTO> getRechargeRecord(String address, Integer type);

    /**
     * 代币approve区块高度
     * @param token
     * @return null表述尚未进区块
     */
    Integer getApproveHeight(String token, String address);
}
