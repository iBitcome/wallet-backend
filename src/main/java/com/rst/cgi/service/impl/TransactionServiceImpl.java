package com.rst.cgi.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.rst.cgi.common.constant.*;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.enums.CoinType;
import com.rst.cgi.common.enums.Money;
import com.rst.cgi.common.enums.ScriptType;
import com.rst.cgi.common.utils.*;
import com.rst.cgi.conf.ExchangeConfig;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mongo.BlockRepository;
import com.rst.cgi.data.dao.mongo.TokenRepository;
import com.rst.cgi.data.dao.mongo.TransactionRepository;
import com.rst.cgi.data.dao.mysql.*;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.DexTopMarket;
import com.rst.cgi.data.dto.ContractTokenBMessage;
import com.rst.cgi.data.dto.PageRep;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.*;
import com.rst.cgi.data.entity.*;
import com.rst.cgi.service.*;
import com.rst.cgi.service.thrift.gen.pushserver.PushService;
import com.rst.thrift.export.ThriftClient;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;
import service.AddressClient;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by mtb on 2018/3/30.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final String BTC_PUSHINFO_PREFIX = "BTC_PUSHINFO";
    private static final String BCH_PUSHINFO_PREFIX = "BCH_PUSHINFO";

    private static final String BTC_PUSHINFO_HASH_LIST = "BTC_PUSHINFO_HASH_LIST";
    private static final String BCH_PUSHINFO_HASH_LIST = "BCH_PUSHINFO_HASH_LIST";

    private static final  String CONTRACT_METHOD_DEPOSIT_ETH = "depositEth";
    private static final  String CONTRACT_METHOD_DEPOSIT_TOKEN = "depositToken";
    private static final  String CONTRACT_METHOD_TOKEN_APPROVE = "approve";

    private final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    public static Integer BTC_BLOCK_HEIGHT = null;//BTC当前最新区块高度

    public static Integer BCH_BLOCK_HEIGHT = null;//BCH当前最新区块高度

    public static Map<String, DexTopMarket.DexToken> DEX_TOKEN_MAP = new HashMap<>();

    @Autowired
    private HttpService httpService;
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;
    @Autowired
    private EquipmentDao equipmentDao;
    @Autowired
    private WalletDao walletDao;
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private ThriftClient<PushService.Iface> pushServerClient;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private FlatMoneyService flatMoneyService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SpecialTimedTask timedTask;
    @Autowired
    private Hint hint;
    @Autowired
    private ETHGasService ethGasService;
    @Autowired
    private ContractTokensService contractTokensService;
    @Autowired
    private GatewayDao gatewayDao;
    @Autowired
    private ExRecordDao exRecordDao;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RpcClient rpcClient;
    @Value("${transactionStatusPatternB}")
    private String transactionStatusPatternB;
    @Value("${net.work.type:test_reg}")
    private String networkType;
    @Value("${eth.node.url}")
    private String ethNodeUrl;


    /**
     * 整理以太坊交易并推送交易结果
     * @param result
     * pushServer: type(1-交易确认, 0-交易被打包入块,4-最新区块高度,-1-交易失败)
     */
    private void push(JSONObject result){
            Integer type = result.getInteger("type");
            Integer coinTypeEth = 60;

            boolean isRechargeRecord = false;
            if (type != 4) {
                String method = result.getString("method");
                JSONObject input = result.getJSONObject("input");
                if (input != null) {
                    String inputMethod = input.getString("method");
                    if ("contractCall".equalsIgnoreCase(method) &&
                            Arrays.asList(CONTRACT_METHOD_DEPOSIT_ETH,
                                    CONTRACT_METHOD_DEPOSIT_TOKEN,
                                    CONTRACT_METHOD_TOKEN_APPROVE).contains(inputMethod)) {
                        isRechargeRecord = true;
                    }

                    if (isRechargeRecord) {
                        dealRechargeRecord(result);
                    }
                }
            }


            if (type == 3 || type == 4) {
                return;
            } else {
//                logger.info("redisListener:{}", result);
                //将地址转换成公钥hash
                result.put("from", AddressClient.addressToHash(coinTypeEth,result.getString("from"), networkType));
                result.put("to", AddressClient.addressToHash(coinTypeEth,result.getString("to"), networkType));


                List<Equipment> fromEm = equipmentDao.queryByWalletAddress(result.getString("from"));
                List<Equipment> fromEmHd = equipmentDao.queryHdByWalletAddress(result.getString("from"));
                fromEm.addAll(fromEmHd);
                HashSet fromEmSet = new HashSet<>(fromEm);
                fromEm.clear();
                fromEm.addAll(fromEmSet);


                List<Equipment> toEm = equipmentDao.queryByWalletAddress(result.getString("to"));
                List<Equipment> toEmHd = equipmentDao.queryHdByWalletAddress(result.getString("to"));
                toEm.addAll(toEmHd);
                HashSet toEmSet = new HashSet<>(toEm);
                toEm.clear();
                toEm.addAll(toEmSet);

                toEm.forEach(equipment -> {
                    if (fromEm.contains(equipment)) {
                        fromEm.remove(equipment);
                    }
                });

                String msg = "";
                String msgType= "";
                SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String now = spf.format(new Date());

                if (isRechargeRecord) {
                    if (type == 1) {
                        msgType = Constant.RECHARGE_WITHDRA;
                    }
                } else {
                    if (type == 2) {
                        msg = hint.TAT_PACKED.pushMsg(hint.getType()).replace("#TIME", now);
                        msgType = Constant.TRANSACTION_PACKED;
                    } else if (type == 1){
                        Boolean success = result.getBoolean("success");
                        if (success) {
                            msg = hint.TAT_CONFIRMED.pushMsg(hint.getType()).replace("#TIME", now);
                            msgType = Constant.TRANSACTION_CONFIRM;
                        } else {
                            msg = hint.TAT_Fail.pushMsg(hint.getType()).replace("#TIME", now);
                            msgType = Constant.TRANSACTION_FAIL;
                        }
                    }
                }


                //推送消息
                List<PushRelation> relations = new ArrayList<>();
                PushRelation fromRelation = new PushRelation();
                fromRelation.setAddress(result.getString("from"));
                fromRelation.setEquipments(fromEm);
                relations.add(fromRelation);

                PushRelation toRelation = new PushRelation();
                toRelation.setAddress(result.getString("to"));
                toRelation.setEquipments(toEm);
                relations.add(toRelation);

                String txHash = result.getString("hash");

                doPush(0, txHash, relations, msg, msgType);
            }

        }

    @Autowired
    private RechargeDao rechargeDao;
    @Autowired
    private TokenRepository tokenRepository;
    /**
     * 处理以太坊充值
     * @param json
     */
    public void dealRechargeRecord(JSONObject json) {
        BlockChainServiceImpl.ETHMessage message =
                new Gson().fromJson(json.toString(), BlockChainServiceImpl.ETHMessage.class);

        String ETHStrHeight = redisTemplate.opsForValue().
                get(Constant.BLOCK_CURRENT_HEIGHT + CoinType.ETH.getCode());
        Long ETHHeight = Long.valueOf(ETHStrHeight);
        if (message.getHeight() != null &&
                ETHHeight < message.getHeight().longValue()) {
            timedTask.updateETHBlockHeight();
        }

        String inputMethod = (String) message.getInputData().get("method");

        RechargeRecord find = new RechargeRecord();
        find.setTxId(message.getTxId());
        List<RechargeRecord> recordListDB = commonDao.queryBy(find);

        Integer status = 0;
        if (json.getInteger("type") == 1) {
            if (json.getBoolean("success")) {
                status = 1;
            } else {
                status = -1;
            }

        }

        logger.info("RechargeRecord:{},recordListDB:{}",json,recordListDB);

        if (recordListDB.isEmpty()) {
            NumberFormat nfm = NumberFormat.getInstance();
            nfm.setGroupingUsed(false);

            String strTimeStamp = String.valueOf(message.getTimestamp());
            Long timestamp = message.getTimestamp();
            if (strTimeStamp.length() > 13) {
                 timestamp = Long.valueOf(strTimeStamp.substring(0, 13));
            }
            RechargeRecord rechargeRecord = new RechargeRecord();
            rechargeRecord.setRechargeTime(timestamp);
            rechargeRecord.setStatus(status);
            rechargeRecord.setCreateTime(System.currentTimeMillis());
            rechargeRecord.setType(1);
            rechargeRecord.setHeight(message.getHeight());
            rechargeRecord.setTxId(message.getTxId());
            rechargeRecord.setTraderAddress(message.getFrom());
            rechargeRecord.setChannel(RechargeRecord.RECORD_CHANNEL_OTHER);

            if (CONTRACT_METHOD_DEPOSIT_ETH.equalsIgnoreCase(inputMethod)){
                String value = nfm.format(message.getValue());
                rechargeRecord.setValue(value);
                rechargeRecord.setTokenName(CoinType.ETH.getName());
            } else if (CONTRACT_METHOD_DEPOSIT_TOKEN.equalsIgnoreCase(inputMethod)){
                Double valueDb = (Double)message.getInputData().get("originalAmount");
                String value = nfm.format(valueDb);
                Double tokenCodeDb = (Double) message.getInputData().get("tokenCode");
                Integer tokenCode = tokenCodeDb.intValue();
                rechargeRecord.setValue(value);
                rechargeRecord.setTokenName(getDexTopTokenName(tokenCode.toString()));
            } else if (CONTRACT_METHOD_TOKEN_APPROVE.equalsIgnoreCase(inputMethod)) {
                rechargeRecord.setType(3);
                Token token;
                if (!org.springframework.util.StringUtils.isEmpty(message.getContract())) {
                    token = tokenRepository.findByAddress(message.getContract().toLowerCase());
                    if (token == null) {
                        logger.info("数据库中未找到合约{}对应的token",message.getContract());
                        return;
                    }
                } else {
                    token = tokenRepository.findByName("ETH");
                    if (token == null) {
                        logger.info("数据库中未找到ETH对应的token");
                        return;
                    }
                }

                if (message.getInputData().containsKey("_value")) {
                    Double valueDb = (Double)message.getInputData().get("_value");
                    String value = nfm.format(valueDb);
                    rechargeRecord.setValue(value);
                }

                rechargeRecord.setTokenName(token.getName());
            }

            try {
                commonDao.insert(rechargeRecord);
            } catch (DuplicateKeyException e) {
                logger.warn("insert recharge_record 主键重复（忽略）");
            }
        } else {
            int type = json.getInteger("type");

            if (type == 1) {
                rechargeDao.updateStatusByTxId(message.getTxId(), System.currentTimeMillis(),
                        status, message.getFrom(), message.getHeight());
            } else if (type == 2) {
                rechargeDao.updateHeightByTxId(message.getTxId(), System.currentTimeMillis(),
                        message.getHeight(), message.getFrom());
            }
        }

    }


    @Override
    public Map<String, Long> getGasLimit(String fromAddress, String toAddress, String tokenName) {
        Map<String, Long>  rst = new HashMap<>();
        Long gasLimit = 0l;
        if (StringUtils.isBlank(tokenName)) {
            CustomException.response(Error.TOKEN_NOT_EMPTY);
        } else if (!SpecialTimedTask.TOKENS_NAME_KEY_MAP.containsKey(tokenName.toUpperCase())) {
            CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT);
        }

        Token token = SpecialTimedTask.TOKENS_NAME_KEY_MAP.get(tokenName.toUpperCase());

        if (CoinType.ETH.getName().equalsIgnoreCase(tokenName)) {
            gasLimit = ethGasService.getETHGasLimit(fromAddress, toAddress);
        } else  {
            gasLimit = ethGasService.getTokenGasLimit(fromAddress, toAddress, token.getChecksumAddress());
        }
        rst.put("gasLimit", gasLimit);
        return rst;
    }




    private static final String WHC_PAYLOAD_PREFIX = "6a";
    private static final String WHC_PAYLOAD_MIDDLE = "08776863";
    private static final String WHC_BURN_PAYLOAD_METHOD = "whc_createpayload_burnbch";
    private static final String WHC_SEND_PAYLOAD_METHOD = "whc_createpayload_simplesend";

    private static final String USDT_PAYLOAD_PREFIX = "6a";
    private static final String USDT_PAYLOAD_MIDDLE = "6f6d6e69";
    private static final String USDT_SEND_PAYLOAD_METHOD = "omni_createpayload_simplesend";

    @Override
    public Map<String, String> getPayload(GetPayloadReqDTO body) {
        Map<String, String> rest = new HashMap<>();
        String payload = "";
        GetPayloadReqDTO.PayloadSendToken payloadSendToken = body.getPayloadSendToken();

//        if (CoinType.WHC.getName().equals(payloadSendToken.getTokenName())) {
//            if (GetPayloadReqDTO.BURN_PAYLOAD.equals(body.getPayloadType())) {
//                payload = getBurnPayload(payloadSendToken.getTokenName(),
//                        WHC_BURN_PAYLOAD_METHOD,
//                        null,
//                        WHC_PAYLOAD_MIDDLE,
//                        WHC_PAYLOAD_PREFIX
//                );
//            } else if (GetPayloadReqDTO.SEND_PAYLOAD.equals(body.getPayloadType())){
//                payload = getSendPayload(payloadSendToken.getTokenName(),
//                        payloadSendToken.getValue(),
//                        WHC_SEND_PAYLOAD_METHOD,
//                        WHC_PAYLOAD_MIDDLE,
//                        WHC_PAYLOAD_PREFIX);
//            }
//        } else if (CoinType.usdt.getName().equals(payloadSendToken.getTokenName())) {
//            payload = getSendPayload(payloadSendToken.getTokenName(),
//                    payloadSendToken.getValue(),
//                    USDT_SEND_PAYLOAD_METHOD,
//                    USDT_PAYLOAD_MIDDLE,
//                    USDT_PAYLOAD_PREFIX);
//        }

        if (GetPayloadReqDTO.BURN_PAYLOAD.equals(body.getPayloadType())) {
            payload = getBurnPayload(payloadSendToken.getTokenName(),
                    WHC_BURN_PAYLOAD_METHOD,
                    null,
                    WHC_PAYLOAD_MIDDLE,
                    WHC_PAYLOAD_PREFIX
            );
        } else if (GetPayloadReqDTO.SEND_PAYLOAD.equals(body.getPayloadType())){
            if (CoinType.WHC.getName().equals(payloadSendToken.getTokenName())) {
                payload = getSendPayload(payloadSendToken.getTokenName(),
                        payloadSendToken.getValue(),
                        WHC_SEND_PAYLOAD_METHOD,
                        WHC_PAYLOAD_MIDDLE,
                        WHC_PAYLOAD_PREFIX);
            } else if (CoinType.usdt.getName().equals(payloadSendToken.getTokenName())) {
                payload = getSendPayload(payloadSendToken.getTokenName(),
                        payloadSendToken.getValue(),
                        USDT_SEND_PAYLOAD_METHOD,
                        USDT_PAYLOAD_MIDDLE,
                        USDT_PAYLOAD_PREFIX);
            }
        } else if (GetPayloadReqDTO.GATE_WAY_PAYLOAD.equals(body.getPayloadType())) {
            GetPayloadReqDTO.PayloadExToken payloadExToken = body.getPayloadExToken();
            payload = getGateWayPayload(payloadExToken.getFromToken(),
                    payloadExToken.getToToken(),
                    payloadExToken.getAddress());
        }

        rest.put("payload", payload);

        return rest;
    }

    /**
     * 获取燃烧的payload并组装成op_return
     * @return
     */
    private String getBurnPayload(String tokenName,
                                  String method,
                                  net.sf.json.JSONArray param,
                                  String payloadMiddle,
                                  String payloadPrefix) {
        net.sf.json.JSONObject rst  = rpcClient.query(tokenName,
                method, param);
        logger.info(tokenName + " RPC BURN PAYLOAD RESULT:{}", rst);
        checkRpcResult(rst);
        String data = rst.getString("result");
        String content = payloadMiddle + data;
        String contentLenth =  Integer.toHexString (content.length()/2);
        contentLenth = contentLenth.length() < 2 ? "0" + contentLenth : contentLenth;
        return payloadPrefix + contentLenth + content;
    }

    /**
     * 获取交易payload并组装成op_return
     * @param tokenName
     * @param method
     * @return
     */
    private String getSendPayload(String tokenName,
                                  String value,
                                  String method,
                                  String payloadMiddle,
                                  String payloadPrefix) {
        Token token = tokenRepository.findByName(tokenName);
        if (Objects.isNull(token)) {
            CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT);
        }

        BigDecimal divisor = new BigDecimal(value);
        BigDecimal dividend = new BigDecimal(Math.pow(10, token.getDecimal()));
        BigDecimal bigValue = divisor.divide(dividend);

        net.sf.json.JSONArray param = new net.sf.json.JSONArray();
        param.add(token.getOwnerTokenId());
        param.add(bigValue.toPlainString());

        net.sf.json.JSONObject rst  = rpcClient.query(tokenName,
                method, param);
        logger.info(tokenName + "RPC SEND PAYLOAD RESULT:{}", rst);
        checkRpcResult(rst);
        String data = rst.getString("result");
        String content = payloadMiddle + data;
        String contentLenth =  Integer.toHexString (content.length()/2);
        contentLenth = contentLenth.length() < 2 ? "0" + contentLenth : contentLenth;
        return payloadPrefix + contentLenth + content;
    }


    void checkRpcResult (net.sf.json.JSONObject jsonObject) {
        if (jsonObject.containsKey("error") && jsonObject.getString("error") != "null") {
            net.sf.json.JSONObject error = jsonObject.getJSONObject("error");
            CustomException.response(-1, error.getString("message"));
        }
    }


    private String getGateWayPayload(String fromTokenName, String toTokenName, String address){
        Token fromToken = tokenRepository.findByName(fromTokenName);
        Token toToken = tokenRepository.findByName(toTokenName);
        if (fromToken == null) {
            CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT.getCode(),
                    fromTokenName + Error.ERR_MSG_TOKEN_NOT_SUPPORT.getMsg());
        }
        if (toToken == null) {
            CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT.getCode(),
                    toTokenName + Error.ERR_MSG_TOKEN_NOT_SUPPORT.getMsg());
        }

        net.sf.json.JSONObject param = new net.sf.json.JSONObject();
        param.put("fromchain", fromToken.getChainName());
        param.put("tochain", toToken.getChainName());
        param.put("addr", address);
        param.put("app", getGateWayAppId(toToken));
        net.sf.json.JSONObject rst =
                httpService.httpGet(Urls.GET_GATEWAY_PAYLOADE, param);
        if (rst.getInt("code") != 200) {
            CustomException.response(-1, rst.getString("data"));
        }

        return  rst.getJSONObject("data").getString("payload");
    }

    /**
     * 从网关获取erc20的对应币种的appid
     * @param token
     * @return
     */
    private Integer getGateWayAppId(Token token) {
        if (token.getAppId() != null) {
            return token.getAppId();
        } else {
            net.sf.json.JSONObject param = new net.sf.json.JSONObject();
            param.put("chain", token.getChainName());
            param.put("contractaddr", token.getChecksumAddress());

            net.sf.json.JSONObject rst = httpService.httpGet(Urls.GET_GATEWAY_TOKEN_REG, param);
            if (rst.getInt("code") != 200) {
                CustomException.response(-1, rst.getString("msg"));
            }
            Integer tokenCode = rst.getJSONObject("data").getInt("token_id");
            if (tokenCode == 0) {
//                CustomException.response(Error.ERR_MSG_GATEWAY_NOT_SUPPROT.getCode(),
//                        Error.ERR_MSG_GATEWAY_NOT_SUPPROT.getMsg() + token.getName());
                CustomException.response(Error.ERR_MSG_GATEWAY_NOT_SUPPROT.format(token.getName()));
            }

            ThreadUtil.runOnOtherThread(() ->{
                token.setAppId(tokenCode);
                tokenRepository.save(token);
            });
            return tokenCode;
        }

    }

    @Autowired
    private MarketCacheService marketCacheService;
    /**
     * 通过dexTop的tokenCode获取TokenName
     * @param code
     * @return
     */
    private String getDexTopTokenName(String code) {
        DexTopMarket.DexToken dexToken = DEX_TOKEN_MAP.get(code);
        if (dexToken == null) {
            DexTopMarket dexTopMarket = (DexTopMarket)marketCacheService.
                    getConfig(ExchangeConfig.DEX_TOP.getName());
            if (dexTopMarket != null && dexTopMarket.getConfig() != null) {
                List<DexTopMarket.DexToken> cashTokens = dexTopMarket.getConfig().getCashTokens();
                List<DexTopMarket.DexToken> stockTokens = dexTopMarket.getConfig().getStockTokens();
                List<DexTopMarket.DexToken> disabledTokens = dexTopMarket.getConfig().getDisabledTokens();
                if (cashTokens != null) {
                    cashTokens.forEach(token -> {
                        DEX_TOKEN_MAP.put(token.getTokenCode()+ "", token);
                        DEX_TOKEN_MAP.put(token.getTokenId(), token);
                    });
                }
                if (stockTokens != null) {
                    stockTokens.forEach(token -> {
                        DEX_TOKEN_MAP.put(token.getTokenCode()+ "", token);
                        DEX_TOKEN_MAP.put(token.getTokenId(), token);
                    });
                }
                if (disabledTokens != null) {
                    disabledTokens.forEach(token -> {
                        DEX_TOKEN_MAP.put(token.getTokenCode()+ "", token);
                        DEX_TOKEN_MAP.put(token.getTokenId(), token);
                    });
                }


                DexTopMarket.DexToken dexTokenSecond = DEX_TOKEN_MAP.get(code);
                if (dexTokenSecond == null) {
                    return code;
                } else {
                    return dexTokenSecond.getTokenId();
                }
            } else {
                return code;
            }
        } else {
            return dexToken.getTokenId();
        }
    }


    /**
     * 整理bit交易并进行第一次推送
     * @param result
     *
     */
    @Transactional(rollbackFor = Exception.class)
    public void cacheBitTranx(JSONObject result){
        Token token = SpecialTimedTask.TOKENS_NAME_KEY_MAP.
                get(result.getString("cointype").toUpperCase());
        if (token == null) {
            return;
        }
        Integer coinType = token.getCoinType();
        String txHash = result.getString("txid");


        //更新当前最新区块高度,并对所待确认交易进行检查和推送
        if ("BCH".equalsIgnoreCase(result.getString("cointype"))) {
            if (BCH_BLOCK_HEIGHT < result.getIntValue("height")) {
                BCH_BLOCK_HEIGHT = result.getIntValue("height");
                logger.info("BCH最新区块高度:{}", BCH_BLOCK_HEIGHT);
                this.pushBit(coinType);
            }
        } else if ("BTC".equalsIgnoreCase(result.getString("cointype"))) {
            if (BTC_BLOCK_HEIGHT < result.getIntValue("height")) {
                BTC_BLOCK_HEIGHT = result.getIntValue("height");
                logger.info("BTC最新区块高度:{}", BTC_BLOCK_HEIGHT);
                this.pushBit(coinType);
            }
        }



        //判断现存的BTC与BCH的PUSHINFO是否存在
        //交易记录链上会在panding和打包入块时都会推送以防漏推送，但是在如果已经推送过一次后，
        //业务不再进行处理
        if ("BTC".equalsIgnoreCase(result.getString("cointype"))) {
            if (StringUtils.isNotBlank(redisTemplate.opsForValue().get(BTC_PUSHINFO_PREFIX + txHash))) {
                return;
            }
        }else {
            if (StringUtils.isNotBlank(redisTemplate.opsForValue().get(BCH_PUSHINFO_PREFIX + txHash))) {
                return;
            }
        }


        /*//现存的BTC与BCH的PUSHINFO_HASH_LIST索引集合中是否有该交易的索引
        //交易记录链上会在panding和打包入块时都会推送以防漏推送，但是在如果已经推送过一次后，
        //业务不再进行处理
        if ("BTC".equalsIgnoreCase(result.getString("cointype"))) {
            for (int i = 0; i<redisTemplate.opsForList().size(BTC_PUSHINFO_HASH_LIST); i++) {
                if (redisTemplate.opsForList().index(BTC_PUSHINFO_HASH_LIST, i).
                        equalsIgnoreCase( BTC_PUSHINFO_PREFIX+ txHash)){
                    return;
                }

            }
        } else {
            for (int i = 0; i<redisTemplate.opsForList().size(BCH_PUSHINFO_HASH_LIST); i++) {
                if (redisTemplate.opsForList().index(BCH_PUSHINFO_HASH_LIST, i).
                        equalsIgnoreCase(BCH_PUSHINFO_PREFIX + txHash)){
                    return;
                }

            }
        }*/


        List<String> vinList = new ArrayList<>();
        List<String> voutList = new ArrayList<>();

        JSONArray voutArray = new JSONArray();
        JSONArray vinArray = new JSONArray();
        if (result.getString("vinaddress") != null) {
            //防止是一个basePay交易，basePay没有输入信息
            vinArray = result.getJSONArray("vinaddress");
        }
        if (result.getString("voutaddress") != null) {
            voutArray = result.getJSONArray("voutaddress");
        }

        //地址转换成公钥Hash
        for (int i = 0; i < vinArray.size(); i++) {
                vinList.add(AddressClient.addressToHash(coinType,vinArray.getString(i), networkType));
            }

        for (int i = 0; i < voutArray.size(); i++) {
            voutList.add(AddressClient.addressToHash(coinType, voutArray.getString(i), networkType));
        }

        //查询出输入地址与输出地址对应的hd钱包公钥hash
        Set<String> vinPukHashSet = new HashSet<>();
        Set<String> voutPukHashSet = new HashSet<>();
        //防止地址列表数量过度造成sql异常，进行分批查询,每次查询300条
        for (int i = 0; i < vinList.size(); i += 300) {
            List<String> queryList = new ArrayList<>();
            Integer endIndex = i + 300;
            endIndex = endIndex > vinList.size() ? vinList.size() : endIndex;
            queryList = vinList.subList(i, endIndex);
            if (!queryList.isEmpty()){
                vinPukHashSet.addAll(walletDao.queryPbkByAddress(queryList));
            }
        }
        for (int i = 0; i < voutList.size(); i += 300) {
            Integer endIndex = i + 300;
            List<String> queryList = new ArrayList<>();
            endIndex = endIndex > voutList.size() ? voutArray.size() : endIndex;
            queryList = voutList.subList(i, endIndex);
            if (!queryList.isEmpty()) {
                voutPukHashSet.addAll(walletDao.queryPbkByAddress(queryList));
            }

        }

        Set<String> pushPubkHash = new HashSet<>();
        pushPubkHash.addAll(vinPukHashSet);
        pushPubkHash.addAll(voutPukHashSet);

        //将推送信息整理到pushInfo中
        JSONObject pushInfo = new JSONObject();
        List<PushRelation> relations = new ArrayList<>();
        Iterator<String> pushPubkIterator = pushPubkHash.iterator();
        while (pushPubkIterator.hasNext()) {
            String pubk = pushPubkIterator.next();
            PushRelation relation = new PushRelation();
            relation.setAddress(pubk);
            List<Equipment> equipments = equipmentDao.queryByPuk(pubk);
            if (Objects.isNull(equipments) || equipments.size() == 0) {
                logger.info(pushPubkHash.iterator().next() + " 对应的设备不存在");
                continue;
            }
            relation.setEquipments(equipments);
            relations.add(relation);
        }

        //系统中输入与输出都没有信息，则直接返回
        if (relations.isEmpty()){
            return;
        }
        logger.info("已过滤推送进入:{}",result);

        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = spf.format(new Date());

        pushInfo.put("txid", txHash);
        pushInfo.put("relation", relations);
//        doPush(1, txHash, relations,
//                Hint.TAT_PACKED.replace("#TIME", now),
//                "3");//第一次交易中状态推送


        //保存交易的索引到索引集合
        //推送对象保存到redis中
        if (coinType == 0) {
            redisTemplate.opsForList().leftPush(BTC_PUSHINFO_HASH_LIST, BTC_PUSHINFO_PREFIX + txHash);
            redisTemplate.opsForValue().set(BTC_PUSHINFO_PREFIX + txHash,
                    pushInfo.toString(), 24, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForList().leftPush(BCH_PUSHINFO_HASH_LIST, BCH_PUSHINFO_PREFIX + txHash);
            redisTemplate.opsForValue().set(BCH_PUSHINFO_PREFIX + txHash,
                    pushInfo.toString(), 24, TimeUnit.HOURS);
        }

    }


    @Value("${confirm.num:6}")
    private Integer confirmNum;
    @Transactional(rollbackFor = Exception.class)
    public void pushBit(Integer coinType){
        List<String> waitPushInfoKeys = new ArrayList<>();
//        Token token = SpecialTimedTask.TOKENS_COINTYPE_KEY_MAP.get(coinType);
        Token token = SpecialTimedTask.TOKENS_NAME_KEY_MAP.get("");
        logger.info("开始推送{}",token.getName());

        String pushInfoHashListType;
        switch (coinType) {
            case 0:
                pushInfoHashListType = BTC_PUSHINFO_HASH_LIST;
                break;
            case 145:
                pushInfoHashListType = BCH_PUSHINFO_HASH_LIST;
                break;
            default:
                return;
        }

        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = spf.format(new Date());
        long waitPushListSize = redisTemplate.opsForList().size(pushInfoHashListType);
        for (long i = 0; i < waitPushListSize; i++) {
            String pushInfoKey = redisTemplate.opsForList().rightPop(pushInfoHashListType);
            if (StringUtils.isBlank(redisTemplate.opsForValue().get(pushInfoKey))) {
                continue;
            }
            JSONObject pushInfo = JSONObject.parseObject(redisTemplate.opsForValue().get(pushInfoKey));

            //进行PushRelation列表进行组装
            JSONArray relationArray = pushInfo.getJSONArray("relation");
            List<PushRelation> relations = new ArrayList<>();
            for (int j = 0; j < relationArray.size(); j++){
                JSONArray equipmentArray = relationArray.getJSONObject(j).getJSONArray("equipments");
                List<Equipment> equipments = new ArrayList<>();
                for (int k = 0; k < equipmentArray.size(); k++) {
                    Equipment equipment = new Equipment();
                    equipment.setEquipmentNo(equipmentArray.getJSONObject(k).getString("equipmentNo"));
                    equipments.add(equipment);
                }
                PushRelation pushRelation = new PushRelation();
                pushRelation.setAddress(relationArray.getJSONObject(j).getString("address"));
                pushRelation.setEquipments(equipments);
                relations.add(pushRelation);
            }

            String txId = pushInfo.getString("txid");

            //查询当前这笔交易在链上是否存在，并判断确认块的数量
            JSONArray getTranxOnChainParam = new JSONArray();
            getTranxOnChainParam.add(pushInfo.getString("txid"));
            getTranxOnChainParam.add(true);
            JSONObject rst = JSONObject.parseObject(
                    rpcClient.query(token.getName(), "getrawtransaction", getTranxOnChainParam).toString()
            );
            if (rst.containsKey("code") &&  rst.getIntValue("code") == -1 &&
                    rst.getString("msg").contains("请求提交失败")) {
                CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
            } else if (rst.getString("result").equalsIgnoreCase("null") ||
                    rst.getJSONObject("result").getIntValue("confirmations") == 0){
                //交易失败推送
                doPush(1,txId, relations,
                        hint.TAT_Fail.pushMsg(hint.getType()).replace("#TIME", now)
                        ,"-1");
                redisTemplate.delete(pushInfoKey);
            } else {
                JSONObject data = rst.getJSONObject("result");
                //比较已确认块的数量和自定义的完成交易值的大小判断是否已经完成交易
                if (data.getIntValue("confirmations") >= confirmNum) {
                    //推送交易成功信息
                    doPush(1,txId, relations,
                            hint.TAT_CONFIRMED.pushMsg(hint.getType()).replace("#TIME", now)
                            ,"1");
                    redisTemplate.delete(pushInfoKey);
                } else {
                    //收集仍未确认的是交易
                    waitPushInfoKeys.add(pushInfoKey);
                }

            }
        }
        if (!waitPushInfoKeys.isEmpty()) {
            redisTemplate.opsForList().leftPushAll(pushInfoHashListType, waitPushInfoKeys);
        }
    }


    /**
     *
     * @param isLot 是否是批量推送(1:是，0:否)--针对每个钱包的多个设备的推送
     * @param relations 关系信息
     * @param msg 推送消息
     * @param msgType type(1-交易确认, 2-交易被打包入块（交易中）,1-交易失败)
     * @param txHash 交易hash
     */
    private void doPush(Integer isLot,
                        String txHash,
                        List<PushRelation> relations,
                        String msg,
                        String msgType){

        if (isLot == 0) { //单个推送
            relations.forEach(pushRelation -> {
                pushRelation.getEquipments().forEach(equipment -> {
                    try {
                        logger.info("push to:{},fromEquipmentNo:{},msg:{}",
                                pushRelation.getAddress() ,equipment.getEquipmentNo(), msg);
                        pushServerClient.get(PushService.Iface.class).
                                push(txHash, equipment.getEquipmentNo(), msg, msgType,3);
                    } catch (TException e) {
                        logger.error("push fromEquipment error");
                        e.printStackTrace();
                    }
                });
            });

        } else if (isLot == 1){
            relations.forEach(pushRelation -> {
                List<String> eqmNo = new ArrayList<>();
                pushRelation.getEquipments().forEach(equipment -> {
                    eqmNo.add(equipment.getEquipmentNo());
                });
                try {
                    logger.info("push to:{},fromEquipmentNo:{},msg:{}",
                            pushRelation.getAddress(), eqmNo, msg);
                    pushServerClient.get(PushService.Iface.class).
                            pushList(txHash, eqmNo, msg, msgType, 3);
                } catch (TException e) {
                    logger.error("push fromEquipment error");
                    e.printStackTrace();
                }
            });


        }


    }



    @Value("${eth.confirm.num:12}")
    private Long ETH_TARGET_CONFIRM_NUM;
     @Value("${eth.web.url:https://kovan.etherscan.io/tx/}")
    private String ETH_WEB_URL;
    @Override
    public CommonPage<GetAllTransactionRepDTO> getAllTransaction(GetAllTransactionReqDTO body) {
        CommonPage<GetAllTransactionRepDTO> commonPage = new CommonPage<>();
        if (body.getWalletAddressList().isEmpty()) {
            return commonPage;
        }
        if (!SpecialTimedTask.TOKENS_NAME_KEY_MAP.
                containsKey(body.getTokenType())) {
            CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT);
        }

        List<String> ethAddress = new ArrayList<>();
        body.getWalletAddressList().forEach(address -> {
            //对eth公钥Hash进行地址转换
            if (address.getCoinType() == 60) {
                ethAddress.add(AddressClient.toEtherAddressByHash(address.getPubkHash()));
            }
        });

        List<GetAllTransactionRepDTO> repDTOList = new ArrayList<>();



        String dataTimeType;
        Integer pageNo = body.getPageNo();
        Integer pageSize = body.getPageSize();

        if ("current".equalsIgnoreCase(body.getTimeType())) {
            PageRequest pageRequest = new PageRequest
                    (body.getPageNo()-1, body.getPageSize(), new Sort(Sort.Direction.DESC, "type","timestamp","tranxIndex"));
            Page<Transaction> page = new PageImpl<>(new ArrayList<>());

            String method = "transaction";
            List<String> conntractMethod = Arrays.asList("transfer", "transferFrom");
            if (CoinType.ETH.getName().equalsIgnoreCase(body.getTokenType())) {
                if (Objects.isNull(body.getTransType())) {
                    page = transactionRepository.findAllByFromInAndMethodOrToInAndMethod(ethAddress,
                            method, ethAddress, method, pageRequest);
                } else if (body.getTransType() == -1) {
                    page = transactionRepository.findAllByFromInAndMethod(ethAddress, method, pageRequest);
                } else if (body.getTransType() == 1) {
                    page = transactionRepository.findAllByToInAndMethod(ethAddress, method, pageRequest);
                }
            } else {
                Token queryToken = SpecialTimedTask.TOKENS_NAME_KEY_MAP.get(body.getTokenType());
                String contranct = queryToken.getChecksumAddress();
                if (Objects.isNull(body.getTransType())) {
//                    page = transactionRepository.findAllByFromInAndContractOrToInAndContract(ethAddress,
//                            contranct,ethAddress, contranct, pageRequest);
                    page = transactionRepository.findAllByFromInOrToInAndContractAndInput_MethodIn(ethAddress,
                            ethAddress, contranct,conntractMethod, pageRequest);
                } else if (body.getTransType() == -1) {
                    page = transactionRepository.findAllByFromInAndContractAndInput_MethodIn(ethAddress,
                            contranct, conntractMethod, pageRequest);
                } else if (body.getTransType() == 1) {
                    page = transactionRepository.findAllByToInAndContractAndInput_MethodIn(ethAddress, contranct,
                            conntractMethod, pageRequest);
                }
            }

            List<Transaction> transactions = page.getContent();
            dataTimeType = "current";

            String ETHStrHeight = redisTemplate.opsForValue().
                    get(Constant.BLOCK_CURRENT_HEIGHT + CoinType.ETH.getCode());
            Long ETHHeight = Long.valueOf(ETHStrHeight);

            //放入返回类中
            for (int i = 0; i<transactions.size(); i++) {
                Integer height = transactions.get(i).getHeight();
                if (height != null && ETHHeight < height.longValue()) {
                    timedTask.updateETHBlockHeight();
                }

                Transaction transaction = transactions.get(i);
                if ("contractCall".equalsIgnoreCase(transaction.getMethod())) {
                    //如果代币类型不支持，就不返回改交易
                    if (!SpecialTimedTask.TOKENS_MAP.containsKey(transaction.getContract())) {
                        continue;
                    }

//                    Transaction.TransactionInput input = transaction.getInput();
//                    if (input.getMethod() != null &&
//                            CONTRACT_METHOD_TOKEN_APPROVE.equalsIgnoreCase(input.getMethod())) {
//                        continue;
//                    }
////                    if (!CONTRACT_METHOD_TOKEN_TRANSFER.equalsIgnoreCase(transaction.getMethod()) &&
////                            !CONTRACT_METHOD_TOKEN_TRANSFER_FROM.equalsIgnoreCase(transaction.getMethod()) &&
////                            ! "transaction".equalsIgnoreCase(transaction.getMethod())) {
////                        continue;
////                    }
                }

                Integer txHeight = transactions.get(i).getHeight();
                Long confirmNums;
                String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + CoinType.ETH.getCode();
                Long currentETHHeight = Long.parseLong(redisTemplate.opsForValue().get(blockHeightKey));
                if (Objects.isNull(txHeight) || txHeight == 0) {
                    confirmNums = 0l;
                } else {
                    confirmNums = currentETHHeight - txHeight.longValue() + 1;
                    if (confirmNums < 1) {
                        confirmNums = 0l;
                    }
                }


                GetAllTransactionRepDTO repDTO = new GetAllTransactionRepDTO();
                repDTO.setMoney(transactions.get(i).getValue());
                repDTO.setFromWallet(transactions.get(i).getFrom());
                repDTO.setHash(transactions.get(i).getHash());
                repDTO.setHeight(transactions.get(i).getHeight());
                repDTO.setSuccess(true);
                repDTO.setTime(transactions.get(i).getTimestamp()/1000);
                repDTO.setToWallet(transactions.get(i).getTo());
                repDTO.setStatus(transactions.get(i).getType());
                repDTO.setContract(transactions.get(i).getContract());
                repDTO.setTxFee(transactions.get(i).getTxFee());
                repDTO.setConfirmNum(confirmNums);
                repDTO.setTargetConfirmNum(ETH_TARGET_CONFIRM_NUM);
                repDTO.setWebTxUrl(ETH_WEB_URL + transactions.get(i).getHash());

                if (transactions.get(i).getType() == 1) {
                    repDTO.setSuccess(transactions.get(i).getSuccess());
                }
                if (transactions.get(i).getFrom().equalsIgnoreCase(transactions.get(i).getTo())) {
                    repDTO.setType(0);
                }else {

                    if (ethAddress.contains(transactions.get(i).getFrom())) {
                        repDTO.setType(-1);
                    } else if (ethAddress.contains(transactions.get(i).getTo())){
                        repDTO.setType(1);
                    }
                }


                if ("contractCall".equalsIgnoreCase(transactions.get(i).getMethod())) {
                    Token token = SpecialTimedTask.TOKENS_MAP.
                            get(transactions.get(i).getContract());
                    repDTO.setTokenName(token.getName());
                    repDTO.setDecimal(token.getDecimal());
                } else if ("contractDeploy".equalsIgnoreCase(transactions.get(i).getMethod())) {
                    continue;
                } else if ("transaction".equalsIgnoreCase(transactions.get(i).getMethod())){
                    repDTO.setTokenName("ETH");
                    repDTO.setDecimal(SpecialTimedTask.TOKENS_NAME_KEY_MAP.get("ETH").getDecimal());
                }

//                TokenPrice tokenPrice = SpecialTimedTask.TOKEN_PRICE_MAP.get(repDTO.getTokenName());
//                repDTO.setPrice(tokenPrice.getPrice());
                repDTO.setPrice(flatMoneyService.tokenPriceAdaptation(repDTO.getTokenName()));

                if (repDTO.getType() == 0) {
                    repDTO.setType(1);
                    repDTOList.add(repDTO);

                    GetAllTransactionRepDTO repDTOPay = new GetAllTransactionRepDTO();
                    BeanCopier.getInstance().copyBean(repDTO, repDTOPay);
                    repDTOPay.setType(-1);
                    repDTOList.add(repDTOPay);
                } else {
                    repDTOList.add(repDTO);
                }

            }


            if (repDTOList.size() < body.getPageSize()) { //如果查询当月交易最后一页未满页，查询历史交易进行补充
                dataTimeType = "history";
                pageNo = 1;
                repDTOList.addAll(getAllHistoryTransaction(ethAddress, pageNo,
                        body.getPageSize(), body.getTransType(), body.getTokenType()));
            }

        } else { //查询历史交易
            dataTimeType = "history";
            repDTOList = getAllHistoryTransaction(ethAddress, body.getPageNo(), body.getPageSize(), body.getTransType(), body.getTokenType());
        }

        //将返回结果进行过滤，只返回需要查询的交易
        List<GetAllTransactionRepDTO> cacheList = repDTOList.stream().filter(
                getAllTransactionRepDTO ->  body.getTokenType().equalsIgnoreCase(
                        getAllTransactionRepDTO.getTokenName())
        ).collect(Collectors.toList());
        if (body.getTransType() != null) {
            cacheList = cacheList.stream().filter(
                    getAllTransactionRepDTO -> getAllTransactionRepDTO.getType().intValue()
                            == body.getTransType()
            ).collect(Collectors.toList());
        }

        pageSize = cacheList.size();
        commonPage.setContent(cacheList);
        commonPage.setDataTimeType(dataTimeType);
        commonPage.setPageNo(pageNo);
        commonPage.setPageSize(pageSize);

        return commonPage;
    }



    private List<GetAllTransactionRepDTO> getAllHistoryTransaction(List<String> walletAddressList, Integer pageNo, Integer PageSize, Integer transType, String tokenName){
        List<GetAllTransactionRepDTO> repDTOList = new ArrayList<>();
        SimpleDateFormat spf = new SimpleDateFormat("YYYY-MM");
        spf.setTimeZone(TimeZone.getTimeZone("GMT"));

        String trans_type = "all";
        if (Objects.isNull(transType)) {
            trans_type = "all";
        } else if (transType == -1) {
            trans_type = "expense";
        } else if (transType == 1) {
            trans_type = "income";
        }

        net.sf.json.JSONObject param = new net.sf.json.JSONObject();
        param.put("addr_list", walletAddressList);
        param.put("from", spf.format(new Date()));
        param.put("page_number", pageNo);
        param.put("page_size", PageSize);
        param.put("trans_type", trans_type);
        param.put("token_type", tokenName);

        JSONObject rsult = JSONObject.parseObject(
                httpService.httpPostWithJson(Urls.BSM_TRANSACTION_URL, param).toString()
        );
        logger.info("BSM RETURN HISTORY TRANSACTIONS:{}",rsult);
        if (Objects.isNull(rsult) || rsult.isEmpty()) {
            logger.error("BSM RETURN HISTORY TRANSACTIONS ERROR");
            CustomException.response(Error.ERR_MSG_SERVICE_ERROR);
        } else if (rsult.getIntValue("code") != 0) {
            if (rsult.containsKey("msg") && rsult.getString("msg").contains("请求提交失败")) {
                CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
            } else {
                CustomException.response(-1, rsult.getString("msg"));
            }
        } else {
            JSONArray data = rsult.getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject obj = data.getJSONObject(i);
                GetAllTransactionRepDTO repDTO = new GetAllTransactionRepDTO();
                Double value = null;
                String toWallet = null;
                String contract = null;
                if (obj.containsKey("tokenTransfer") && !obj.getJSONObject("tokenTransfer").isEmpty()) {
                    JSONObject tokenTransfer = obj.getJSONObject("tokenTransfer");
                    value = Double.valueOf(tokenTransfer.getString("value"));
                    toWallet = tokenTransfer.getString("to");
                    contract = obj.getString("to");
                } else {
                    value = Double.valueOf(obj.getString("value"));
                    toWallet = obj.getString("to");
                }

                //如果代币类型不支持，就不返回改交易
                if ("contractCall".equalsIgnoreCase(obj.getString("type"))) {
                    if (!SpecialTimedTask.TOKENS_MAP.containsKey(contract)) {
                        continue;
                    }
                }

                if (!StringUtils.isBlank(contract)) {
                    Token token = SpecialTimedTask.TOKENS_MAP.get(contract);
                    repDTO.setTokenName(token.getName());
                    repDTO.setDecimal(token.getDecimal());
                } else {
                    String name = "ETH";
                    repDTO.setTokenName(name);
                    repDTO.setDecimal(SpecialTimedTask.TOKENS_NAME_KEY_MAP.get(name).getDecimal());
                }

                Long txHeight = obj.getLong("blockNumber");
                Long confirmNums;
                String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + CoinType.ETH.getCode();
                Long currentETHHeight = Long.parseLong(redisTemplate.opsForValue().get(blockHeightKey));
                if (Objects.isNull(txHeight) || txHeight == 0L) {
                    confirmNums = 0L;
                } else {
                    confirmNums = currentETHHeight - txHeight + 1;
                    if (confirmNums < 1) {
                        confirmNums = 0L;
                    }
                }

                repDTO.setPrice(flatMoneyService.tokenPriceAdaptation(repDTO.getTokenName()));
                repDTO.setMoney(value);
                repDTO.setTxFee(Double.valueOf(obj.getString("txFee")));
                repDTO.setFromWallet(obj.getString("from"));
                repDTO.setHash(obj.getString("hash"));
                repDTO.setHeight(obj.getInteger("blockNumber"));
                repDTO.setTime(obj.getLong("timestamp") * 1000);
                repDTO.setToWallet(toWallet);
                repDTO.setStatus(1);
                repDTO.setContract(contract);
                repDTO.setWebTxUrl(ETH_WEB_URL + obj.getString("hash"));
                repDTO.setConfirmNum(confirmNums);
                repDTO.setTargetConfirmNum(ETH_TARGET_CONFIRM_NUM);
                if (obj.getLong("gasLimit") > obj.getLong("gasUsed")) {
                    repDTO.setSuccess(true);
                } else {
                    repDTO.setSuccess(false);
                }
                if (obj.getString("from").equalsIgnoreCase(toWallet)) {
                    repDTO.setType(0);
                }else {
//                    if (walletAddress.equalsIgnoreCase(obj.getString("from"))) {
//                        repDTO.setType(-1);
//                    } else if (walletAddress.equalsIgnoreCase(obj.getString("to"))){
//                        repDTO.setType(1);
//                    }
                    if (walletAddressList.contains(obj.getString("from"))) {
                        repDTO.setType(-1);
                    } else if (walletAddressList.contains(toWallet)){
                        repDTO.setType(1);
                    }
                }

                if (repDTO.getType() == 0) {
                    repDTO.setType(1);
                    repDTOList.add(repDTO);

                    GetAllTransactionRepDTO repDTOPay = new GetAllTransactionRepDTO();
                    BeanCopier.getInstance().copyBean(repDTO, repDTOPay);
                    repDTOPay.setType(-1);
                    repDTOList.add(repDTOPay);
                }else {
                    repDTOList.add(repDTO);
                }
            }
        }

        return repDTOList;
    }


    @Value("${usdt.confirm.num:1}")
    private Long USDT_CONFIRM_NUM;
    @Value("${whc.confirm.num:1}")
    private Long WHC_CONFIRM_NUM;
    @Override
    public CommonPage<GetAllTransactionRepDTO> getHdAllTransaction(GetAllHdTransactionReqDTO body) {
        CommonPage<GetAllTransactionRepDTO> rst = new CommonPage<>();
        if (body.getWalletAddressList().isEmpty()) {
            return rst;
        }
        if(body.getCoinType()==194){
            //此为EOS交易记录
            String s;
            if(body.getTransType() == 1){
                s=GetAllHdTransactionReqDTO.INCOME;
            }else if(body.getTransType()== -1){
                s=GetAllHdTransactionReqDTO.EXPENSE;
            }else{
                s=GetAllHdTransactionReqDTO.ALL;
            }
            List<GetAllTransactionRepDTO> list = dealEOSTranxRecord(body.getEosAccountList(), s, body.getPageNo(), body.getPageSize(), body.getTokenType());
            rst.setContent(list);
            rst.setPageNo(body.getPageNo());
            rst.setPageSize(list.size());
            return rst;
        }
        //对公钥hash进行分类转换成对应的钱包地址
        List<String> addressListBTC = new ArrayList<>();
        List<String> addressListBCH = new ArrayList<>();
        List<String> pubkHashListInETH = new ArrayList<>();//包括所有以太坊链上的代币地址
        List<String> addressListZCASH = Lists.newArrayList();
        body.getWalletAddressList().forEach(address -> {
            String scriptType = address.getType() == 0 ?
                    ScriptType.P2PKH.getScriptType() :
                    ScriptType.P2SH.getScriptType();
            switch (address.getCoinType()) {
                case 0:
                    addressListBTC.add(AddressClient.toLegacyAddressByPubkHash(address.getPubkHash(),
                            scriptType, networkType));
                    break;
                case 145:
                    addressListBCH.add(AddressClient.toCashAddressByPubkHash(address.getPubkHash(),
                            scriptType, networkType));
                    break;
                case 60:
                    pubkHashListInETH.add(address.getPubkHash());//以太坊公钥hash暂时不转换成地址（在调用方法中会被转换）
                    break;
                case 133:
                    addressListZCASH.add(AddressClient.toZcashAddressByPubkHash(address.getPubkHash(),
                            scriptType,networkType));
                default:
                        break;
            }
        });

        if (body.getCoinType() == 60) {
            List<GetAllTransactionReqDTO.TranxAddress> addressList = new ArrayList<>();
            pubkHashListInETH.forEach(addr ->{
                GetAllTransactionReqDTO.TranxAddress addressE = new GetAllTransactionReqDTO().new TranxAddress();
                addressE.setCoinType(body.getCoinType());
                addressE.setPubkHash(addr);
                addressList.add(addressE);

            });
            GetAllTransactionReqDTO getTraReq = new GetAllTransactionReqDTO();
            getTraReq.setTransType(body.getTransType());
            getTraReq.setPageNo(body.getPageNo());
            getTraReq.setPageSize(body.getPageSize());
            getTraReq.setTimeType(body.getTimeType());
            getTraReq.setWalletAddressList(addressList);
            getTraReq.setTokenType(body.getTokenType());
            rst = this.getAllTransaction(getTraReq);


        } else if (Arrays.asList(0, 145).contains(body.getCoinType())) {
            String transType;
            net.sf.json.JSONObject param = new net.sf.json.JSONObject();


            switch (body.getTransType() == null ? 2 : body.getTransType()) {
                case 1:
                    transType = GetAllHdTransactionReqDTO.INCOME;
                    break;
                case -1:
                    transType = GetAllHdTransactionReqDTO.EXPENSE;
                    break;
                default:
                    transType = GetAllHdTransactionReqDTO.ALL;
            }

            List<GetAllTransactionRepDTO> rstData = new ArrayList<>();

            if (CoinType.WHC.getName().equalsIgnoreCase(body.getTokenType())) {
                Token WHCTOken = tokenRepository.findByName(CoinType.WHC.getName());
                rstData = contractTokensService.dealContractTokensTranB(addressListBCH,  WHCTOken,
                        WHC_CONFIRM_NUM,transType,  body.getPageNo(), body.getPageSize());
            } else if (CoinType.usdt.getName().equals(body.getTokenType())) {
                Token usdtTOken = tokenRepository.findByName(CoinType.usdt.getName());
                rstData = contractTokensService.dealContractTokensTranB(addressListBTC,  usdtTOken,
                        USDT_CONFIRM_NUM, transType,  body.getPageNo(), body.getPageSize());
            }else {
                if (body.getCoinType() == 145) {
                    param.put("addr_list", addressListBCH);
                }else {
                    param.put("addr_list", addressListBTC);
                }
                param.put("token_type",body.getTokenType());
                param.put("page_number",body.getPageNo());
                param.put("page_size",body.getPageSize());
                param.put("trans_type", transType);

                JSONObject result = JSONObject.parseObject(
                        httpService.httpPostWithJson(Urls.BSM_TRANSACTION_B_URL, param).toString()
                );

                if (Objects.isNull(result) || result.isEmpty()) {
                    logger.error("BSM RETURN HISTORY TRANSACTIONS ERROR");
                    CustomException.response(Error.ERR_MSG_SERVICE_ERROR);
                } else if (result.getIntValue("code") != 0) {
                    if (result.containsKey("msg") && result.getString("msg").contains("请求提交失败")) {
                        CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
                    } else {
                        CustomException.response(-1, result.getString("msg"));
                    }
                }


                JSONArray data = result.getJSONArray("data");
                rstData = this.dealBitTranxNew(data, body.getTokenType());
            }


            List<GetAllTransactionRepDTO> returnList = new ArrayList<>();
            if (Objects.nonNull(body.getTransType())) {
                returnList = rstData.stream().filter(tranx -> tranx.getType().intValue() == body.getTransType()).
                        collect(Collectors.toList());
            } else {
                returnList = rstData;
            }

            rst.setContent(returnList);
            rst.setPageNo(body.getPageNo());
            rst.setPageSize(returnList.size());
        } else if (body.getCoinType().equals(CoinType.ZEC.getCode())){
            String trantype = null ;
            if (body.getTransType() == 1){
                trantype = GetAllHdTransactionReqDTO.INCOME;
            } else if (body.getTransType() == -1){
                trantype = GetAllHdTransactionReqDTO.EXPENSE;
            } else {
                trantype = GetAllHdTransactionReqDTO.ALL;
            }
            List<GetAllTransactionRepDTO> getAllTransactionRepDTOS = this.dealZcashTranRecord(addressListZCASH, trantype, body.getPageNo(), body.getPageSize());
            rst.setContent(getAllTransactionRepDTOS);
            rst.setPageNo(body.getPageNo());
            rst.setPageSize(getAllTransactionRepDTOS.size());
        }
        return rst;
    }

    /**
     * ZCASH交易记录整理
     * @param zcashAddressList
     * @param transtype
     * @param pageNum
     * @param pageSize
     * @return
     */
    private List<GetAllTransactionRepDTO> dealZcashTranRecord(List<String> zcashAddressList,String transtype,int pageNum,int pageSize){
        net.sf.json.JSONObject var1 = new net.sf.json.JSONObject();
        var1.put("addr_list",zcashAddressList);
        var1.put("trans_type",transtype);
        var1.put("page_number",pageNum);
        var1.put("page_size",pageSize);
        logger.info("zcash交易记录查询请求参数:{}",var1);
        JSONObject var2 = JSONObject.parseObject(
                httpService.httpPostWithJson(Urls.ZCASH_TRANSATION_URL, var1).toString()
        );
        logger.info("zcash交易记录请求结果:{}",var2);
        if (var2 == null || var2.isEmpty()) {
            logger.error("zcash交易记录请求失败");
            CustomException.response(Error.ERR_MSG_SERVICE_ERROR);
        } else if (var2.getIntValue("code") != 0) {
            if (var2.containsKey("msg") && var2.getString("msg").contains("请求提交失败")) {
                CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
            } else {
                CustomException.response(-1, var2.getString("msg"));
            }
        }
        JSONArray var3 = var2.getJSONArray("data");
        List<GetAllTransactionRepDTO> var4 = this.dealBitTranxNew(var3, CoinType.ZEC.getName());
        logger.info("zcash交易记录整理结果:{}",var4);
        return var4;
    }

    /**
     * EOS交易记录整理
     * @param accountList
     * @return
     */
    private List<GetAllTransactionRepDTO> dealEOSTranxRecord(List<String> accountList,String transType,int pageNum,int pageSize,String tokenType){
        List<GetAllTransactionRepDTO> list=Lists.newArrayList();
        net.sf.json.JSONObject jsonObject = new net.sf.json.JSONObject();
        jsonObject.put("account_list",accountList);
        jsonObject.put("token_type",tokenType);
        jsonObject.put("trans_type",transType);
        jsonObject.put("page_number",pageNum);
        jsonObject.put("pageSize",pageSize);
        logger.info("EOS交易记录查询:{}",jsonObject);
        JSONObject data = JSONObject.parseObject(httpService.httpPostWithJson(Urls.EOS_TRANSACTION_RECORD, jsonObject).toString());
        if(data==null){
            logger.error("获取EOS交易记录失败");
            CustomException.response(Error.ERR_MSG_SERVICE_ERROR);
        }else if(data.getIntValue("code")!=0){
            logger.error("获取EOS交易记录失败");
            CustomException.response(-1, data.getString("msg"));
        }
        JSONArray jsonData = data.getJSONArray("data");
        for(int i=0;i<jsonData.size();i++){
            JSONObject job = jsonData.getJSONObject(i);
            GetAllTransactionRepDTO repDTO = new GetAllTransactionRepDTO();
            if(transType.equals(GetAllHdTransactionReqDTO.INCOME)){
                repDTO.setType(1);
            }else if(transType.equals(GetAllHdTransactionReqDTO.EXPENSE)){
                repDTO.setType(-1);
            }
            repDTO.setStatus(job.getIntValue("type")==1?1:3);
            repDTO.setMoney(Double.parseDouble(StringUtils.substringBefore(job.getString("quantity")," ")));
            repDTO.setTime(job.getLongValue("block_timestamp"));
            repDTO.setHeight(job.getIntValue("block_num"));
            repDTO.setSuccess(job.getIntValue("type")==1?true:false);
            repDTO.setHash(job.getString("tx_id"));
            repDTO.setTokenName(job.getString("symbol"));
            repDTO.setPrice(flatMoneyService.tokenPriceAdaptation(tokenType));
            repDTO.setDecimal(job.getIntValue("precision"));
            repDTO.setMemo(job.getString("memo"));
            repDTO.setFromWallet(job.getString("from"));
            repDTO.setToWallet(job.getString("to"));
            repDTO.setConfirmNum(job.getLong("confirmations"));
            repDTO.setTargetConfirmNum(job.getLong("confirm_need_num"));
            repDTO.setWebTxUrl("https://scaneos.io");
            list.add(repDTO);
        }
        return list;

    }




    /**
     * 获取WHC转币记录
     * @param addressList
     * @return
     */
    List<GetAllTransactionRepDTO> dealWHCTranx(List<String> addressList,
                                               String transType,
                                               Integer pageNo,
                                               Integer pageSize ) {
        List<GetAllTransactionRepDTO> repDTOS = new ArrayList<>();

        Token WHCTOken = tokenRepository.findByName(CoinType.WHC.getName());
        if (WHCTOken == null) {
            CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT);
        }
        List<ContractTokenBMessage> contractTokenBMessageList = this.getWHCTranaction(addressList,
                SEND_TOKEN,
                WHCTOken.getOwnerTokenId(),
                transType,
                pageNo,
                pageSize);
        if (contractTokenBMessageList == null || contractTokenBMessageList.isEmpty()) {
            return repDTOS;
        } else {
            Double whcPrice = flatMoneyService.tokenPriceAdaptation(WHCTOken.getName());
            contractTokenBMessageList.forEach(contractTokenBMessage -> {
                GetAllTransactionRepDTO repDTO = new GetAllTransactionRepDTO();
                repDTO = contractTokenBMessage.toAllTransactionRepDTO(WHCTOken, WHC_CONFIRM_NUM, whcPrice, BCH_WEB_URL);
                //状态判断逻辑
                if (addressList.contains(contractTokenBMessage.getFromAddress()) &&
                        !addressList.contains(contractTokenBMessage.getToAddress())) {
                    repDTO.setType(GetAllTransactionRepDTO.EXPENSE);
                    repDTOS.add(repDTO);

                } else if (!addressList.contains(contractTokenBMessage.getFromAddress()) &&
                        addressList.contains(contractTokenBMessage.getToAddress())) {
                    repDTO.setType(GetAllTransactionRepDTO.INCOME);
                    repDTOS.add(repDTO);

                } else if (contractTokenBMessage.getFromAddress().
                        equalsIgnoreCase(contractTokenBMessage.getToAddress()) ||
                        addressList.contains(contractTokenBMessage.getFromAddress()) &&
                                addressList.contains(contractTokenBMessage.getToAddress())) {
                    repDTO.setType(GetAllTransactionRepDTO.EXPENSE);
                    repDTOS.add(repDTO);

                    GetAllTransactionRepDTO repDTOIncome = new GetAllTransactionRepDTO();
                    BeanCopier.getInstance().copyBean(repDTO, repDTOIncome);
                    repDTOIncome.setType(GetAllTransactionRepDTO.INCOME);
                    repDTOS.add(repDTOIncome);

                }
            });

        }
        return repDTOS;
    }



    /**
     * 获取WHC地址的所有交易记录
     * @param addressList
     * @param isBurn
     * @param propertyId
     * @param transType
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<ContractTokenBMessage> getWHCTranaction(List<String> addressList,
                                                 Integer isBurn,
                                                 Long propertyId,
                                                 String transType,
                                                 Integer pageNo,
                                                 Integer pageSize) {
       String trancstr = OkHttpUtil.http(Urls.GET_WHC_TRANSACTION)
               .param("addr_list", addressList)
               .param("is_burn", isBurn)
               .param("property_id", propertyId)
               .param("trans_type", transType)
               .param("page_number", pageNo)
               .param("page_size", pageSize)
               .post();

       NewsRepWHCRep newsRepWHCRep = null;
       try {
            newsRepWHCRep = new Gson().fromJson(trancstr, NewsRepWHCRep.class);
       } catch (Exception e) {
           logger.error("NEWS SERVER返回数据有误");
           CustomException.response(Error.SERVER_EXCEPTION);
       }

       return newsRepWHCRep.getData();
    }

    @Data
    class NewsRepWHCRep{
        private Integer code;
        private String msg;
        private Integer total;
        private List<ContractTokenBMessage> data;
    }




    @Value("${whc.burn.confirm.num:1000}")
    private Long WHC_BURN_CONFIRM_NUM;
    @Override
    public List<WHCBurnHistoryRepDTO> getWHCBurnHistory(WHCBurnHistoryReqDTO body) {
        List<AddressReqDTO.Address> whcAddressList = body.getAddressList();

        if (whcAddressList != null) {
            List<String> addressList = new ArrayList<>();
            whcAddressList.forEach(address -> {
                String scriptType = address.getType() == 0 ?
                        ScriptType.P2PKH.getScriptType() :
                        ScriptType.P2SH.getScriptType();

                addressList.add(AddressClient.toCashAddressByPubkHash(address.getAddressHash(),
                        scriptType, networkType));
            });

            Token WHCToken = tokenRepository.findByName(CoinType.WHC.getName());
            if (WHCToken == null) {
                CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT);
            }

            List<ContractTokenBMessage> contractTokenBMessageList = this.getWHCTranaction(addressList, BURN_WHC, WHCToken.getOwnerTokenId(),
                    "", body.getPageNo(), body.getPageSize());
            logger.info("NEWS SERVER RETURN WHC BURN DATA SIZE:{}", contractTokenBMessageList == null ? null : contractTokenBMessageList.size());

            String strHeight = redisTemplate.opsForValue().get(Constant.BLOCK_CURRENT_HEIGHT + WHCToken.getCoinType());
            Long currentHeight = Long.parseLong(strHeight);

            SimpleDateFormat spf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            List<WHCBurnHistoryRepDTO> whcBurnHistoryRepDTOS = new ArrayList<>();
            if (contractTokenBMessageList != null) {
                contractTokenBMessageList.forEach(contractTokenBMessage -> {
                    Long currentConfirmNum = 0l;
                    if (contractTokenBMessage.getBlockHeight() != null) {
                        currentConfirmNum = currentHeight - contractTokenBMessage.getBlockHeight() + 1;
                        if (currentConfirmNum < 1l) {
                            currentConfirmNum = 1l;
                        }
                    }
                    String blockTime = "";
                    if (contractTokenBMessage.getBlockTime() != null) {
                        blockTime = spf.format(new Date(contractTokenBMessage.getBlockTime() * 1000));
                    }
                    WHCBurnHistoryRepDTO burnHistory = new WHCBurnHistoryRepDTO();
                    burnHistory.setAddress(contractTokenBMessage.getFromAddress());
                    burnHistory.setTxId(contractTokenBMessage.getTxId());
                    burnHistory.setTokenName(WHCToken.getName());
                    burnHistory.setGetTokenNum(contractTokenBMessage.getValue());
                    burnHistory.setExpectConfirmNum(WHC_BURN_CONFIRM_NUM);
                    burnHistory.setDecimal(WHCToken.getDecimal());
                    burnHistory.setBlockTime(blockTime);
                    burnHistory.setFailReason(contractTokenBMessage.getFailReason());
                    burnHistory.setBurnTokenNum(contractTokenBMessage.getBurn());
                    if (contractTokenBMessage.getSuccess() != null) {
                        if (contractTokenBMessage.getSuccess()) {
                            if (currentConfirmNum >= WHC_BURN_CONFIRM_NUM) {
                                burnHistory.setSuccess(WHCBurnHistoryRepDTO.SUCCESS);
                            } else {
                                burnHistory.setSuccess(WHCBurnHistoryRepDTO.BURNING);
                            }
                        } else {
                            burnHistory.setSuccess(WHCBurnHistoryRepDTO.FAIL);
                        }
                    } else {
                        burnHistory.setSuccess(WHCBurnHistoryRepDTO.BURNING);
                    }


                    if (currentConfirmNum >= WHC_BURN_CONFIRM_NUM) {
                        burnHistory.setConfirmNum(WHC_BURN_CONFIRM_NUM);
                    } else {
                        if (contractTokenBMessage.getConfirmations() > currentConfirmNum) {
                            burnHistory.setConfirmNum(contractTokenBMessage.getConfirmations());
                        } else {
                            burnHistory.setConfirmNum(currentConfirmNum);
                        }
                    }

                    whcBurnHistoryRepDTOS.add(burnHistory);
                });
            }

            return whcBurnHistoryRepDTOS;
        } else {
            CustomException.response(Error.ERR_MSG_PARAM_ERROR);
        }

        return null;
    }

    @Override
    public PageRep<GetExRecordRepDTO> getExRecord(GetExRecordReqDTO body) {

//        List<String> address = new ArrayList<>();
//        body.getAddressInfoList().forEach(addressInfo -> {
//            String scriptType = ScriptType.P2SH.name();
//            if (addressInfo.getType().equals(0)) {
//                scriptType = ScriptType.P2PKH.name();
//            }
//            if (CoinType.BCH.getCode().equals(addressInfo.getCoinType())) {
//                address.add(AddressClient.toCashAddressByPubkHash(addressInfo.getAddress(), scriptType, networkType));
//            } else if (CoinType.ETH.getCode().equals(addressInfo.getCoinType())) {
//                address.add(AddressClient.toEtherAddressByHash(addressInfo.getAddress()));
//            }
//        });
//
//        List<GetExRecordRepDTO> repDTOS = new ArrayList<>();
//        PageRequest pageRequest = new PageRequest
//                (body.getPageNo()-1, body.getPageSize(), new Sort(Sort.Direction.DESC, "create_time"));
//        Page<ExRecord> exRecordPage = exRecordRepository.findByFromAddressInAndFromNameOrFromAddressInAndToName(address,
//                body.getTokenName(), address, body.getTokenName(), pageRequest);
//        List<ExRecord> exRecords = exRecordPage.getContent();

        PageRep<GetExRecordRepDTO> result = new PageRep<>();
        List<GetExRecordRepDTO> repDTOS = new ArrayList<>();
        SimpleDateFormat spf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        PageHelper.startPage(body.getPageNo(), body.getPageSize());
        List<ExRecord> exRecords = exRecordDao.queryByWalletHash(body.getWalletHash(),
                body.getTokenName());
        PageInfo<ExRecord> pageInfo = new PageInfo<>(exRecords);

        for (int i = 0; i < exRecords.size(); i++) {
            ExRecord exRecord = exRecords.get(i);
            Token fromToken = tokenRepository.findByName(exRecord.getFromName());
            if (Objects.isNull(fromToken)) {
                logger.info("暂不支持代币" + exRecord.getFromName());
                continue;
            }
            Token toToken = tokenRepository.findByName(exRecord.getToName());
            if (Objects.isNull(toToken)) {
                logger.info("暂不支持代币" + exRecord.getToName());
                continue;
            }

            GetExRecordRepDTO dto = new GetExRecordRepDTO();
            BeanCopier.getInstance().copyBean(exRecord, dto);
            dto.setCreateTime(spf.format(exRecord.getCreateTime()));
            dto.setFromToken(exRecord.getFromName());
            dto.setToToken(exRecord.getToName());
            dto.setFromTokenDecimal(fromToken.getDecimal());
            dto.setToTokenDecimal(toToken.getDecimal());
            repDTOS.add(dto);
        }

        result.setContent(repDTOS);
        result.setPageNo(pageInfo.getPageNum());
        result.setPageTotal(pageInfo.getPages());
        return result;
    }

    @Override
    public GetExInfoRepDTO getExRecordInfo(GetExInfoReqDTO body) {
        ExTxInfo exTxInfo = gatewayDao.queryByFromHash(body.getFromHash());
        SimpleDateFormat spf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        if (exTxInfo != null) {
            String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + CoinType.GATEWAY.getName();
            String StrHeight = redisTemplate.opsForValue().get(blockHeightKey);
            BigInteger currentHeight = new BigInteger(StrHeight);
            BigInteger confirmNum = currentHeight.subtract(BigInteger.valueOf(exTxInfo.getBlockHeight()));

            GetExInfoRepDTO repDTO = new GetExInfoRepDTO();
            repDTO.setConfirmNum(confirmNum.longValue());
            repDTO.setTxHeight(exTxInfo.getBlockHeight());
            repDTO.setSndTime(spf.format(new Date(exTxInfo.getTime() * 1000)));
            repDTO.setServiceFee(exTxInfo.getDgwFee());
            repDTO.setGateHash(exTxInfo.getDgwHash());

            GetExInfoRepDTO.TxInfo toTx = new GetExInfoRepDTO.TxInfo();
            toTx.setValue(exTxInfo.getFinalAmount());
            toTx.setTokeName(exTxInfo.getToTokenSymbol());
            toTx.setHash(exTxInfo.getToTxHash());
            toTx.setTokenDecimal(exTxInfo.getToTokenDecimals());
            toTx.setAddress(Arrays.asList(exTxInfo.getToAddrs()));
            toTx.setChain(exTxInfo.getToChain());
            toTx.setWebUrl(getWebUrl(exTxInfo.getToChain(), exTxInfo.getToTxHash()));
            repDTO.setToTx(toTx);

            GetExInfoRepDTO.TxInfo fromTx = new GetExInfoRepDTO.TxInfo();
            fromTx.setTxFee(exTxInfo.getFromFee());
            fromTx.setValue(exTxInfo.getAmount());
            fromTx.setTokeName(exTxInfo.getTokenSymbol());
            fromTx.setHash(exTxInfo.getFromTxHash());
            fromTx.setTokenDecimal(exTxInfo.getTokenDecimals());
            fromTx.setAddress(Arrays.asList(exTxInfo.getFromAddrs().split(",")));
            fromTx.setChain(exTxInfo.getFromChain());
            fromTx.setWebUrl(getWebUrl(exTxInfo.getFromChain(), exTxInfo.getFromTxHash()));
            repDTO.setFromTx(fromTx);

            return repDTO;

        }
        return null;
    }

    @Override
    public CommonResult validZcashAddress(Map map) {
        String address = (String)map.get("address");
        if(address == null){
            CustomException.response(Error.REQUEST_PARAM_INVALID);
        }
        JSONArray param = new JSONArray();
        param.add(address);
        String strRst = rpcClient.query(CoinType.ZEC.getName(),"validateaddress", param).toString();
        JSONObject var1 = JSONObject.parseObject(strRst);
        JSONObject var2 = var1.getJSONObject("result");
        Boolean var3 = var2.getBoolean("isvalid");
        CommonResult commonResult = new CommonResult();
        commonResult.setData(var3);
        return commonResult;
    }

    @Override
    public CommonResult getZcashHeight() {
        String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + CoinType.ZEC.getCode();
        String zcashStrHeight = stringRedisTemplate.opsForValue().get(blockHeightKey);
        CommonResult commonResult = new CommonResult();
        commonResult.setData(zcashStrHeight);
        return commonResult;
    }


    /**
     * 获取三方web地址
     * @return
     */
    public  String getWebUrl(String chainName,String txHash){
        String webUrl = "";
        if (CoinType.ETH.getName().equalsIgnoreCase(chainName)) {
            webUrl = ETH_WEB_URL;
        } else if (CoinType.BCH.getName().equalsIgnoreCase(chainName)) {
            webUrl = BCH_WEB_URL;
        } else if (CoinType.BTC.getName().equalsIgnoreCase(chainName)) {
            webUrl = BTC_WEB_URL;
        }

        return webUrl + txHash;
    }


    /**
     * 整理bit交易（新）
     * @param data
     * @param tokenName
     * @return
     */
    @Value("${bch.web.url}")
    private String BCH_WEB_URL;
    @Value("${btc.web.url}")
    private String BTC_WEB_URL;
    @Value("${zcash.web.url}")
    private String ZCASH_WEB_URL;
    @Value("${confirm.num:6}")
    private Long CONFIRM_NUM;
    private List<GetAllTransactionRepDTO> dealBitTranxNew(JSONArray data,
                                                       String tokenName){
        List<GetAllTransactionRepDTO> rstData = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject tranxJs = data.getJSONObject(i);
            JSONArray vinArray = tranxJs.getJSONArray("vin");
            JSONArray voutArray = tranxJs.getJSONArray("vout");

            BigInteger inputPriceCount = BigInteger.valueOf(0);
            BigInteger outPriceCount = BigInteger.valueOf(0);

            List<GetAllTransactionRepDTO.TranxAddrInfo> fromAddressList = new ArrayList<>(); //转账方地址集合
            List<GetAllTransactionRepDTO.TranxAddrInfo> toAddressList = new ArrayList<>(); //收款方的钱包地址集合
            if(vinArray.size() != 0){
                for (int j = 0; j < vinArray.size(); j++) {
                    JSONObject vin = vinArray.getJSONObject(j);
                    GetAllTransactionRepDTO.TranxAddrInfo addrInfo = new GetAllTransactionRepDTO.TranxAddrInfo();
                    addrInfo.setAddress(vin.getString("input_address"));
                    addrInfo.setMoney(vin.getDouble("pre_vout_value"));
                    fromAddressList.add(addrInfo);
                    inputPriceCount = inputPriceCount.add(BigInteger.valueOf(vin.getLong("pre_vout_value")));
                }
            }
            if(voutArray.size() != 0){
                for (int j = 0; j < voutArray.size(); j++) {
                    JSONObject vout = voutArray.getJSONObject(j);
                    GetAllTransactionRepDTO.TranxAddrInfo addrInfo = new GetAllTransactionRepDTO.TranxAddrInfo();
                    addrInfo.setAddress(vout.getString("output_address"));
                    addrInfo.setMoney(vout.getDouble("vout_value"));
                    toAddressList.add(addrInfo);
                    outPriceCount = outPriceCount.add(BigInteger.valueOf(vout.getLong("vout_value")));
                }
            }
            Token token = tokenRepository.findByName(tokenName);
            String txId = tranxJs.getString("txid");
            GetAllTransactionRepDTO repDTO = new GetAllTransactionRepDTO();
            repDTO.setOutputAddress(toAddressList);
            repDTO.setInputAddress(fromAddressList);
            repDTO.setTime(tranxJs.getLong("block_time") * 1000);
            repDTO.setSuccess(true); //需确认
            repDTO.setPrice(flatMoneyService.tokenPriceAdaptation(tokenName));
            repDTO.setHeight(tranxJs.getInteger("block_height"));
            repDTO.setHash(txId);
            repDTO.setTokenName(tokenName);
            repDTO.setDecimal(SpecialTimedTask.TOKENS_NAME_KEY_MAP.get(tokenName).getDecimal());
            repDTO.setTxFee(inputPriceCount.subtract(outPriceCount).doubleValue());
            if (CoinType.BCH.getCode().equals(token.getCoinType())) {
                repDTO.setWebTxUrl(BCH_WEB_URL + txId);
            } else if (CoinType.BTC.getCode().equals(token.getCoinType())) {
                repDTO.setWebTxUrl(BTC_WEB_URL + txId);
            } else if (CoinType.ZEC.getCode().equals(token.getCoinType())){
                repDTO.setWebTxUrl(ZCASH_WEB_URL + txId);
            }

            switch (tranxJs.getString("type")) {
                case "income":
                    repDTO.setType(1);
                    break;
                case "expense":
                    repDTO.setType(-1);
                    break;
                default:
                    break;
            }
//            switch (tranxJs.getIntValue("status")) {
//                case 0 :
//                    repDTO.setStatus(3);
//                    break;
//                case 3 :
//                    repDTO.setStatus(3);
//                    break;
//                case 1:
//                    repDTO.setStatus(1);
//                    break;
//                case 2:
//                    repDTO.setSuccess(false);
//                default:
//                    break;
//            }
            String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT;
            Long needConfirNum  = 0l;
            if (CoinType.BCH.getName().equals(tokenName)) {
                 needConfirNum  = WHC_CONFIRM_NUM;
                 blockHeightKey = blockHeightKey + CoinType.BCH.getCode();
            } else if(CoinType.BTC.getName().equalsIgnoreCase(tokenName)) {
                needConfirNum = CONFIRM_NUM;
                blockHeightKey = blockHeightKey + CoinType.BTC.getCode();
            } else if(CoinType.ZEC.getName().equalsIgnoreCase(tokenName)){
                needConfirNum = CONFIRM_NUM;
                blockHeightKey = blockHeightKey + CoinType.ZEC.getCode();
            }
            String strHeight = redisTemplate.opsForValue().get(blockHeightKey);
            Long currentHeight = StringUtils.isBlank(strHeight) ? 0l : Long.parseLong(strHeight);

            Long height = tranxJs.getLong("block_height");
            Long differences = height == 0 ? 0 : currentHeight - height + 1;
            Long confirmations = differences > 0l ? differences : 0l;

            if (confirmations == 0) {
                repDTO.setStatus(GetAllTransactionRepDTO.PENDING);
            } else if (confirmations > 0 && confirmations < needConfirNum) {
                repDTO.setStatus(GetAllTransactionRepDTO.PACKED);
            } else if (confirmations >= needConfirNum) {
                repDTO.setStatus(GetAllTransactionRepDTO.CONFIRMED);
            } else {
                repDTO.setStatus(GetAllTransactionRepDTO.UNKNOWN);
            }

            repDTO.setTargetConfirmNum(needConfirNum);
            repDTO.setConfirmNum(confirmations);
            rstData.add(repDTO);
        }
        return rstData;
    }


    /**
     * 整理bit交易（旧）
     * @param data
     * @param addressListBTC
     * @param addressListBCH
     * @param tokenName
     * @return
     */
    private List<GetAllTransactionRepDTO> dealBitTranx(JSONArray data,
                                                       List<String> addressListBTC,
                                                       List<String> addressListBCH,
                                                       String tokenName){
        List<GetAllTransactionRepDTO> rstData = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            GetAllTransactionRepDTO repDTO = new GetAllTransactionRepDTO();
            JSONObject tranxJs = data.getJSONObject(i);

            //通过输入地址中只要有一个（包含一个）是该Hd钱包地址就是输出交易
            Integer inNum = 0;
            JSONArray vinArray = tranxJs.getJSONArray("vin");
            JSONArray voutArray = tranxJs.getJSONArray("vout");
            List<GetAllTransactionRepDTO.TranxAddrInfo> fromAddressList = new ArrayList<>(); //转账方地址集合
            BigInteger inputPriceCount = BigInteger.valueOf(0);
            BigInteger outPriceCount = BigInteger.valueOf(0);
            List<String> inAddressList = new ArrayList<>();
            for (int j = 0; j < vinArray.size(); j++) {
                GetAllTransactionRepDTO.TranxAddrInfo addrInfo = new GetAllTransactionRepDTO.TranxAddrInfo();
                JSONObject vin = vinArray.getJSONObject(j);
                if (addressListBCH.contains(vin.getString("input_address")) ||
                        addressListBTC.contains(vin.getString("input_address"))) {
                    inNum = inNum + 1;
                }
                addrInfo.setAddress(vin.getString("input_address"));
                addrInfo.setMoney(vin.getDouble("pre_vout_value"));
                fromAddressList.add(addrInfo);

                inputPriceCount = inputPriceCount.add(BigInteger.valueOf(vin.getLong("pre_vout_value")));

                inAddressList.add(vin.getString("input_address"));

            }

            List<GetAllTransactionRepDTO.TranxAddrInfo> toAddressList = new ArrayList<>(); //收款方的钱包地址集合
            BigInteger selfGetPrice = BigInteger.valueOf(0); //自方收款金额
            BigInteger otherGetPrice = BigInteger.valueOf(0); //对方收款金额

            List<String> outAddressList = new ArrayList<>();
            for (int index = 0; index < voutArray.size(); index++) {
                outAddressList.add(voutArray.getJSONObject(index).
                        getString("output_address"));
            }

            Boolean isPayToSelf = false;
            if (addressListBCH.containsAll(outAddressList) && addressListBCH.containsAll(inAddressList) ||
                    addressListBTC.containsAll(outAddressList) && addressListBTC.containsAll(inAddressList)) {
                //如果是自转账交易
                isPayToSelf = true;
                for (int j = 0; j < voutArray.size(); j++) {
                    JSONObject vout = voutArray.getJSONObject(j);
                    selfGetPrice = selfGetPrice.add(BigInteger.valueOf(vout.getLong("vout_value")));
                    GetAllTransactionRepDTO.TranxAddrInfo addrInfo =new GetAllTransactionRepDTO.TranxAddrInfo();
                    addrInfo.setAddress(vout.getString("output_address"));
                    addrInfo.setMoney(vout.getDouble("vout_value"));
                    toAddressList.add(addrInfo);
                }

            }else {
                //普通交易
                for (int j = 0; j < voutArray.size(); j++) {
                    JSONObject vout = voutArray.getJSONObject(j);
                    GetAllTransactionRepDTO.TranxAddrInfo addrInfo = new GetAllTransactionRepDTO.TranxAddrInfo();
                    addrInfo.setAddress(vout.getString("output_address"));
                    addrInfo.setMoney(vout.getDouble("vout_value"));
                    toAddressList.add(addrInfo);
                    outPriceCount = outPriceCount.add(BigInteger.valueOf(vout.getLong("vout_value")));
                }
                if (inNum > 0) { //如果输入地址中只要有一个以上该hd钱包的地址，就是输出
                    repDTO.setType(-1);
                } else {
                    repDTO.setType(1);
                }
            }


            repDTO.setOutputAddress(toAddressList);
            repDTO.setInputAddress(fromAddressList);
            repDTO.setTime(tranxJs.getLong("block_time") * 1000);
            repDTO.setSuccess(true); //需确认
            repDTO.setPrice(flatMoneyService.tokenPriceAdaptation(tokenName));
            repDTO.setHeight(tranxJs.getInteger("block_height"));
            repDTO.setHash(tranxJs.getString("txid"));
            repDTO.setTokenName(tokenName);
            repDTO.setDecimal(SpecialTimedTask.TOKENS_NAME_KEY_MAP.get(tokenName).getDecimal());
            switch (tranxJs.getIntValue("status")) {
                case 0 :
                    repDTO.setStatus(3);
                    break;
                case 3 :
                    repDTO.setStatus(3);
                    break;
                case 1:
                    repDTO.setStatus(1);
                    break;
                case 2:
                    repDTO.setSuccess(false);
                default:
                    break;
            }

            if (isPayToSelf) {
                repDTO.setTxFee(inputPriceCount.subtract(selfGetPrice).doubleValue());//计算矿工费
                repDTO.setType(-1);
                rstData.add(repDTO);

                GetAllTransactionRepDTO receiveRepDTO = new GetAllTransactionRepDTO();
                BeanCopier.getInstance().copyBean(repDTO, receiveRepDTO);
                receiveRepDTO.setType(1);
                rstData.add(receiveRepDTO);
            } else {
                repDTO.setTxFee(inputPriceCount.subtract(outPriceCount).doubleValue());//计算矿工费
                rstData.add(repDTO);
            }
        }
        return rstData;
    }

    @Override
    public List<Rate> getRate() {
        JSONObject rst = JSONObject.parseObject(
                httpService.httpGet(Urls.GET_USDT_TO_CNY,null).toString()
        );
        if (rst.containsKey("code") && rst.getIntValue("code") != 0) {
            CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
        }

        List<Rate> rateList = new ArrayList<>();


        Rate rateForCNY = new Rate();//美元->人民币的汇率（方便返回所有币种信息和切换）
        Money moneyCNY = Money.getMoney("CNY");
        rateForCNY.setSymbol(moneyCNY.getSymblo());
        rateForCNY.setRate(rst.getJSONObject("gateio").getDouble("last"));
        rateForCNY.setName(moneyCNY.getCode());
        rateForCNY.setcName(moneyCNY.getName());
        rateList.add(rateForCNY);

        Rate rateForD = new Rate();//美元->美元的汇率（方便返回所有币种信息和切换）
        Money moneyD = Money.getMoney("USD");
        rateForD.setSymbol(moneyD.getSymblo());
        rateForD.setRate(1.0);
        rateForD.setName(moneyD.getCode());
        rateForD.setcName(moneyD.getName());
        rateList.add(rateForD);

        return rateList;


//        List<Rate> rateList = new ArrayList<>();
//        Map<String, Object> rateParm = new HashMap<>();
//        rateParm.put("key", appKey);
//        JSONObject rateRst = httpService.httpGet(Urls.GET_MONEY_RATE, rateParm);
//        logger.info("rateRst :{}",rateRst);
//
//        if (Objects.isNull(rateRst) || rateRst.isEmpty()) {
//            logger.error("GET RATE ERROR");
//            throw new CustomException(CgiError.ERR_MSG_SERVICE_ERROR, CgiError.ERR_COMMON_ERROR);
//        } else if (!rateRst.containsKey("error_code") || rateRst.getInt("error_code") != 0) {
//            if (rateRst.containsKey("msg") && rateRst.getString("msg").contains("请求提交失败")) {
//                throw new CustomException(CgiError.ERR_MSG_REQUEST_FAIL, CgiError.ERR_REQUEST_FAIL);
//            } else {
//                throw new CustomException(CgiError.ERR_MSG_RATE_ERROR, CgiError.ERR_RATE_ERROR);
//            }
//        }
//
//        JSONArray data = rateRst.getJSONObject("result").getJSONArray("list");
//
//        Double dollarRate = null;
//        for (int i = 0; i<data.size(); i++) {//第一次遍历获取美元和人民币之间的汇率
//            JSONArray rateJs = data.getJSONArray(i);
//            if ("美元".equalsIgnoreCase(rateJs.getString(0))){
//                dollarRate = rateJs.getDouble(2)/rateJs.getDouble(1);
//                Rate rate = new Rate();
//                Money money = SpecialTimedTask.MONEY_CODE_MAP.get("CNY");
//                rate.setName(money.getCode());
//                rate.setRate(dollarRate);
//                rate.setcName(money.getName());
//                rate.setSymbol(money.getSymbol());
//
//                rateList.add(rate);
//                break;
//            }
//        }
//
//        Rate rateForD = new Rate();//美元->美元的汇率（方便返回所有币种信息和切换）
//        Money moneyD = SpecialTimedTask.MONEY_CODE_MAP.get("USD");
//        rateForD.setSymbol(moneyD.getSymbol());
//        rateForD.setRate(1.0);
//        rateForD.setName(moneyD.getCode());
//        rateForD.setcName(moneyD.getName());
//        rateList.add(rateForD);
//
//
//        for (int i = 1; i<data.size(); i++) { //第二次遍历整理出美元和其他法币的汇率
//            Rate rate = new Rate();
//            JSONArray rateJs = data.getJSONArray(i);
//
//            Money money = SpecialTimedTask.MONEY_NAME_MAP.get(rateJs.getString(0));
//            rate.setName(money.getCode());
//            rate.setRate(dollarRate / (rateJs.getDouble(2)/rateJs.getDouble(1)));
//            rate.setcName(money.getName());
//            rate.setSymbol(money.getSymbol());
//
//            rateList.add(rate);
//
//        }
//        List<Rate> resp = rateList.stream().filter(rate ->
//                Money.isSupport(rate.getName())).collect(Collectors.toList());
//        return resp;


    }


    @Override
    public String sendTransactionB(SendtranxBReqDTO body) {
        //兼容老的数据格式
        if (StringUtils.isBlank(body.getTokenName())) {
            if (CoinType.BTC.getCode().equals(body.getCoinType())) {
                body.setTokenName(CoinType.BTC.getName());
            } else if (CoinType.BCH.getCode().equals(body.getCoinType())){
                body.setTokenName(CoinType.BCH.getName());
            } else if (CoinType.ZEC.getCode().equals(body.getCoinType())){
                body.setTokenName(CoinType.ZEC.getName());
            } else {
                CustomException.response(Error.ERR_MSG_COIN_TYPE_ERROR);
            }
        }

        JSONArray param = new JSONArray();
        JSONObject rst = new JSONObject();

        param.add(body.getTransaction());

        if (Arrays.asList(145, 0,133).contains(body.getCoinType())) {
            String tokenName;
            if (CoinType.BCH.getCode().equals(body.getCoinType())) {
                if (CoinType.WHC.getName().equalsIgnoreCase(body.getTokenName())) {
                    tokenName = CoinType.WHC.getName();
                } else {
                    tokenName = CoinType.BCH.getName();
                }
            } else if (CoinType.BTC.getCode().equals(body.getCoinType())) {
                if (CoinType.usdt.getName().equalsIgnoreCase(body.getTokenName())) {
                    tokenName = CoinType.usdt.getName();
                } else {
                    tokenName = CoinType.BTC.getName();
                }
            } else {
                tokenName = CoinType.ZEC.getName();
            }
            String strRst = rpcClient.query(tokenName,"sendrawtransaction", param).toString();
            rst = JSONObject.parseObject(strRst);
            logger.info("strRst:{},jsonRst:{},", strRst, rst);

            if (rst.containsKey("code") &&  rst.getIntValue("code") == -1 &&
                    rst.getString("msg").contains("请求提交失败")) {
                CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
            }else if(rst.containsKey("error") && rst.getJSONObject("error") != null){
                CustomException.response(rst.getJSONObject("error").getString("message"));
            }
        } else {
            CustomException.response(Error.ERR_MSG_COIN_TYPE_ERROR);
        }

        String txId = rst.getString("result");
        saveBeecoinTranx(txId); //保存转出交易

        //保存兑换记录
        saveExInfo(body.getExInfo(), txId);

        return txId;
    }

    @Autowired
    private BlockRepository blockRepository;

    /**
     * 保存由beecoin发起的转出交易
     * @param txId
     */
    private void saveBeecoinTranx(String txId){
        BlockTransaction find = blockRepository.findByTxId(txId);
        if (find != null) {
            find.setChannel(BlockTransaction.TX_CHANNEL_BEECOIN);
        } else {
            find = new BlockTransaction();
            find.setTxId(txId);
            find.setChannel(BlockTransaction.TX_CHANNEL_BEECOIN);
        }
        blockRepository.save(find);
    }



    @Override
    public List<GetTxFeeRepDTO> getTxFee(GetTxFeeReqDTO body) {
        List<GetTxFeeRepDTO> resp = new ArrayList<>();

        net.sf.json.JSONObject param = new net.sf.json.JSONObject();
        param.put("token_type", body.getTokenNames());
        JSONObject rst = JSONObject.parseObject(
                httpService.httpPostWithJson(Urls.GET_TXFEE_URL,param).toString()
        );
        if (rst.containsKey("code") &&  rst.getIntValue("code") == -1) {
            if (rst.getString("msg").contains("请求提交失败")) {
                CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
            } else {
                CustomException.response(rst.getString("msg"));
            }

        }

        JSONArray data = rst.getJSONArray("data");
        for (int i = 0; i < data.size(); i++) {
            GetTxFeeRepDTO txFee = new GetTxFeeRepDTO();
            JSONObject txfeeObj = data.getJSONObject(i);

            List<GetTxFeeRepDTO.TxFeeMap> txFeeMapList = new ArrayList<>();
            for (int j = 0; j < txfeeObj.getJSONArray("data").size(); j++) {
                JSONObject infoObj = txfeeObj.getJSONArray("data").getJSONObject(j);
                GetTxFeeRepDTO.TxFeeMap txFeeMap = new GetTxFeeRepDTO.TxFeeMap();
                txFeeMap.setLatency(infoObj.getInteger("latency"));
                txFeeMap.setTxFee(infoObj.getLong("price"));
                txFeeMapList.add(txFeeMap);
            }
            txFee.setTokenName(txfeeObj.getString("token"));
            txFee.setTxFeeMap(txFeeMapList);
            txFee.setHigh(txfeeObj.getLong("high"));
            txFee.setLow(txfeeObj.getLong("low"));
            resp.add(txFee);
        }

        return resp;
    }

    @Override
    public TransactionRes getTransactionById(GetTransactionByIdReqDTO body) {
        TransactionRes res = new TransactionRes();

        if (StringUtils.isBlank(body.getTokenName())) {
            if (CoinType.BTC.getCode().equals(body.getCoinType())) {
                body.setTokenName(CoinType.BTC.getName());
            } else if (CoinType.BCH.getCode().equals(body.getCoinType())) {
                body.setTokenName(CoinType.BCH.getName());
            } else if (CoinType.ETH.getCode().equals(body.getCoinType())) {
                body.setTokenName(CoinType.ETH.getName());
            }
        }

        BlockTransaction transaction = new BlockTransaction();
        if (CoinType.ETH.getCode().equals(body.getCoinType())) {
            JSONObject result = JSONObject.parseObject(
                    OkHttpUtil.http(Urls.GET_ETH_TX_INFO)
                    .param("txid", body.getTxId())
                    .post());
            JSONObject data = result.getJSONObject("data");
            if (Objects.isNull(data) || data.isEmpty()) {
                CustomException.response(result.getString("msg"));
            }
            String from = data.getString("from");
            String to = data.getString("to");
            if (from != null) {
                VIn in = new VIn();
                in.setAddress(from);

                in.setValue(data.getLong("value") + data.getLong("txFee"));
                transaction.setVIns(Arrays.asList(in));
            }

            if (to != null) {
                VOut out = new VOut();
                out.setAddress(to);
                out.setValue(data.getLong("value"));
                transaction.setVOuts(Arrays.asList(out));
            }

            Integer status = data.getInteger("type");
            if (status == 1) {
                if (data.getBoolean("success")) {
                    transaction.setStatus(BlockTransaction.CONFIRMED);
                } else {
                    transaction.setStatus(BlockTransaction.FAILED);
                }
                transaction.setBlockTime(data.getLong("timestamp"));
            } else if (status == 2) {
                transaction.setStatus(BlockTransaction.PACKED);
                transaction.setBlockTime(data.getLong("timestamp"));
            } else if (status == 3) {
                transaction.setStatus(BlockTransaction.PENDING);
                transaction.setPendingTime(data.getLong("timestamp"));
            } else {
                transaction.setStatus(BlockTransaction.UNKNOWN);
            }
            transaction.setTxId(data.getString("hash"));
            transaction.setHeight(data.getInteger("height"));
            transaction.setTokenName(CoinType.ETH.getName());
            transaction.setIndex(data.getInteger("tranxIndex"));

        } else if (CoinType.BTC.getCode().equals(body.getCoinType())
                || CoinType.BCH.getCode().equals(body.getCoinType())) {
            String tokenName = CoinType.BTC.getCode().equals(body.getCoinType()) ?
                    CoinType.BTC.getName() : CoinType.BCH.getName();
            net.sf.json.JSONObject getTxParam = new net.sf.json.JSONObject();
            getTxParam.put("txid", body.getTxId());
            getTxParam.put("token_type", tokenName);
            JSONObject txInfo = JSONObject.parseObject(httpService.httpPostWithJson(Urls.GET_TX_INFO, getTxParam).toString());
            if (txInfo.containsKey("code") && txInfo.getIntValue("code") != 0) {
                CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
            }

            JSONObject data = txInfo.getJSONObject("data");
            if (Objects.isNull(data) || data.isEmpty()) {
                CustomException.response(txInfo.getString("msg"));
            }
            JSONArray vinArray = data.getJSONArray("vin");
            JSONArray voutArray = data.getJSONArray("vout");
            List<VIn> vInList = new ArrayList<>();
            vinArray.forEach(vin ->{
                VIn vInEntity = new VIn();
                JSONObject vinJson = (JSONObject) vin;
                vInEntity.setAddress(vinJson.getString("input_address"));
                vInEntity.setValue(vinJson.getLong("pre_vout_value"));
                vInEntity.setTxId(vinJson.getString("pre_txid"));
                vInList.add(vInEntity);
            });
            List<VOut> vOutList = new ArrayList<>();
            voutArray.forEach(vout ->{
                VOut voutEntity = new VOut();
                JSONObject voutJson = (JSONObject)vout;
                voutEntity.setAddress(voutJson.getString("output_address"));
                voutEntity.setValue(voutJson.getLong("vout_value"));
                vOutList.add(voutEntity);
            });

            Integer status = BlockTransaction.UNKNOWN;
            switch (data.getIntValue("status")) {
                case 0:
                    status = BlockTransaction.PENDING;
                    break;
                case 1:
                    status = BlockTransaction.CONFIRMED;
                    break;
                case 2:
                    status = BlockTransaction.FAILED;
                    break;
                case 3:
                    status = BlockTransaction.PACKED;
                    break;
                default:
                    status = BlockTransaction.UNKNOWN;
                    break;
            }
            transaction.setStatus(status);
            transaction.setBlockTime(data.getLong("block_time"));
            transaction.setHeight(data.getInteger("block_height"));
            transaction.setVOuts(vOutList);
            transaction.setVIns(vInList);
            transaction.setTxId(data.getString("txid"));
            transaction.setTokenName(tokenName);
            transaction.setIndex(data.getInteger("txid_index"));

        } else {
            CustomException.response(Error.ERR_MSG_TOKEN_NOT_SUPPORT);
        }

        Token token = tokenRepository.findByName(body.getTokenName().toUpperCase());//待确认
        res.setCurrentHeight(this.getBlockHeight(new GetBlockHeightReqDTO(body.getCoinType())));
        res.setDecimal(token.getDecimal());
        res.setName(token.getName());
        res.setPrice("" + flatMoneyService.tokenPriceAdaptation(token.getName()));
        res.setTransaction(transaction);

        return res;
    }




    @Autowired
    private Web3jClient web3jClient;
    @Autowired
    private ETHNonceService ethNonceService;
    @Override
    public Map<String, String> sendTransactionEth(SendTranxEReqDTO body) {
        Web3j web3j = web3jClient.getWeb3j();
        Long nowTime = System.currentTimeMillis();
        RechargeRecord rechargeRecord = new RechargeRecord();
        rechargeRecord.setValue(body.getValue());
        rechargeRecord.setCreateTime(nowTime);
        rechargeRecord.setTokenName(body.getTokenName().toUpperCase());
        rechargeRecord.setRechargeTime(nowTime);
        rechargeRecord.setTraderAddress(body.getAddress());
        rechargeRecord.setChannel(RechargeRecord.RECORD_CHANNEL_BEECOIN);
        Integer type = null;
        if (Arrays.asList(CONTRACT_METHOD_DEPOSIT_ETH, CONTRACT_METHOD_DEPOSIT_TOKEN).
                contains(body.getMethod())) {
            type = 1;
        } else if (CONTRACT_METHOD_TOKEN_APPROVE.equalsIgnoreCase(body.getMethod())) {
            type = 3;
        }
        rechargeRecord.setType(type);
        EthSendTransaction transaction = null;
        try {
            transaction =  web3j.ethSendRawTransaction(body.getTransaction()).send();
            Response.Error error = transaction.getError();
            if (error != null) {
                String errorMsg = error.getMessage();
                logger.error(errorMsg);
                if ("replacement transaction underpriced".equalsIgnoreCase(error.getMessage())) {
                    CustomException.response(Error.ERR_MSG_WAILT_OT_REPLACE);
                } else {
                    CustomException.response(-1, errorMsg);
                }
            }


            //如果广播的是充值或授权交易
            if (Arrays.asList(CONTRACT_METHOD_DEPOSIT_ETH,
                    CONTRACT_METHOD_DEPOSIT_TOKEN,
                    CONTRACT_METHOD_TOKEN_APPROVE).
                    contains(body.getMethod())) {
                rechargeRecord.setTxId(transaction.getTransactionHash());
                commonDao.insert(rechargeRecord);
            } else {
                saveBeecoinTranx(transaction.getTransactionHash());
            }
        } catch (Exception e) {
            e.printStackTrace();
            //如果充值交易保存数据异常，先暂时保存到redis后由定时任务进行补充
            if (e instanceof BadSqlGrammarException) {
               redisTemplate.opsForList().leftPush(Constant.LOST_RECHARGE_RECORD_KEY,
                       JSONObject.toJSONString(rechargeRecord));

            } else if (e instanceof IOException) {
                CustomException.response(Error.ERR_MSG_SEND_TRANSACTION_FAI);
            } else if (e instanceof CustomException){
                throw (CustomException) e;
            } else {
                CustomException.response(Error.SERVER_EXCEPTION);
            }

        }

        Map<String, String> result = new HashMap<>();
        String txId = transaction.getTransactionHash();
        result.put("txId", txId);

        EthTransaction ethTransaction = new EthTransaction();
        Long nonce ;
        try {
             ethTransaction = web3j.ethGetTransactionByHash(txId).send();
             nonce = ethTransaction.getResult().getNonce().longValue();
        } catch (IOException e) {
            e.printStackTrace();
            nonce = body.getNonce();
        }

        ethNonceService.saveNonce(body.getAddress(), nonce, transaction.getTransactionHash());

        //保存兑换记录
        saveExInfo(body.getExInfo(), txId);

        return result;
    }


    /**
     *保存兑换记录
     */
    private void saveExInfo(ExInfoReqDTO exInfo, String txId) {
        if (exInfo != null) {
            ExRecord exRecord = new ExRecord();
            BeanCopier.getInstance().copyBean(exInfo, exRecord);
            exRecord.setStatus(ExInfoReqDTO.EXCHANGE);
            exRecord.setCreateTime(new Date());
            exRecord.setChannel(ExInfoReqDTO.BEECOIN_CHANNEL);
            exRecord.setFromHash(txId);
            commonDao.insert(exRecord);
        }
    }


    @Override
    public List<Rate> getAllRate() {
        List<Rate> rates = new ArrayList<>();

        List<Money> moneyList = Arrays.asList(Money.values());
        moneyList.forEach(money -> {
            Rate rate = new Rate();
            rate.setSymbol(money.getSymblo());
            rate.setcName(money.getName());
            rate.setName(money.getCode());
            rate.setRate(
                    flatMoneyService.getRate(Money.USD.getCode(),
                            money.getCode())
            );
            rates.add(rate);
        });

        return rates;

    }

    @Override
    public Integer getBlockHeight(GetBlockHeightReqDTO body) {

        String strHeight = redisTemplate.opsForValue().
                get(Constant.BLOCK_CURRENT_HEIGHT + body.getCoinType());

        return  Long.valueOf(strHeight).intValue();
    }

}
