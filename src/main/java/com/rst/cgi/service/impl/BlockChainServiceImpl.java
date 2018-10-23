package com.rst.cgi.service.impl;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Hint;
import com.rst.cgi.common.constant.Urls;
import com.rst.cgi.common.enums.CoinType;
import com.rst.cgi.common.utils.HttpService;
import com.rst.cgi.common.utils.MiscUtil;
import com.rst.cgi.common.utils.RpcClient;
import com.rst.cgi.data.dao.mongo.*;
import com.rst.cgi.data.dao.mysql.*;
import com.rst.cgi.data.dto.*;
import com.rst.cgi.data.dto.request.ExInfoReqDTO;
import com.rst.cgi.data.dto.request.TransactionHistoryReq;
import com.rst.cgi.data.dto.response.TransactionHistoryRes;
import com.rst.cgi.data.entity.*;
import com.rst.cgi.service.BlockChainService;
import com.rst.cgi.service.PushDeviceService;
import com.rst.cgi.service.ThirdService;
import com.rst.cgi.service.thrift.gen.pushserver.PushService;
import com.rst.thrift.export.ThriftClient;
import lombok.Data;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import service.AddressClient;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Service
public class    BlockChainServiceImpl implements BlockChainService {
    private final Logger logger = LoggerFactory.getLogger(BlockChainServiceImpl.class);

    @Value("${redis.message.channel.eth-transaction:124}")
    private String ethMsgTopic;
    @Value("${redis.message.channel.btc-transaction}")
    private String btcMsgTopic;
    @Value("${redis.message.channel.bch-transaction}")
    private String bchMsgTopic;
    @Value("${redis.message.channel.whc-transaction}")
    private String whcMsgTopic;
    @Value("${redis.message.channel.eos-transaction}")
    private String eosMsgTopic;
    @Value("${redis.message.channel.usdt-transaction}")
    private String usdtMsgTopic;
    @Value("${redis.message.channel.gateway-transaction}")
    private String GateWayMsgTopic;
    @Value("${redis.message.channel.zcash-transaction}")
    private String zcashMsgTopic;
    @Value("${net.work.type:test_reg}")
    private String networkType;
    @Value("${eth.node.url}")
    private String ethNodeUrl;
    private static final String BLOCK_CURRENT_HEIGHT = "BLOCK_CURRENT_HEIGHT.";

    private static final int BTC_TYPE = 0;
    private static final int BCH_TYPE = 145;
    private static final int ETH_TYPE = 60;

    private static final String BASE_API_ETH = "/es_api/get_all_eth_tx_by_addr_list";
    private static final String BASE_API_BTC = "/es_api/get_all_btc_or_bch_tranx_by_addr_list";
    private static final String BASE_API_BCH = "/es_api/get_all_btc_or_bch_tranx_by_addr_list";

    @Autowired
    private RedisMessageListenerContainer msgListeners;
    @Autowired
    private RpcClient rpcClient;
    @Autowired
    private WalletDao walletDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ATRRepository atrRepository;
    @Autowired
    private WalletAddressDao walletAddressDao;
    @Autowired
    private Hint hint;
    @Autowired
    private ThriftClient<PushService.Iface> pushServerClient;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private GatewayDao gatewayDao;
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private ExRecordDao exRecordDao;

    @PostConstruct
    public void init(){
        //初始化最新区块高度
        Arrays.asList(RpcClient.BTC, RpcClient.BCH).forEach(tokenName -> {
            JSONObject obj =  rpcClient.query(tokenName, "getblockcount", null);
            if (!obj.getString("error").equalsIgnoreCase("null") &&
                    obj.getString("result").equalsIgnoreCase("null")){
                logger.error(tokenName + " get last height error");
                return;
            }
            Integer height = obj.getInt("result");

            switch (tokenName) {
                case RpcClient.BTC:
                    stringRedisTemplate.opsForValue().set(BLOCK_CURRENT_HEIGHT + BTC_TYPE, "" + height);
                    break;
                case RpcClient.BCH:
                    stringRedisTemplate.opsForValue().set(BLOCK_CURRENT_HEIGHT + BCH_TYPE, "" + height);
                    break;
                default:
                    break;
            }
        });

        Web3j web3j = Web3j.build(new org.web3j.protocol.http.HttpService(ethNodeUrl));
        Request<?, Web3ClientVersion> request = web3j.web3ClientVersion();
        request.setMethod("eth_blockNumber");
        try {
            String heightStr = request.send().getWeb3ClientVersion();
            Integer height = Integer.valueOf(heightStr.substring(2, heightStr.length()), 16);
            stringRedisTemplate.opsForValue().set(BLOCK_CURRENT_HEIGHT + ETH_TYPE, "" + height);
        } catch (IOException e) {
            logger.info("初始化以太坊最新块高度失败");
            e.printStackTrace();
        }
    }

    @Autowired
    private ATTRepository attRepository;
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private ThirdService thirdService;
    @Autowired
    private HttpService httpService;
    @Autowired
    private PushDeviceService pushDeviceService;

    @Override
    public TransactionHistoryRes getTransactionHistory(TransactionHistoryReq request) {
        List<String> addresses = request.getTokenData().stream()
               .map(token -> token.getAddresses())
               .reduce(new ArrayList<>(), (dest, src) -> {
                   dest.addAll(src);return dest;
               }).stream().map(item -> item.getHash()).collect(Collectors.toList());

        List<String> tokenNames = request.getTokenData().stream()
                                         .map(token -> token.getName()).collect(Collectors.toList());

       updateTxHistoryFromBlockChain(request.getTokenData());

        List<Boolean> isRollOutList = new ArrayList<>();
        switch (request.getTxType()) {
            case TransactionHistoryReq.TX_ROLL_IN:
                isRollOutList.add(false);
                break;
            case TransactionHistoryReq.TX_ROLL_OUT:
                isRollOutList.add(true);
                break;
            default:
                isRollOutList.add(false);
                isRollOutList.add(true);
                break;
        }

        //从缓存（mongodb）获取历史交易记录
        List<AddressToTransaction> atts = new ArrayList<>();
        if (request.getPageNo() == -1) {
            switch (request.getTimeType()) {
                case TransactionHistoryReq.TIME_TYPE_BLOCK:
                    atts.addAll(attRepository.findAllByRollOutInAndTokenInAndAddressInAndBlockTimeBetween(
                            isRollOutList, tokenNames, addresses,
                            request.getBeginTime(), request.getEndTime()));
                    break;
                case TransactionHistoryReq.TIME_TYPE_CONFIRMED:
                    atts.addAll(attRepository.findAllByRollOutInAndTokenInAndAddressInAndConfirmedTimeBetween(
                            isRollOutList, tokenNames, addresses,
                            request.getBeginTime(), request.getEndTime()));
                    break;
                case TransactionHistoryReq.TIME_TYPE_PENDING:
                    atts.addAll(attRepository.findAllByRollOutInAndTokenInAndAddressInAndPendingTimeBetween(
                            isRollOutList, tokenNames, addresses,
                            request.getBeginTime(), request.getEndTime()));
                    break;
                default:
                    break;
            }
        } else {
            PageRequest pageRequest = new PageRequest
                    (request.getPageNo(),request.getPageSize(),
                            new Sort(Sort.Direction.DESC, "confirmedTime"));
            Page<AddressToTransaction> result = null;
            switch (request.getTimeType()) {
                case TransactionHistoryReq.TIME_TYPE_BLOCK:
                    result = attRepository.findAllByRollOutInAndTokenInAndAddressInAndBlockTimeBetween(
                            isRollOutList, tokenNames, addresses,
                            request.getBeginTime(), request.getEndTime(), pageRequest);
                    break;
                case TransactionHistoryReq.TIME_TYPE_CONFIRMED:
                    result = attRepository.findAllByRollOutInAndTokenInAndAddressInAndConfirmedTimeBetween(
                            isRollOutList, tokenNames, addresses,
                            request.getBeginTime(), request.getEndTime(), pageRequest);
                    break;
                case TransactionHistoryReq.TIME_TYPE_PENDING:
                    result = attRepository.findAllByRollOutInAndTokenInAndAddressInAndPendingTimeBetween(
                            isRollOutList, tokenNames, addresses,
                            request.getBeginTime(), request.getEndTime(), pageRequest);
                    break;
                default:
                    break;
            }

            atts.addAll(result.getContent());
        }

        Map<String, Boolean> txIdToRollOut = new HashMap<>();
        atts.stream().forEach(att -> {
            Boolean value = txIdToRollOut.get(att.getTxId());
            if (value == null || !value) {
                txIdToRollOut.put(att.getTxId(), att.getRollOut());
            }
        });

        List<BlockTransaction> blockTransactions =
                blockRepository.findAllByTxIdIn(
                        atts.stream().map(att -> att.getTxId()).collect(Collectors.toList()));

        List<TransactionHistoryRes.TransactionItem> transactionItems =
                blockTransactions.stream().map(transaction -> {
                    Boolean isRollOut = txIdToRollOut.get(transaction.getTxId());
                    TransactionHistoryRes.TransactionItem transactionItem = new TransactionHistoryRes.TransactionItem();
                    transactionItem.setTransaction(transaction);
                    transactionItem.setRollOut(isRollOut);
                    return transactionItem;
                }).collect(Collectors.toList());

        List<Token> tokens = tokenRepository.findByNameIn(tokenNames);
        List<TransactionHistoryRes.TokenItem> tokenItems =
                tokens.stream().map(token -> {
                    TransactionHistoryRes.TokenItem tokenItem = new TransactionHistoryRes.TokenItem();
                    tokenItem.setName(token.getName());
                    tokenItem.setDecimal(token.getDecimal());
                    tokenItem.setPrice("" + thirdService.getUSDByToken(token.getName()));
                    String strHeight = stringRedisTemplate.opsForValue().get(BLOCK_CURRENT_HEIGHT + token.getCoinType());
                    int currentHeight = StringUtils.isEmpty(strHeight) ? 0 : Integer.parseInt(strHeight);

                    tokenItem.setCurrentHeight(currentHeight);
                    return tokenItem;
                }).collect(Collectors.toList());

        TransactionHistoryRes res = new TransactionHistoryRes();
        res.setTokens(tokenItems);
        res.setTransactions(transactionItems);

        return res;
    }

    private String keyFrom(String token, String address, int type) {
        return address + "." + token + "." + type;
    }

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Scheduled(fixedDelay = 60000)
    public void handleBCHRedisMessage() {
        handleRedisMessage(bchMsgTopic, message -> onBitCoinMsgComing(CoinType.BCH.getName(), message));
//        handleRedisMessage(bchMsgTopic);
    }

    @Scheduled(fixedDelay = 10000)
    public void handleETHRedisMessage() {
        handleRedisMessage(ethMsgTopic, message -> onETHMsgComing(message));
//        handleRedisMessage(ethMsgTopic);

    }

    @Scheduled(fixedDelay = 50000)
    public void handleBTCRedisMessage() {
         handleRedisMessage(btcMsgTopic, message -> onBitCoinMsgComing(CoinType.BTC.getName(), message));
//        handleRedisMessage(btcMsgTopic);

    }

    @Scheduled(fixedDelay = 70000)
    public void handleEOSRedisMessage() {
        handleRedisMessage(eosMsgTopic, message -> onEosMsgComing(message));
//        handleRedisMessage(btcMsgTopic);
    }

    @Scheduled(fixedDelay = 40000)
    public void handleWHCRedisMessage() {
        handleRedisMessage(whcMsgTopic, message -> onContractTokenBMsgComing(CoinType.WHC.getName(), message));
//        handleRedisMessage(whcMsgTopic);

    }

    @Scheduled(fixedDelay = 40000)
    public void handleUsdtRedisMessage() {
        handleRedisMessage(usdtMsgTopic, message -> onContractTokenBMsgComing(CoinType.usdt.getName(), message));

    }

    @Scheduled(fixedDelay = 40000)
    public void handleGateRedisMessage() {
        handleRedisMessage(GateWayMsgTopic, message -> onGatewayMsgComing(message));

    }

    @Scheduled(fixedDelay = 40000)
    public void handleZCASHRedisMessage() {
        handleRedisMessage(zcashMsgTopic, message -> onZCASHMsgComing(message));

    }


    private interface RedisMessageHandler {
        /**
         * 消息处理器
         * @param message
         */
        void handle(String message);
    }

    private void handleRedisMessage(String redisKey, RedisMessageHandler handler) {
            int count = 0;
            long start = System.currentTimeMillis();

            ListOperations operations = stringRedisTemplate.opsForList();
            do {
                String message = (String)operations.rightPop(redisKey);

                if (StringUtils.isEmpty(message)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    message = (String)operations.rightPop(redisKey);
                    if (StringUtils.isEmpty(message)) {
                        break;
                    }
                }
                if (handler != null) {
                    try {
                        handler.handle(new String(message));
                        count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            } while (true);

//            logger.info("Consume Message|redisKey:{}, count:{}, cost:{}", redisKey, count, System.currentTimeMillis() - start);
        }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTxHistoryFromBlockChain(List<TokenData> tokenAddressList) {
        List<String> atrAdressList = new ArrayList<>();
        tokenAddressList.stream().forEach(
                tokenData -> tokenData.getAddresses().forEach(
                        address -> atrAdressList.add(
                                address.getHash() + tokenData.getName() + address.getType())));

        //从mongo查出在atrList中的数据，这些数据对应的address的交易记录已经全部同步完成
        Set<String> atrFilter = atrRepository.findAllByAtrIn(atrAdressList)
                .stream()
                .map(atr -> atr.getAtr())
                .collect(Collectors.toSet());

        //同步缓存
        List<TokenData> tokenDataToSync = new ArrayList<>();
        //找出请求中还没有同步完交易记录的地址重新组成TokenData的列表
        tokenAddressList.forEach(tokenData -> {
            List<TokenData.Address> addressNotSync =
                    tokenData.getAddresses().stream()
                            .filter(address ->
                                    !atrFilter.contains(address.getHash() + tokenData.getName() + address.getType()))
                            .collect(Collectors.toList());
            if (addressNotSync != null && !addressNotSync.isEmpty()) {
                tokenDataToSync.add(new TokenData(tokenData.getName(), addressNotSync, null));
            }
        });

        if (tokenDataToSync.isEmpty()) {
            return;
        }

        String api = "http://47.98.185.203:8000";

        List<BlockTransaction> transactions = new ArrayList<>();
        tokenDataToSync.stream().forEach(tokenAddress -> {
            Token token = tokenRepository.findByName(tokenAddress.getName());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("token_type", token.getName());
            JSONArray jsonArray = new JSONArray();
            tokenAddress.getAddresses().forEach(address -> {
                        jsonArray.add(encodePubHashToAddress(address.getHash(),
                                address.getType(), token.getCoinType()));
                    });
            jsonObject.put("addr_list", jsonArray);
            JSONObject result = httpService.httpPostWithJson(apiFrom(token), jsonObject);
            transactions.addAll(resultToTransactions(tokenAddress, result));
        });

        saveTransactions(transactions);

        List<AddressTxReady> atrList = new ArrayList<>();
        tokenDataToSync.stream().forEach(
                tokenData -> tokenData.getAddresses().forEach(
                        address -> atrList.add(new AddressTxReady(
                                address.getHash() + tokenData.getName() + address.getType()))));
        atrRepository.save(atrList);
    }

    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void asyncUpdateTxHistoryFromBlockChain(List<TokenData> tokenAddressList) {
        if (tokenAddressList == null || tokenAddressList.isEmpty()) {
            return;
        }

        if (dbExecutor.isShutdown()) {
            dbExecutor = Executors.newSingleThreadExecutor();
        }

        logger.info("asyncUpdateTxHistoryFromBlockChain:{}", tokenAddressList.size());

        dbExecutor.submit(() -> updateTxHistoryFromBlockChain(tokenAddressList));
    }

    @Value("${usdt.confirm.num:1}")
    private Long USDT_CONFIRM_NUM;
    @Value("${whc.confirm.num:1}")
    private Long WHC_CONFIRM_NUM;
    @Value("${confirm.num:1}")
    private Long CONFIRM_NUM;
    private List<BlockTransaction> resultToTransactions(TokenData tokenAddress, JSONObject result) {
        if (!result.containsKey("code") || result.getInt("code") != 0) {
            return new ArrayList<>();
        }

        Token token = tokenRepository.findByName(tokenAddress.getName());

        List<BlockTransaction> transactions = new ArrayList<>();
        JSONArray datas = result.getJSONArray("data");
        for (int i = 0; i < datas.size(); i++) {
            String data = datas.getString(i);

            if (token == null) {
                logger.info("Not supported token:{}", tokenAddress.getName());
                continue;
            }

            if (token.getCoinType() == BTC_TYPE || token.getCoinType() == BCH_TYPE) {
                if (CoinType.WHC.getName().equals(token.getName())) {
                    ContractTokenBMessage contractTokenBMessage =
                            new Gson().fromJson(data, ContractTokenBMessage.class);
                    transactions.add(contractTokenBMessage.toTransaction(token, WHC_CONFIRM_NUM));
                } else if ( CoinType.usdt.getName().equals(token.getName())) {
                    ContractTokenBMessage contractTokenBMessage =
                            new Gson().fromJson(data, ContractTokenBMessage.class);
                    transactions.add(contractTokenBMessage.toTransaction(token, USDT_CONFIRM_NUM));
                } else if (CoinType.BTC.getName().equals(token.getName())){
                    String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT;
                    Long needConfirNum = 0l;

                    blockHeightKey = blockHeightKey + CoinType.BTC.getCode();
                    needConfirNum = CONFIRM_NUM;

                    String strHeight = stringRedisTemplate.opsForValue().get(blockHeightKey);
                    Long currentHeight = org.apache.commons.lang.StringUtils.isBlank(strHeight) ? 0l : Long.parseLong(strHeight);

                    BitCoinMessage bitCoinMessage = new Gson().fromJson(data, BitCoinMessage.class);
                    transactions.add(bitCoinMessage.toTransaction(token, needConfirNum, currentHeight));
                } else {
                    String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT;
                    Long needConfirNum = 0l;

                    blockHeightKey = blockHeightKey + CoinType.BCH.getCode();
                    needConfirNum = WHC_CONFIRM_NUM;

                    String strHeight = stringRedisTemplate.opsForValue().get(blockHeightKey);
                    Long currentHeight = org.apache.commons.lang.StringUtils.isBlank(strHeight) ? 0l : Long.parseLong(strHeight);

                    BitCoinMessage bitCoinMessage = new Gson().fromJson(data, BitCoinMessage.class);
                    transactions.add(bitCoinMessage.toTransaction(token, needConfirNum, currentHeight));
                }

            } else if (token.getCoinType() == ETH_TYPE) {
                ETHMessage ethMessage = new Gson().fromJson(data, ETHMessage.class);
                transactions.add(ethMessage.toTransaction(token));
            }
        }

        return transactions;
    }

    @Value("${server.bsm.uri}")
    private String api;
    private String apiFrom(Token token) {
        int coinType = token.getCoinType();
        switch (coinType) {
            case ETH_TYPE:
                return api + BASE_API_ETH;
            case BCH_TYPE:
                if (CoinType.BCH.getName().equals(token.getName())) {
                    return api + BASE_API_BCH;
                } else if (CoinType.WHC.getName().equals(token.getName())) {
                    return Urls.GET_WHC_TRANSACTION;
                }
            case BTC_TYPE:
                if (CoinType.BTC.getName().equals(token.getName())) {
                    return api + BASE_API_BTC;
                } else if (CoinType.usdt.getName().equals(token.getName())) {
                    return Urls.GET_usdt_TRANSACTION;
                }

            default:
                break;
        }
        return api;
    }

    //only support SCRIPT_P2PKH
    private String encodePubHashToAddress(String pubHash, int type, int coinType) {
        String typeStr = "p2pkh";
        if (type == 1) {
            typeStr = "p2sh";
        }
        switch (coinType) {
            case BTC_TYPE:
                return AddressClient.toLegacyAddressByPubkHash(pubHash,
                        typeStr, networkType);
            case BCH_TYPE:
                return AddressClient.toCashAddressByPubkHash(pubHash,
                        typeStr, networkType);
            case ETH_TYPE:
                return AddressClient.toEtherAddressByHash(pubHash);
            default:
                break;
        }
        return "";
    }

    private boolean updateBlockHeight(Token token, Integer newHeight) {
        if (newHeight == null) {
            return false;
        }

        String key = BLOCK_CURRENT_HEIGHT + token.getCoinType();
        boolean changed = false;
        int currentHeight = 0;
        if (stringRedisTemplate.hasKey(key)) {
            currentHeight = Integer.parseInt(stringRedisTemplate.opsForValue().get(key));
        }

        if (currentHeight < newHeight) {
            logger.info("插入记录 key:{}, 高度:{})",key,newHeight);
            stringRedisTemplate.opsForValue().set(key, "" + newHeight);
            changed = true;
        }

        if (changed) {
            //确认进度变化，根据需求推送进度
            logger.info("【{}】块高度变化，当前最新为：{}", token.getName(), newHeight);
        }

        return changed;
    }


    private void onGatewayMsgComing(String message) {
        GatewayMessage gatewayMessage = new Gson().fromJson(message, GatewayMessage.class);
        logger.info("【GATEWAY】交易推送 fromHash:{}, gateHash:{}, toHash:{}",
                gatewayMessage.getFromTxHash(), gatewayMessage.getDgwHash(), gatewayMessage.getToTxHash());

        //查询是兑换记录是否已经存在 1，如果存在讲状态改成已完成；2：如果不存在并且fromAddress是我们钱包的地址就保存并且状态是已经完成
        String fromHash = gatewayMessage.getFromTxHash();
        List<ExRecord> exRecords = exRecordDao.queryByFromHash(fromHash);
        List<Integer> exRecordIds = new ArrayList<>();
        exRecords.forEach(exRecord -> {
            exRecordIds.add(exRecord.getId());
        });

        List<String> fromAddressHashList = new ArrayList<>();
        String toAddressHash;
        List<String> fromAddressList = Arrays.asList(gatewayMessage.getFromAddr().split(","));
        String toAddress = gatewayMessage.getToAddrs();
        fromAddressList.forEach(address -> {
            fromAddressHashList.add(AddressClient.addressToHash(CoinType.valueOf(gatewayMessage.getFromChain().toUpperCase()).getCode(),
                    address, networkType));
        });
        toAddressHash = AddressClient.addressToHash(CoinType.valueOf(gatewayMessage.getToChain().toUpperCase()).getCode(),
                toAddress, networkType);

        List<String> addressHashList = new ArrayList<>();
        addressHashList.addAll(fromAddressHashList);
        addressHashList.add(toAddressHash);
        boolean needPush = true;
        if (Objects.nonNull(exRecords) && !exRecords.isEmpty()) {
            exRecordDao.updateRecordStatusAndTxFee(ExInfoReqDTO.SUCCESS,
                    gatewayMessage.getFromFee(), exRecordIds);
        } else {
            Set<String> walletHashList =  walletDao.queryPbkByAddress(addressHashList);
            if (Objects.nonNull(walletHashList) && walletHashList.size() != 0) {
                List<ExRecord> exRecordSaveList = new ArrayList<>();
                walletHashList.forEach(walletHash -> {
                    ExRecord exRecordSave = new ExRecord();
                    exRecordSave.setStatus(ExInfoReqDTO.SUCCESS);
                    exRecordSave.setTxFee(gatewayMessage.getFromFee());
                    exRecordSave.setToValue(gatewayMessage.getFinalAmount());
                    exRecordSave.setToName(gatewayMessage.getToTokenSymbol());
                    exRecordSave.setFromValue(gatewayMessage.getAmount());
                    exRecordSave.setFromName(gatewayMessage.getTokenSymbol());
                    exRecordSave.setFromHash(gatewayMessage.getFromTxHash());
                    exRecordSave.setCreateTime(new Date(gatewayMessage.getTime() * 1000));
                    exRecordSave.setChannel(ExInfoReqDTO.OTHER_CHANNEL);
                    exRecordSave.setWalletHash(walletHash);
                    exRecordSaveList.add(exRecordSave);
                });
                commonDao.batchInsert(exRecordSaveList, ExRecord.class);
            } else {
                needPush = false;
            }
        }

        if (needPush) {
            //推送网关兑换成功消息
            List<String> fromEmHd = pushDeviceService.getAvailableDevices(getArray(fromAddressHashList));
            List<String> toEmHd = pushDeviceService.getAvailableDevices(getArray(Arrays.asList(toAddressHash)));
            BlockTransaction transaction = new BlockTransaction();
            transaction.setTokenName(gatewayMessage.getTokenSymbol() + "->" + gatewayMessage.getToTokenSymbol());
            transaction.setTxId(gatewayMessage.getDgwHash());
            transaction.setStatus(BlockTransaction.EXCHANGE_SUCCESS);
            pushToClient(transaction, fromEmHd, toEmHd);
        }
    }

    public void onZCASHMsgComing(String message) {
        System.out.println("zcashmessage------" + message);
        ZcashMessage zcashMessage = new Gson().fromJson(message, ZcashMessage.class);
        Token token = tokenRepository.findByName(CoinType.ZEC.getName());
        if (token == null) {
            logger.info("Not supported token:{}", CoinType.ZEC.getName());
            return;
        }
        handleTransaction(zcashMessage.toTransaction(token),token);
    }


    private void onContractTokenBMsgComing(String tokenName, String message) {
        ContractTokenBMessage contractTokenBMessage = new Gson().fromJson(message, ContractTokenBMessage.class);

        Token token = tokenRepository.findByName(tokenName);
        if (token == null) {
            logger.info("Not supported token:{}", tokenName);
            return;
        }


        Long confirmNum = 6l;
        if (CoinType.WHC.getName().equals(tokenName)) {
            confirmNum = WHC_CONFIRM_NUM;
        } else if (CoinType.usdt.equals(tokenName)) {
            confirmNum = USDT_CONFIRM_NUM;
        }

        handleTransaction(contractTokenBMessage.toTransaction(token, confirmNum), token);
    }

    private void onEosMsgComing(String message){
        EOSMessage eosMessage = new Gson().fromJson(message, EOSMessage.class);
        Token token = tokenRepository.findByName(CoinType.EOS.getName());
        if (token == null) {
            logger.info("Not supported token:EOS", CoinType.EOS.getName());
            return;
        }
        handleTransaction(eosMessage.totransaction(token),token);

    }
    private void onBitCoinMsgComing(String tokenName, String message) {
//        logger.info("token:{},message:{}",tokenName,message);
        BitCoinMessage bitCoinMessage = new Gson().fromJson(message, BitCoinMessage.class);

        Token token = tokenRepository.findByName(tokenName);
        if (token == null) {
            logger.info("Not supported token:BTC", tokenName);
            return;
        }

        String blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT;
        Long needConfirNum = 0l;
        if (CoinType.BTC.getName().equals(tokenName)) {
            blockHeightKey = blockHeightKey + CoinType.BTC.getCode();
            needConfirNum = CONFIRM_NUM;
        } else {
            blockHeightKey = blockHeightKey + CoinType.BCH.getCode();
            needConfirNum = WHC_CONFIRM_NUM;
        }

        String strHeight = stringRedisTemplate.opsForValue().get(blockHeightKey);
        Long currentHeight = org.apache.commons.lang.StringUtils.isBlank(strHeight) ? 0l : Long.parseLong(strHeight);
        handleTransaction(bitCoinMessage.toTransaction(token, needConfirNum, currentHeight), token);
    }

    private void onETHMsgComing(String message) {
//        logger.info("esomessage:{}",message);
        ETHMessage ethMessage = new Gson().fromJson(message, ETHMessage.class);

        if (!isTransactionETH(ethMessage)) {
            return;
        }

        Token token;
        if (!StringUtils.isEmpty(ethMessage.getContract())) {
            token = tokenRepository.findByAddress(ethMessage.getContract().toLowerCase());
        } else {
            token = tokenRepository.findByName("ETH");
        }

        if (token == null) {
            return;
        }

        //只更新高度
        if (ethMessage.getStatus() == 4) {
            updateBlockHeight(token, ethMessage.getHeight());
            return;
        }

        BlockTransaction blockTransaction = ethMessage.toTransaction(token);

        handleTransaction(blockTransaction,  token);
    }

    /**
     * 检查以太坊交易是否是一个常规的交易
     * @param ethMessage
     * @return
     */
    private Boolean isTransactionETH(ETHMessage ethMessage){
        boolean result = false;
        List<String> conntractMethod = Arrays.asList("transfer", "transferFrom");
        if ("transaction".equalsIgnoreCase(ethMessage.getMethod())) {
           result = true;
        } else if ("contractCall".equalsIgnoreCase(ethMessage.getMethod())) {
            Map<String, Object> inputData = ethMessage.getInputData();
            if (inputData != null &&  inputData.get("method") != null) {
                String inputMethod = (String) inputData.get("method");
                if (conntractMethod.contains(inputMethod)) {
                    result = true;
                }
            }
        }
        return result;
    }

    private void saveTransactions(List<BlockTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }

        List<AddressToTransaction> attList = new ArrayList<>();
        List<BlockTransaction> saveTransactions = new ArrayList<>();

        transactions.forEach(transaction -> {
            Token token = tokenRepository.findByName(transaction.getTokenName());
            Set<String> inAddress = transaction.inAddressList().stream().map(
                    address -> AddressClient.addressToHash(token.getCoinType(), address, networkType))
                                                .filter(address -> !StringUtils.isEmpty(address))
                                                .collect(Collectors.toSet());

            Set<String> outAddress = transaction.outAddressList().stream().map(
                    address -> AddressClient.addressToHash(token.getCoinType(), address, networkType))
                                                 .filter(address -> !StringUtils.isEmpty(address)
                                                         && !inAddress.contains(address))
                                                 .collect(Collectors.toSet());

            inAddress.forEach(address -> attList.add(latestATT(transaction, address, true)));
            outAddress.forEach(address -> attList.add(latestATT(transaction, address, false)));

            BlockTransaction find = blockRepository.findByTxId(transaction.getTxId());
            if (find != null) {
                MiscUtil.fill(find, transaction);
            } else {
                find = transaction;
            }
            saveTransactions.add(find);
        });


        blockRepository.save(saveTransactions);
        attRepository.save(attList);
    }

    private List<String> combine(List<String> dest, List<String> next) {
        dest.addAll(next);
        return dest;
    }

    private AddressToTransaction latestATT(BlockTransaction transaction, String address, boolean rollOut) {
        AddressToTransaction att = attRepository.findByTxIdAndAddress(transaction.getTxId(), address);
        if (att == null) {
            att = new AddressToTransaction();
        }
        att.setAddress(address);
        att.setTxId(transaction.getTxId());
        att.setBlockTime(transaction.getBlockTime());
        att.setConfirmedTime(transaction.getConfirmedTime());
        att.setStatus(transaction.getStatus());
        att.setPendingTime(transaction.getPendingTime());
        att.setToken(transaction.getTokenName());
        if (!Boolean.TRUE.equals(att.getRollOut()) ) {
            att.setRollOut(rollOut);
        }
        return att;
    }

    private void handleTransaction(BlockTransaction transaction, Token token){
        updateBlockHeight(token, transaction.getHeight());
        Set<String> inAddress = Sets.newHashSet();
        Set<String> outAddress = Sets.newHashSet();
        if(transaction.getChannel() != null && transaction.getChannel().equalsIgnoreCase("eos")){
            List<VIn> vIns = transaction.getVIns();
            String in = vIns.get(0).getAddress();
            String out = transaction.getVOuts().get(0).getAddress();
            List<EosAccount> inEosAccountDto = walletAddressDao.queryEosAccount(in);
            if(inEosAccountDto != null && inEosAccountDto.size() != 0){
                inAddress.add(inEosAccountDto.get(0).getWalletAddress());
            }
            List<EosAccount> outEosAccountDto = walletAddressDao.queryEosAccount(out);
            if(outEosAccountDto != null && outEosAccountDto.size() != 0){
                outAddress.add(outEosAccountDto.get(0).getWalletAddress());
            }
            if ((inAddress == null || inAddress.isEmpty())
                    && (outAddress == null || outAddress.isEmpty()) ) {
                return;
            }

        }else{
            if (token.getCoinType().equals(133)){
                if (transaction.inAddressList().size() != 0){
                    for(String s :transaction.inAddressList()){
                        try {
                            inAddress.add(AddressClient.addressToZcashHash(token.getCoinType(), s, networkType));
                        } catch ( Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                inAddress = transaction.inAddressList().stream().map(
                        address -> AddressClient.addressToHash(token.getCoinType(), address, networkType))
                        .filter(address -> !StringUtils.isEmpty(address))
                        .collect(Collectors.toSet());
            }
            final Set<String> inAddressCopy = inAddress;
            if (token.getCoinType().equals(133)){
                if (transaction.outAddressList().size() != 0) {
                    for (String s :transaction.outAddressList()) {
                        try {
                            outAddress.add(AddressClient.addressToZcashHash(token.getCoinType(), s, networkType));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                outAddress = transaction.outAddressList().stream().map(
                        address -> AddressClient.addressToHash(token.getCoinType(), address, networkType))
                        .filter(address -> !StringUtils.isEmpty(address)
                                && !inAddressCopy.contains(address))
                        .collect(Collectors.toSet());
            }
            //过滤掉在mysql中不存在的钱包地址
            if (inAddress != null && !inAddress.isEmpty()) {
                inAddress = walletAddressDao.queryByAddressList(new ArrayList<>(inAddress)).stream().
                        map(walletAddress -> walletAddress.getWalletAddress()).collect(Collectors.toSet());
            }
            if (outAddress != null && !outAddress.isEmpty()) {
                outAddress = walletAddressDao.queryByAddressList(new ArrayList<>(outAddress)).stream().
                        map(walletAddress -> walletAddress.getWalletAddress()).collect(Collectors.toSet());
            }
//        logger.info("inaddress:{},outaddress:{}",inAddress,outAddress);
            if ((inAddress == null || inAddress.isEmpty())
                    && (outAddress == null || outAddress.isEmpty()) ) {
                return;
            }
        }
        //更新索引
        inAddress.forEach(address -> attRepository.save(latestATT(transaction, address, true)));
        outAddress.forEach(address -> attRepository.save(latestATT(transaction, address, false)));

        boolean statusChanged = true;

        //更新数据库交易
        BlockTransaction find = blockRepository.findByTxId(transaction.getTxId());
        if (find != null) {
            statusChanged = !transaction.getStatus().equals(find.getStatus());
            MiscUtil.fill(find, transaction);
        } else {
            find = transaction;
        }
        blockRepository.save(find);

        if (statusChanged) {
            if (transaction.getStatus() == BlockTransaction.PENDING) {
                transaction.setPendingTime(System.currentTimeMillis());
            } else if (transaction.getStatus() == BlockTransaction.CONFIRMED) {
                transaction.setConfirmedTime(System.currentTimeMillis());
            }
        }

        List<String> fromEmHd = new ArrayList<>();
        if (!inAddress.isEmpty()) {
            List<String> addresses = new ArrayList<>();
            addresses.addAll(inAddress);
            fromEmHd = pushDeviceService.getAvailableDevices(getArray(addresses));
        }

        List<String> toEmHd = new ArrayList<>();
        if (!outAddress.isEmpty()) {
            List<String> addresses = new ArrayList<>();
            addresses.addAll(outAddress);
            toEmHd = pushDeviceService.getAvailableDevices(getArray(addresses));
        }

        if (statusChanged) {
            pushToClient(transaction, fromEmHd, toEmHd);
        }

        logger.info("transaction:{}, statusChanged:{}, status:{}, inAddress:{}, outAddress:{}",
                transaction.getTxId(), statusChanged, transaction.getStatus(), inAddress, outAddress);
    }


    private int[] getArray(List<String> addressList){
        List<Integer> walletIdList = walletDao.queryWalletIdByAddressList(addressList);
        logger.info("输入地址：{},查询钱包id:{}",addressList,walletIdList);
        int[] idArray = new int[walletIdList.size()];
        for(int i=0;i<walletIdList.size();i++){
            if (Objects.isNull(walletIdList.get(i))) {
                continue;
            }
            idArray[i]=walletIdList.get(i);
        }
        return idArray;
    }

    private void pushToClient(BlockTransaction transaction, List<String> from, List<String> to) {

        String msg = "";
        String msgType= "";
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = spf.format(new Date());
        boolean needPush = true;

        switch (transaction.getStatus()) {
            //交易已打包
            case BlockTransaction.PACKED:
                //需通知所有关联方
                logger.info("【{}】交易已打包:{},交易状态:{}",
                        transaction.getTokenName(), transaction.getTxId(),transaction.getStatus());
                msg = Hint.TAT_PACKED.pushMsg(hint.getType()).replace("#TOKEN", transaction.getTokenName())
                        .replace("#TIME", now);
                msgType = Constant.TRANSACTION_PACKED;

                break;
            //交易已确认完成
            case BlockTransaction.CONFIRMED:
                //需通知所有关联方
                logger.info("【{}】交易已确认完成:{},交易状态:{}",
                        transaction.getTokenName(), transaction.getTxId(),transaction.getStatus());
//                msg = hint.TAT_CONFIRMED.pushMsg(hint.getType()).replace("#TIME", now);
//                msgType = Constant.TRANSACTION_CONFIRM;
                needPush = false;
                break;
            //交易被节点接收处于等待打包状态
            case BlockTransaction.PENDING:
                //需要通知收款方
                logger.info("【{}】 交易进入缓存池:{},交易状态:{}",
                        transaction.getTokenName(), transaction.getTxId(),transaction.getStatus());
                needPush = false;
                break;
            case BlockTransaction.FAILED:
                //需通知所有关联方
                logger.info("【{}】交易失败:{}",
                        transaction.getTokenName(), transaction.getTxId());
                msg = Hint.TAT_Fail.pushMsg(hint.getType()).replace("#TIME", now);
                msgType = Constant.TRANSACTION_FAIL;
                break;
            case BlockTransaction.EXCHANGE_SUCCESS:
                logger.info("【{}】兑换成功:网关交易hash:{}",
                        transaction.getTokenName(), transaction.getTxId());
                msg = Hint.EXCHANGE_SUCCESS.pushMsg(hint.getType()).replace("#SYMBOL", transaction.getTokenName())
                        .replace("#TIME", now);
                msgType = Constant.EXCHANGE_SUCCESS;
                break;
            default:
                needPush = false;
                break;
        }


        if (needPush) {
            Set<String> allEquipmentSet = new HashSet<>();
            allEquipmentSet.addAll(from);
            allEquipmentSet.addAll(to);
            List<String> allEquipmentList = new ArrayList<>(allEquipmentSet);
            logger.info("push to EquipmentNo:{},msg:{},txId:{}", allEquipmentList, msg,transaction.getTxId());
            try {
                pushServerClient.get(PushService.Iface.class).
                        pushList(transaction.getTxId(), allEquipmentList, msg, msgType, 1);
            } catch (TException e) {
                logger.error("push transaction error");
                e.printStackTrace();
            }
        }
    }


    @Data
    public static class ETHMessage {
        @SerializedName("hash")
        private String txId;
        private String from;
        private Double value;
        private Double txFee;
        @SerializedName("timestamp")
        private Long timestamp;
        private String method;
        private String to;
        private String contract;
        @SerializedName("type")
        private Integer status;
        private Integer height;
        @SerializedName("tranxIndex")
        private Integer index;
        private Boolean success;
        @SerializedName("input")
        private Map<String, Object> inputData;

        public BlockTransaction toTransaction(Token token) {
            BlockTransaction blockTransaction = new BlockTransaction();

            blockTransaction.setTokenName(token.getName());

            if (success != null && !success) {
                blockTransaction.setStatus(BlockTransaction.FAILED);
            } else if (status != null){
                if (status == 1) {
                    blockTransaction.setStatus(BlockTransaction.CONFIRMED);
                } else if (status == 2) {
                    blockTransaction.setStatus(BlockTransaction.PACKED);
                } else if (status == 3) {
                    blockTransaction.setStatus(BlockTransaction.PENDING);
                } else {
                    blockTransaction.setStatus(BlockTransaction.UNKNOWN);
                }
            } else {
                blockTransaction.setStatus(BlockTransaction.UNKNOWN);
            }

            if (blockTransaction.getStatus() == BlockTransaction.PACKED) {
                blockTransaction.setBlockTime(timestamp / 1000);
            }

            if (txId != null) {
                blockTransaction.setTxId(txId);
            }

            if (height != null) {
                blockTransaction.setHeight(height);
            }

            if (from != null) {
                VIn in = new VIn();
                in.setAddress(from);

                in.setValue(value.longValue() + txFee.longValue());
                blockTransaction.setVIns(Arrays.asList(in));
            }

            if (to != null) {
                VOut out = new VOut();
                out.setAddress(to);
                out.setValue(value.longValue());
                blockTransaction.setVOuts(Arrays.asList(out));
            }

            if (index != null) {
                blockTransaction.setIndex(index);
            }

            String inputMethod = null;
            if (Objects.nonNull(inputData) && inputData.containsKey("method")) {
               inputMethod = (String) inputData.get("method");
            }
            if (!"transaction".equalsIgnoreCase(method) &&
                    !"transfer".equalsIgnoreCase(inputMethod) &&
                    !"transferFrom".equalsIgnoreCase(inputMethod)) {
                blockTransaction.setTranxType(BlockTransaction.OTHER);
            }

            return blockTransaction;
        }
    }

    @Data
    public static class BitCoinMessage {
        @SerializedName("txid")
        private String txId;
        @SerializedName("txid_index")
        private Integer index;
        @SerializedName("locktime")
        private Integer lockTime;
        @SerializedName("status")
        private Integer status;
        @SerializedName("block_height")
        private Integer height;
        @SerializedName("block_time")
        private Long blockTime;
        @SerializedName("vin")
        private List<VIn> VIns;
        @SerializedName("vout")
        private List<VOut> VOuts;

        public BlockTransaction toTransaction(Token token, Long needConfirNum, Long currentHeight) {
            BlockTransaction blockTransaction = new BlockTransaction();

            blockTransaction.setTokenName(token.getName());

//            if (status != null) {
//                switch (status) {
//                    case 0:
//                        blockTransaction.setStatus(BlockTransaction.PENDING);
//                        break;
//                    case 1:
//                        blockTransaction.setStatus(BlockTransaction.CONFIRMED);
//                        break;
//                    case 2:
//                        blockTransaction.setStatus(BlockTransaction.FAILED);
//                        break;
//                    case 3:
//                        blockTransaction.setStatus(BlockTransaction.PACKED);
//                        break;
//                    default:
//                        blockTransaction.setStatus(BlockTransaction.UNKNOWN);
//                        break;
//                }
//            } else {
//                blockTransaction.setStatus(BlockTransaction.UNKNOWN);
//            }

            Long differences = height == 0 ? 0 : currentHeight - height + 1;
            Long confirmations = differences > 0l ? differences : 0l;

            if (confirmations == 0) {
                blockTransaction.setStatus(BlockTransaction.PENDING);
            } else if (confirmations < needConfirNum) {
                blockTransaction.setStatus(BlockTransaction.PACKED);
            }  else {
                blockTransaction.setStatus(BlockTransaction.CONFIRMED);
            }

            if (blockTime != null) {
                blockTransaction.setBlockTime(blockTime);
            }

            if (txId != null) {
                blockTransaction.setTxId(txId);
            }

            if (height != null) {
                blockTransaction.setHeight(height);
            }

            if (VIns != null) {
                blockTransaction.setVIns(VIns);
            }

            if (VOuts != null) {
                blockTransaction.setVOuts(VOuts);
            }

            if (index != null) {
                blockTransaction.setIndex(index);
            }

            return blockTransaction;
        }
    }

}
