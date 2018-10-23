package com.rst.cgi.service.impl;

import com.google.gson.Gson;
import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.constant.Urls;
import com.rst.cgi.common.enums.CoinType;
import com.rst.cgi.common.utils.BeanCopier;
import com.rst.cgi.common.utils.DoubleUtil;
import com.rst.cgi.common.utils.OkHttpUtil;
import com.rst.cgi.conf.EthereumConfig;
import com.rst.cgi.conf.ExchangeConfig;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mongo.DexTopTradeOrderRepository;
import com.rst.cgi.data.dao.mongo.DexTopUserInfoRepository;
import com.rst.cgi.data.dao.mysql.CommonDao;
import com.rst.cgi.data.dao.mysql.RechargeDao;
import com.rst.cgi.data.dao.mysql.UserDao;
import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.dto.SymbolBrief;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.GetBalanceRes;
import com.rst.cgi.data.dto.response.GetOrderListRes;
import com.rst.cgi.data.dto.response.RechargeRecordRepDTO;
import com.rst.cgi.data.entity.*;
import com.rst.cgi.service.MarketCacheService;
import com.rst.cgi.service.TradeCenterService;
import com.rst.cgi.service.TransactionService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Service
public class DexTopTradeCenter implements TradeCenterService {
    private final Logger logger = LoggerFactory.getLogger(DexTopTradeCenter.class);

    private static final String CONNECT_TOKEN_KEY_PREFIX = "Dex-Top.TradeCenterService.Token.";
//    private static final String ADDRESS_RECHARGE_RECORD_KEY = "Recharge.Record.Address";

    @Autowired
    private DexTopUserInfoRepository dexTopUserInfoRepository;
    @Autowired
    private DexTopTradeOrderRepository dexTopTradeOrderRepository;
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RechargeDao rechargeDao;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MarketCacheService marketCacheService;
    @Autowired
    private SpecialTimedTask timedTask;


    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public TradeOrder placeOrder(PlaceOrderReq orderReq) {
        JSONObject result = JSONObject.fromObject(
                OkHttpUtil.http(ExchangeConfig.DEX_TOP.getApiHost() + "placeorder")
                          .param("amount", orderReq.getAmount())
                          .param("price", orderReq.getPrice())
                          .param("action", TradeOrder.toDexTopAction(orderReq.getAction()))
                          .param("nonce", orderReq.getNonce())
                          .param("expireTimeSec", orderReq.getExpireTimeSec())
                          .param("pairId", orderReq.getSymbol().exchangeName(
                                  ExchangeConfig.DEX_TOP.getSymbolSeparator()))
                          .param("sig", orderReq.getSig())
                          .param("traderAddr", orderReq.getTradeAddr())
                          .header("Authorization", getAuthToken( orderReq.getTradeAddr()))
                          .post());

        checkDexTopError(result);
        TradeOrder tradeOrder = TradeOrder.parseDexTopJson(result.getJSONObject("order"));

        PlaceOrderRecord record = new PlaceOrderRecord();
        BeanCopier.getInstance().copyBean(orderReq, record);
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.submit(() -> dexTopTradeOrderRepository.save(record));

        return tradeOrder;
    }

    @Override
    public void CancelOrder(CancelOrderReq cancelReq) {
        String api = ExchangeConfig.DEX_TOP.getApiHost() + "cancelorder";
        if (StringUtils.isEmpty(cancelReq.getOrderId())) {
            api = ExchangeConfig.DEX_TOP.getApiHost() + "cancelallorders";
        }

        String resStr = OkHttpUtil.http(api)
                                  .param("orderId", cancelReq.getOrderId())
                                  .param("pairId", cancelReq.getSymbol().exchangeName(
                                        ExchangeConfig.DEX_TOP.getSymbolSeparator()))
                                  .param("nonce", "" + System.currentTimeMillis())
                                  .header("Authorization", getAuthToken(cancelReq.getBindAddress()))
                                  .cookie("dex-trader-addr", cancelReq.getBindAddress())
                                  .post();

        JSONObject result = JSONObject.fromObject(resStr);
        checkDexTopError(result);
    }

    @Override
    public GetOrderListRes getOrderList(GetOrderListReq req) {
        String api = ExchangeConfig.DEX_TOP.getApiHost() + "pastorders";
        if (req.getType() == GetOrderListReq.TYPE_ACTIVE) {
            api = ExchangeConfig.DEX_TOP.getApiHost() + "activeorders";
        }

        OkHttpUtil okHttpUtil = OkHttpUtil.http(api)
                  .path(req.getBindAddress())
                  .path(req.getSymbol().exchangeName(ExchangeConfig.DEX_TOP.getSymbolSeparator()))
                  .path("" + req.getSize())
                  .path("" + req.getPage()).header(
                          "Authorization", getAuthToken(req.getBindAddress() ));

        if (req.getFrom() != null) {
            Long to = req.getTo();
            if (to == null) {
                to = System.currentTimeMillis() / 1000;
            }

            okHttpUtil.param("from_time_sec", ""+req.getFrom())
                      .param("to_time_sec", "" + to);
        }

        JSONObject result = JSONObject.fromObject(okHttpUtil.get());

        checkDexTopError(result);

        List<TradeOrder> tradeOrders = new ArrayList<>();
        JSONArray orders = result.getJSONArray("orders");
        for (int i = 0; i < orders.size(); i++) {
            tradeOrders.add(TradeOrder.parseDexTopJson(orders.getJSONObject(i)));
        }

        GetOrderListRes res = new GetOrderListRes();
        res.setOrders(tradeOrders);
        res.setPage(result.getInt("page"));
        res.setTotal(result.getInt("total"));

        return res;
    }

    @Override
    public TradeOrder getOrderDetail(GetOrderDetailReq req) {

        JSONObject result = JSONObject.fromObject(
                OkHttpUtil.http(ExchangeConfig.DEX_TOP.getApiHost() + "orderbyid")
                          .path(req.getBindAddress())
                          .path(req.getOrderId())
                          .header("Authorization", getAuthToken(req.getBindAddress()))
                          .get());

        checkDexTopError(result);

        return TradeOrder.parseDexTopJson(result.getJSONObject("order"));
    }

    @Override
    public void withdraw(WithdrawReq req) {
        Double minWithdrawValue = 0.01;
        String tokenName = "";
        if (!StringUtils.isEmpty(req.getToken())) {
            tokenName = req.getToken().toUpperCase();
        }
        Token token = SpecialTimedTask.TOKENS_NAME_KEY_MAP.get(tokenName);
        String value = "0";
        if (token != null) {
            Double valueDB = Double.valueOf(req.getAmount()) / Math.pow(10, token.getDecimal());
            value = DoubleUtil.format(valueDB, 8);
            if (token.getName().equalsIgnoreCase(CoinType.ETH.getName()) && valueDB <= minWithdrawValue) {
                CustomException.response(Error.WITHDRAW_MONEY_NOT_ENOUGH);
            }
        } else {
            CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT);
        }

        long nowTime = System.currentTimeMillis();
        String api = ExchangeConfig.DEX_TOP.getApiHost() + "withdraw";

        String resStr = OkHttpUtil.http(api)
                                  .param("amount", value)
                                  .param("tokenId", req.getToken())
                                  .param("timestampSec", req.getTimestampSec())
                                  .param("sig", req.getSig())
                                  .header("Authorization", getAuthToken(req.getAddress()))
                                  .cookie("dex-trader-addr", req.getAddress())
                                  .post();

        JSONObject result = JSONObject.fromObject(resStr);

        //保存提现记录
        RechargeRecord rechargeRecord = new RechargeRecord();
        Integer status = 1;
        Integer height = 12;
        if (result.containsKey("error")) {
            status = -1;
            height = -1;
        }

        rechargeRecord.setStatus(status);
        rechargeRecord.setHeight(height);
        rechargeRecord.setValue(req.getAmount());
        rechargeRecord.setCreateTime(nowTime);
        rechargeRecord.setTokenName(req.getToken());
        rechargeRecord.setRechargeTime(nowTime);
        rechargeRecord.setType(2);
        rechargeRecord.setTraderAddress(req.getAddress());
        rechargeRecord.setChannel(RechargeRecord.RECORD_CHANNEL_BEECOIN);
        commonDao.insert(rechargeRecord);

        checkDexTopError(result);
    }


    @Override
    public void markSymbol(int userId, MarkSymbolReq req) {
        FavoriteSymbol favoriteSymbol = new FavoriteSymbol();
        favoriteSymbol.setUserId(userId);
        favoriteSymbol.setExchange(ExchangeConfig.DEX_TOP.getName());
        favoriteSymbol.setBaseAsset(req.getSymbol().getBaseAsset());
        favoriteSymbol.setQuoteAsset(req.getSymbol().getQuoteAsset());

        FavoriteSymbol symbol = commonDao.queryFirstBy(favoriteSymbol);

        if (symbol == null) {
            favoriteSymbol.setStatus(req.getStatus());
            commonDao.insert(favoriteSymbol);
        } else {
            symbol.setStatus(req.getStatus());
            commonDao.update(symbol);
        }
    }

    @Override
    public List<SymbolBrief> getFavoriteSymbols(int userId) {
        FavoriteSymbol favoriteSymbol = new FavoriteSymbol();
        favoriteSymbol.setUserId(userId);
        favoriteSymbol.setExchange(ExchangeConfig.DEX_TOP.getName());
        favoriteSymbol.setStatus(1);

        Set<Symbol> symbolSet = commonDao.queryBy(favoriteSymbol)
                                         .stream()
                                         .map(fs -> new Symbol(fs.getBaseAsset(), fs.getQuoteAsset()))
                                         .collect(Collectors.toSet());

        return marketCacheService.getSymbolData(
                ExchangeConfig.DEX_TOP.getName(), null, null)
                          .stream().filter(item -> symbolSet.contains(item.getSymbol()))
                          .collect(Collectors.toList());
    }

    @Override
    public GetBalanceRes getBalances(String bindAddress) {
        String api = ExchangeConfig.DEX_TOP.getApiHost() + "balances";

        String resStr = OkHttpUtil.http(api)
                                  .path(bindAddress)
                                  .header("Authorization", getAuthToken(bindAddress))
                                  .get();

        JSONObject result = JSONObject.fromObject(resStr);
        checkDexTopError(result);
        GetBalanceRes getBalanceRes = new Gson().fromJson(resStr, GetBalanceRes.class);

        return new Gson().fromJson(resStr, GetBalanceRes.class);
    }

    @Override
    public String connect(ConnectExchangeReq req) {
        String api = ExchangeConfig.DEX_TOP.getApiHost() + "authenticate";

        JSONObject result = JSONObject.fromObject(OkHttpUtil.http(api)
                                                            .param("traderAddr", req.getTraderAddr())
                                                            .param("timestampSec", "" + req.getTimestampSec())
                                                            .param("durationSec", "1296000")
                                                            .param("sig", req.getSig())
                                                            .post());
        checkDexTopError(result);

        String token = result.getString("token");
        logger.info("save token>{}:{}", req.getTraderAddr(), token);
        stringRedisTemplate.opsForValue().set(CONNECT_TOKEN_KEY_PREFIX +
                req.getTraderAddr(), token, 1296000, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public void isConnected(String bindAddress) {
        getAuthToken(bindAddress);
    }

    @Override
    public DexTopUserInfo bind(BindExchangeAccountReq req) {
        UserEntity userEntity = userDao.findByEmail(req.getEmail());
        if (userEntity == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        int userId = userEntity.getId();

        DexTopUserInfo dexTopUserInfo = dexTopUserInfoRepository.findByUserId(userId);
        if (dexTopUserInfo == null) {
            dexTopUserInfo = new DexTopUserInfo();
        }

        dexTopUserInfo.setUserId(userId);
        dexTopUserInfo.setAccount(req.getExchangeAccount());
        dexTopUserInfo.setPassword(req.getExchangePassword());
        dexTopUserInfo.addBindAddress(req.getBaseToken(), req.getAddressBindWithExchange());

        dexTopUserInfoRepository.save(dexTopUserInfo);

        String api = ExchangeConfig.DEX_TOP.getApiHost() + "authenticate";

        JSONObject result = JSONObject.fromObject(OkHttpUtil.http(api)
                                                            .param("email", req.getExchangeAccount())
                                                            .param("password", "" + req.getExchangePassword())
                                                            .post());
        checkDexTopError(result);

        String token = result.getString("token");
        logger.info("token:{}", token);
        stringRedisTemplate.opsForValue().set(CONNECT_TOKEN_KEY_PREFIX +
                userId + "." + req.getAddressBindWithExchange(), token, 1296000, TimeUnit.SECONDS);

        return dexTopUserInfo;
    }


    @Autowired
    private TransactionService transactionService;
    @Value("${dex.smart.contract}")
    private String dexSmartContract;

    @Override
    public List<RechargeRecordRepDTO> getRechargeRecord(String address, Integer type) {
        com.alibaba.fastjson.JSONObject res = com.alibaba.fastjson.JSONObject.parseObject(OkHttpUtil.http(Urls.GET_DEX_ETH_TX)
                .param("from", address)
                .param("to", dexSmartContract)
                .post());
        logger.info("newsServer Record:{}",res);
        if (res.getJSONArray("data").size() != 0) {
            com.alibaba.fastjson.JSONArray data = res.getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                com.alibaba.fastjson.JSONObject txJson = data.getJSONObject(i);
                if (txJson.containsKey("timestamp") &&
                        !org.apache.commons.lang.StringUtils.isBlank(txJson.getString("timestamp")) &&
                        txJson.getString("timestamp").length() > 13) {
                    txJson.put("timestamp", Long.valueOf(txJson.getString("timestamp").substring(0, 13)));

                }
                transactionService.dealRechargeRecord(txJson);
            }

        }

        List<RechargeRecord> records = new ArrayList<>();
        if (type != 0) {
            records = rechargeDao.findCashRecordsByType(type, address);
        } else {
            records = rechargeDao.findCashRecordsByAddress(address);
        }
        records.sort((o1, o2) -> {
            if (o1.getRechargeTime() > o2.getRechargeTime()) {
                return -1;
            } else {
                return 1;
            }
        });

        String ETHStrHeight = stringRedisTemplate.opsForValue().
                get(Constant.BLOCK_CURRENT_HEIGHT + CoinType.ETH.getCode());
        Long ETHHeight = Long.valueOf(ETHStrHeight);

        List<RechargeRecordRepDTO> result = records.stream().map(rechargeRecord -> {
            RechargeRecordRepDTO repDTO = new RechargeRecordRepDTO();
            BeanCopier.getInstance().copyBean(rechargeRecord, repDTO);
            if (rechargeRecord.getStatus() != -1) {
                if (rechargeRecord.getHeight() ==  null || rechargeRecord.getHeight() == 0) {
                    repDTO.setConfirmNum(0);
                } else {
                    if (ETHHeight < rechargeRecord.getHeight()) {
                       timedTask.updateETHBlockHeight();
                    }
                    repDTO.setConfirmNum(ETHHeight.intValue() - rechargeRecord.getHeight());
                }
            } else {
                repDTO.setConfirmNum(-1);
            }

            return repDTO;
        }).collect(Collectors.toList());

        return result;
    }

    @Override
    public Integer getApproveHeight(String token, String address) {
        RechargeRecord find = new RechargeRecord();
        find.setTokenName(token);
        find.setTraderAddress(address);
        find.setType(3);
        find.setStatus(1);
        find = commonDao.queryFirstBy(find);
        if (find != null) {
            return find.getHeight();
        }

        return null;
    }

    private String getAuthToken(String traderAddr) {
        String token = stringRedisTemplate.opsForValue().get(
                CONNECT_TOKEN_KEY_PREFIX + traderAddr);
        if (StringUtils.isEmpty(token)) {
            //先连接接到交易所
            CustomException.response(Error.EXCHANGE_NOT_CONNECTED);
        }

        return "Bearer " + token;
    }

    private void checkDexTopError(JSONObject result) {
        if (result.containsKey("error")) {
            logger.info("DexTop Error:{}", result);
            //token error
            if (result.containsKey("code") && result.getInt("code") == 16) {
                CustomException.response(Error.EXCHANGE_NOT_CONNECTED);
            }

            CustomException.response(result.getString("error"));
        }
    }
}
