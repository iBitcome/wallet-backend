package com.rst.cgi.service.impl;

import com.google.gson.Gson;
import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Urls;
import com.rst.cgi.common.enums.CoinType;
import com.rst.cgi.common.utils.DateUtil;
import com.rst.cgi.common.utils.HttpService;
import com.rst.cgi.common.utils.RpcClient;
import com.rst.cgi.common.utils.Web3jClient;
import com.rst.cgi.data.dao.mongo.TokenRepository;
import com.rst.cgi.data.dao.mysql.CommonDao;
import com.rst.cgi.data.dao.mysql.InvitationCodeDao;
import com.rst.cgi.data.dao.mysql.RechargeDao;
import com.rst.cgi.data.dto.response.InvitationTopRepDTO;
import com.rst.cgi.data.entity.RechargeRecord;
import com.rst.cgi.data.entity.Token;
import com.rst.cgi.data.entity.TokenPriceHistory;
import com.rst.cgi.service.ThirdService;
import com.rst.cgi.service.TokenPriceHistoryService;
import com.rst.cgi.service.TransactionService;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mtb on 2018/4/13.
 */
@Service
public class SpecialTimedTask {

    private final Logger logger = LoggerFactory.getLogger(SpecialTimedTask.class);

    public static Map<String, Token> TOKENS_MAP = new HashMap<>();
    public static Map<String, Token> TOKENS_NAME_KEY_MAP = new HashMap<>();
    public static Map<String, Integer> POP_TOP = new HashMap<>();
//    public static Map<Integer, Token> TOKENS_COINTYPE_KEY_MAP = new HashMap<>();
//    public static List<Rate> RATE_LIST = new ArrayList<>();
//    public static Map<String,Money> MONEY_CODE_MAP = new HashMap<>();
//    public static Map<String,Money> MONEY_NAME_MAP = new HashMap<>();

    private volatile int tokenRequestCount = 0;
    private volatile boolean needInitSaveToken = true;

    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private HttpService httpService;
    @Autowired
    private TokenPriceHistoryService tokenPriceHistoryService;
    @Autowired
    private ThirdService thirdService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private InvitationCodeDao invitationCodeDao;

    @Value("${money.rate}")
    private String moneyRateStr;
    @Value("${ac.now.type}")
    private String type;

    @PostConstruct
    private void init(){
        //获取当前系统支持的币种集合
        List<Token> tokens = tokenRepository.findAll();
        tokens.forEach(token -> {
//            TOKENS_COINTYPE_KEY_MAP.put(token.getCoinType(), token);
            TOKENS_MAP.put(token.getAddress(), token);
            TOKENS_MAP.put(token.getChecksumAddress(), token);
            TOKENS_NAME_KEY_MAP.put(token.getName(), token);
        });

        //统计目前数据库中邀请人数前十的用户
        List<InvitationTopRepDTO> invitationTopRepDTOS =  invitationCodeDao.getInvitationTop();
        stringRedisTemplate.delete(Constant.INVITATION_TOP);
        invitationTopRepDTOS.forEach(invitationTopRepDTO -> {
            stringRedisTemplate.opsForZSet().add(Constant.INVITATION_TOP,
                    invitationTopRepDTO.getId().toString(),
                    invitationTopRepDTO.getInvitationNum());
        });

    }

    @Scheduled(fixedRate = 30 * 1000)
    private void refreshTokenMap(){
        //定时刷新当前系统支持的币种集合
        List<Token> tokens = tokenRepository.findAll();
        /*tokens.forEach(token -> {
            if (!TOKENS_MAP.keySet().contains(token.getAddress())) {
                TOKENS_MAP.remove(token.getAddress());
            }
            if (!TOKENS_MAP.keySet().contains(token.getChecksumAddress())) {
                TOKENS_MAP.remove(token.getChecksumAddress());
            }
            if (!TOKENS_NAME_KEY_MAP.keySet().contains(token.getName())) {
                TOKENS_NAME_KEY_MAP.remove(token.getName());
            }
        });*/
        TOKENS_MAP.clear();
        TOKENS_NAME_KEY_MAP.clear();

        tokens.forEach(token -> {
            TOKENS_MAP.put(token.getAddress(), token);
            TOKENS_MAP.put(token.getChecksumAddress(), token);
            TOKENS_NAME_KEY_MAP.put(token.getName(), token);
        });


       /* //每30秒获取汇率
        RATE_LIST = transactionService.getRate();
        //获取配置中汇率
        if (!StringUtils.isBlank(moneyRateStr)) {
            List<String> moneyRateStrList = Arrays.asList(moneyRateStr.split(";"));
            moneyRateStrList.forEach(s -> {
                String name = s.split(",")[0];
                Double rate = Double.parseDouble(s.split(",")[1]);
                Rate rateEntity = new Rate();
                rateEntity.setName(name);
                rateEntity.setRate(rate);
                RATE_LIST.add(rateEntity);
            });
        }*/
    }


    @Value("${third.exchange.name:feixiaohao}")
    private String exchangeName;
    /**
     * 保存代币价格数据，30秒请求一次数据放入缓存，每10分钟保存一次数据库。
     * @author huangxiaolin
     * @date 2018-05-17 18:54
     */
    @Scheduled(fixedRate = 30000)
    private void requestTokenData() {
        //最后从第三方请求数据
        Map<String, Object> param = new HashMap<>(2);
        param.put("trade_market", Arrays.asList(exchangeName));
        param.put("symbol_str", thirdService.getSupportTokenParam());
        param.put("cache", false);
        JSONObject jsonObject = httpService.postJSONForResult(Urls.EXCHANGE_PRICE_URL,
                null, param, true);
        if (jsonObject.getInt(Constant.CODE_KEY) == Constant.SUCCESS_CODE) {
            JSONArray jsonArray = jsonObject.getJSONArray(Constant.MESSAGE_KEY);
            int size = (jsonArray == null) ? 0 : jsonArray.size();
            List<Map<String, Object>> dataList = new ArrayList<>(size);

            List<TokenPriceHistory> tokenList = null;
            Date now = null;
            JSONObject jsonData = null;
            double tokePrice = -1;
            String symbolStr = "";
            //是否保存到数据库,每10分钟存一次数据库 20 = 10 * 60 / 30
            tokenRequestCount++;
            boolean isSaveDB = (needInitSaveToken || tokenRequestCount == 20);
            for (int i = 0; i < size; i++) {
                try {
                    jsonData = jsonArray.getJSONObject(i);//返回的数据有时不是json格式
                } catch (JSONException ex) {
                    //logger.warn("错误的代币格式：{}", jsonArray.getString(i));
                    continue;//非json格式直接进行下次循环
                }
                String priceStr = jsonData.getString("price");
                //价格可能为空，"null"
                if (StringUtils.isEmpty(priceStr) || "null".equals(priceStr)) {
                    continue;
                }
                //redis的缓存数据
                symbolStr = jsonData.getString("symbol");
                tokePrice = Double.valueOf(priceStr);
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("symbol", symbolStr);
                dataMap.put("price", tokePrice);
                dataMap.put("trade_market", jsonData.getString("trade_market"));
                dataList.add(dataMap);

                if (isSaveDB) {
                    needInitSaveToken = false;
                    if (tokenRequestCount == 20) {
                        tokenRequestCount = 0;
                    }

                    if (tokenList == null) {
                        tokenList = new ArrayList<>();
                    }
                    if (now == null) {
                        now = new Date();
                    }
                    TokenPriceHistory tp = new TokenPriceHistory();
                    tp.setCreateTime(now);
                    tp.setTokenFrom(symbolStr.split("/")[0]);
                    tp.setTokenPrice(tokePrice);
                    tp.setTokenTo(Constant.USDT_TOKEN);
                    tp.setTimeUtc(DateUtil.parseTime(jsonData.getString("time")));
                    tp.setTradeMarket(jsonData.getString("trade_market"));
                    tokenList.add(tp);
                }
            }
            //放入redis缓存
            if (!CollectionUtils.isEmpty(dataList)) {
                String jsonStr = JSONArray.fromObject(dataList).toString();
                redisTemplate.opsForValue().set(Constant.TOKEN_THRID_KEY, jsonStr);
            }
            //批量保存数据库
            tokenPriceHistoryService.batchInsert(tokenList);
        }
    }


    /**
     * 检查并补充充值记录到数据库
     */
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private RechargeDao rechargeDao;

    @Scheduled(fixedRate = 10 * 1000)
    @Transactional(rollbackFor = Exception.class)
    void checkLostRechargeRecord(){
        Long size = redisTemplate.opsForList().size(Constant.LOST_RECHARGE_RECORD_KEY);
        if (size > 0) {
           List<String> data =  redisTemplate.opsForList().
                   range(Constant.LOST_RECHARGE_RECORD_KEY, 0, -1);
            logger.info("LOST RECHARGE RECORD:{}", data.toString());
           List<RechargeRecord> records = new ArrayList<>();
           data.forEach(s -> {
               records.add(new Gson().fromJson(s, RechargeRecord.class));
           });

           //过滤已经由节点推送被保存的充值记录
           List<String> txIds = records.stream().map(rechargeRecord -> rechargeRecord.getTxId())
                   .collect(Collectors.toList());
           List<RechargeRecord> saveRecord = rechargeDao.findByTxId(txIds);
           List<String> saveRecordTxIds = saveRecord.stream().map(rechargeRecord -> rechargeRecord.getTxId())
                   .collect(Collectors.toList());
           List<RechargeRecord> lostRecord = new ArrayList<>();
           lostRecord = records.stream().filter(rechargeRecord -> !saveRecordTxIds.contains(rechargeRecord.getTxId()))
                   .collect(Collectors.toList());


           commonDao.batchInsert(lostRecord, RechargeRecord.class);
           redisTemplate.delete(Constant.LOST_RECHARGE_RECORD_KEY);
        }
    }



    /**
     * 维护各链的最新高度
     * @return
     */
    @Autowired
    private RpcClient rpcClient;
    @Autowired
    private Web3jClient web3jClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 维护以太坊的最新高度
     * @return
     */
    @Scheduled(fixedRate = 15 * 1000)
    void updateETHBlockHeight(){
        Web3j web3 = web3jClient.getWeb3j();
        Request<?, Web3ClientVersion> request = web3.web3ClientVersion();
        request.setMethod("eth_blockNumber");
        try {
            String heightStr = request.send().getWeb3ClientVersion();
            Long height = Long.valueOf(heightStr.substring(2, heightStr.length()), 16);

            String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + CoinType.ETH.getCode();

            String ETHStrHeight = stringRedisTemplate.opsForValue().get(blockHeightKey);
            if (ETHStrHeight == null || Long.valueOf(ETHStrHeight) < height) {
                stringRedisTemplate.opsForValue().set(blockHeightKey, "" + height);
                logger.info("【ETH】最新区块高度:{}", height);
            }
        } catch (IOException e) {
            logger.info("获取以太坊最新块高度失败");
            e.printStackTrace();
        }
    }


    /**
     * 维护B链的最新高度
     * @return
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
     void updateBitCoinBlockHeight(){
        Arrays.asList(RpcClient.BTC, RpcClient.BCH).forEach(tokenName -> {
            JSONObject obj =  rpcClient.query(tokenName, "getblockcount", null);
            if (!obj.getString("error").equalsIgnoreCase("null") &&
                    obj.getString("result").equalsIgnoreCase("null")){
                logger.error(tokenName + " get last height error");
                return;
            }
            Long height = obj.getLong("result");

            Token token = tokenRepository.findByName(tokenName);
            String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + token.getCoinType();
            String bitCoinStrHeight = stringRedisTemplate.opsForValue().get(blockHeightKey);

            switch (tokenName) {
                case RpcClient.BTC:
                    if (bitCoinStrHeight == null || Long.valueOf(bitCoinStrHeight) < height){
                        stringRedisTemplate.opsForValue().set(blockHeightKey, "" + height);
                        logger.info("【BTC】最新区块高度:{}", height);
                    }
                    break;
                case RpcClient.BCH:
                    if (bitCoinStrHeight == null || Long.valueOf(bitCoinStrHeight) < height){
                        stringRedisTemplate.opsForValue().set(blockHeightKey, "" + height);
                        logger.info("【BCH】最新区块高度:{}", height);
                    }
                    break;
                default:
                    break;
            }
        });
    }

    @Scheduled(fixedRate = 2 * 60 * 1000)
    void updateZcash(){
        JSONObject obj =  rpcClient.query(RpcClient.ZCASH, "getblockcount", null);
        if (!obj.getString("error").equalsIgnoreCase("null") &&
                obj.getString("result").equalsIgnoreCase("null")){
            logger.error(RpcClient.ZCASH + " get last height error");
            return;
        }
        Long height = obj.getLong("result");
        String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + CoinType.ZEC.getCode();
        String zcashStrHeight = stringRedisTemplate.opsForValue().get(blockHeightKey);
        if (zcashStrHeight == null || Long.valueOf(zcashStrHeight) < height){
            stringRedisTemplate.opsForValue().set(blockHeightKey, "" + height);
            logger.info("【ZCASH】最新区块高度:{}", height);
        }

    }

    /**
     * 定时开启活动
     */
    @Scheduled(cron = "0 0 10-18/2 * * ?")
    public void openAcButtom(){
        String[] array = type.split(",");
        for(int i=0;i<array.length;i++){
            redisTemplate.opsForValue().set(Constant.ACTIVITY_RESERVE_NUMBER+array[i],"1");
        }
    }

    /**
     * 维护网关高度
     */
    @Scheduled(fixedRate = 5 * 1000)
    void updateGatewayBlockHeight(){
        JSONObject jsobj = httpService.httpGet(Urls.GET_GATEWAY_LAST_BLOCK, null);
        if (jsobj.getInt("code") != 200) {
            logger.info("code :{}, msg:{}", jsobj.getInt("code"), jsobj.getString("msg"));
        }
        BigInteger currentHeight = new BigInteger(jsobj.getJSONObject("data").getString("height"));

        String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + CoinType.GATEWAY.getName();
        String StrHeight = stringRedisTemplate.opsForValue().get(blockHeightKey);
        if (StrHeight == null || new BigInteger(StrHeight).compareTo(currentHeight) < 0){
            stringRedisTemplate.opsForValue().set(blockHeightKey, currentHeight.toString());
            logger.info("【GATEWAY】最新区块高度:{}", currentHeight);
        }

    }


    @Scheduled(fixedRate = 60 * 1000)
    void getPopTopUser(){

    }


}

