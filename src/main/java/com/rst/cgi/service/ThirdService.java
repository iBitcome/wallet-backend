package com.rst.cgi.service;

import com.rst.cgi.data.dto.request.DexPlaceOrderDTO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 第三方服务接口
 * @author huangxiaolin
 * @date 2018-04-20 下午6:03
 */
public interface ThirdService {

    /**
     * 通过代币获取等价的美元价格
     * @author huangxiaolin
     * @date 2018-04-20 14:31
     * @param token 代币，比如BTC、ETH
     */
    double getUSDByToken(String token);

    Map<String, Double> getUSDByTokens(List<String> tokenList);

    /**
     * 获取目前支持的代币，提供数据查询参数
     * @author hxl
     * 2018/5/28 下午6:54
     */
    String getSupportTokenParam();


    JSONObject getDexPlaceOrder(DexPlaceOrderDTO placeOrderDto);

    JSONObject dexCancelOrder(DexPlaceOrderDTO placeOrderDto);

    JSONObject dexCancelAllOrders(DexPlaceOrderDTO placeOrderDto);

    JSONObject getDexWithdraw(String traderAddr, String token, String amount);

    /**
     * 获取dex市场信息
     * @author huangxiaolin
     * @date 2018-05-08 15:09
     */
    JSONObject getDexMaket();

    JSONObject getDexPairs(String tokenId);

    JSONObject getDexPairInfo(String pairId);

    JSONObject getDexTradeHistory(String pairId, int size);

    JSONObject getDexPairDepth(String pairId, int size);

    JSONObject getDexActiveOrders(String walletAddr, String pairId, int size, int page);

    JSONObject getDexPastOrders(String walletAddr, String pairId, int size, int page);

    JSONObject getDexOrderById(String orderId);
    /**
     * 获取某个钱包地址下的某个交易对的最近交易信息
     * @author huangxiaolin
     * @date 2018-05-09 16:10
     * @param walletAddr 钱包地址
     * @param  pairId 交易对
     * @param size 查询的交易数量
     */
    JSONObject getDexGrades(String walletAddr, String pairId, int size);

    /**
     * 获取认证口令
     * @author huangxiaolin
     * @date 2018-05-09 16:14
     */
    String getDexAuthToken(String email, String password);

    String getDexBalances(String traderAddr);
}
