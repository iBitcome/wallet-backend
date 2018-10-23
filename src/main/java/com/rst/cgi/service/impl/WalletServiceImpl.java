package com.rst.cgi.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.rst.cgi.common.EOS.EOSSignUtil;
import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.constant.Urls;
import com.rst.cgi.common.enums.CoinType;
import com.rst.cgi.common.enums.ScriptType;
import com.rst.cgi.common.utils.*;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mongo.TokenRepository;
import com.rst.cgi.data.dao.mysql.CommonDao;
import com.rst.cgi.data.dao.mysql.WalletAddressDao;
import com.rst.cgi.data.dao.mysql.WalletDao;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.*;
import com.rst.cgi.data.entity.*;
import com.rst.cgi.service.FlatMoneyService;
import com.rst.cgi.service.ThirdService;
import com.rst.cgi.service.UserService;
import com.rst.cgi.service.WalletService;
import lombok.Data;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.AddressClient;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by mtb on 2018/3/29.
 */
@Service
public class WalletServiceImpl implements WalletService {

    private final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    @Autowired
    private HttpService httpService;
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private WalletAddressDao walletAddressDao;
    @Autowired
    private WalletDao walletDao;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private FlatMoneyService flatMoneyService;
//    private ThirdService thirdService;
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void manageWallet(NumbersManageWalletReqDTO body, HttpServletRequest request) {
        if (body.getIsHd() == 1) { //如果是HD钱包
            this.manageHDWallet(body, request);
        } else { //如果是普通钱包
            this.manageSimpleWallet(body, request);
        }
    }


    private void manageHDWallet(NumbersManageWalletReqDTO body, HttpServletRequest request) {
        boolean needActivaty = false;
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        if (user.getStatus() ==2) {
            needActivaty = true;
        }


        Equipment query = new Equipment();
        query.setEquipmentNo(body.getEquipmentNo());
        Equipment equipment = commonDao.queryFirstBy(query);


        //设备保存类
        Equipment equipmentSave = new Equipment();
        equipmentSave.setCreatTime(new Date());
        equipmentSave.setEquipmentNo(body.getEquipmentNo());

        //主公钥保存类
        Wallet hpkSave = new Wallet();
        hpkSave.setOwner(user.getId());
        hpkSave.setCreateTime(new Date());
        hpkSave.setEquipmentNo(body.getEquipmentNo());
        hpkSave.setKeyStatus(1);
        hpkSave.setPublicKey(body.getPublicKey());
        hpkSave.setType(body.getIsHd() == 1 ? 0 : 1);//(需要临时性，需要改进)
        hpkSave.setCreateIp(IpUtil.clientIpFrom(request));


        if (body.getType() == 1) {//添加钱包
            List<HdWalletPath> hdWalletPathList = new ArrayList<>();
            List<WalletAddress> walletAddressList = new ArrayList<>();
//            List<EosAccount> eosAccountList=Lists.newArrayList();
            body.getWalletInfoList().forEach(manageWalletInfo -> {
                if (StringUtils.isBlank(manageWalletInfo.getWalletPathDepth())) {
                    CustomException.response(Error.ERR_MSG_ADD_WALLET_FAIL);
                }
                String PathDepth = manageWalletInfo.getWalletPathDepth();
                String path = PathDepth.substring(0,PathDepth.lastIndexOf("/"));
                Integer depth = Integer.valueOf(PathDepth.substring(path.length() + 1,
                        PathDepth.length()));

                //钱包路径和深度保存类
                HdWalletPath walletPathSave = new HdWalletPath();
                walletPathSave.setDepth(depth);
                walletPathSave.setPath(path);
                hdWalletPathList.add(walletPathSave);
                //钱包地址保存类
                WalletAddress walletAddressSave = new WalletAddress();
                walletAddressSave.setWalletAddress(manageWalletInfo.getWalletAddress());
                walletAddressSave.setCreatTime(new Date());
                walletAddressSave.setStatusCode(1);
                walletAddressSave.setType(manageWalletInfo.getType());
                walletAddressList.add(walletAddressSave);
                /*//EOS账户保存类
                List<String> eosAccount = manageWalletInfo.getAccount();
                if(eosAccount != null && eosAccount.size() != 0) {
                    eosAccount.forEach( account ->{
                        EosAccount eosaccount = new EosAccount();
                        eosaccount.setWalletAddress(manageWalletInfo.getWalletAddress());
                        eosaccount.setEosAccount(account);
                        eosaccount.setCreateTime(new Date());
                        eosAccountList.add(eosaccount);
                    });
                }*/
            });
            if (Objects.isNull(equipment)) {//如果设备不存在（对应的设备主公钥、钱包地址、钱包路径和深度肯定不存在）时
                //保存设备信息
                commonDao.insert(equipmentSave);
                //保存主公钥信息
                commonDao.insert(hpkSave);
                //保存钱包路径和深度
//                walletPathSave.setPublicKeyId(hpkSave.getId());
//                commonDao.insert(walletPathSave);

                //对路径列表中相同路径获取最大的深度的HdWalletPath对象，并放入hdWalletPathSaveList中
                hdWalletPathList.forEach(hdWalletPath ->
                        hdWalletPath.setWalletId(hpkSave.getId()));
                List<HdWalletPath> hdWalletPathSaveList = new ArrayList<>();
                Map<String, List<HdWalletPath>> groupPathList =  hdWalletPathList.stream().
                        collect(Collectors.groupingBy(HdWalletPath::getPath));// 按路径分类
                for ( Map.Entry<String, List<HdWalletPath>> entry: groupPathList.entrySet()) { //每个路径分类列表中获取最大深度的对象
                    hdWalletPathSaveList.add(
                            entry.getValue().stream().max(
                                    Comparator.comparingInt(HdWalletPath::getDepth)).get()
                    );
                }
                commonDao.batchInsert(hdWalletPathSaveList, HdWalletPath.class);

                //保存钱包地址
                walletAddressList.forEach(walletAddress -> walletAddress.setWalletId(hpkSave.getId()));
                commonDao.batchInsert(walletAddressList, WalletAddress.class);
                //保存EOS账户
                /*if(eosAccountList != null && eosAccountList.size() != 0){
                    commonDao.batchInsert(eosAccountList,EosAccount.class);
                }*/
                if (needActivaty) {
                    userService.activationAccount(user.getId());//激活账号
                }

            } else {            //如果设备信息存在
                //先判断该设备主公钥信息是否存在
                Wallet pkQuery = new Wallet();
                pkQuery.setPublicKey(body.getPublicKey());
                pkQuery.setEquipmentNo(body.getEquipmentNo());
                pkQuery.setOwner(user.getId());
                Wallet pkResult = commonDao.queryFirstBy(pkQuery);

                if (Objects.nonNull(pkResult)) { //如果主公钥地址存在
                    if (pkResult.getKeyStatus() == 0) {
                        pkResult.setKeyStatus(1);
                        pkResult.setUpdateTime(new Date());
                        commonDao.update(pkResult);
                    }
                    hdWalletPathList.forEach(hdWalletPath -> {
                        HdWalletPath queryPath = new HdWalletPath();
                        queryPath.setWalletId(pkResult.getId());
                        queryPath.setPath(hdWalletPath.getPath());
                        HdWalletPath walletPathDepth = commonDao.queryFirstBy(queryPath);
                        //判断钱包路径和深度信息是否存在并处理
                        if (Objects.isNull(walletPathDepth)) {
                            hdWalletPath.setWalletId(pkResult.getId());
                            commonDao.insert(hdWalletPath);
                        } else {
                            //当钱包路径和深度不为空时，
                            // 如果前端提供深度大于当前深度，则进行更新
                            if (walletPathDepth.getDepth().compareTo(hdWalletPath.getDepth()) < 0) {
                                walletPathDepth.setDepth(hdWalletPath.getDepth());
                                commonDao.update(walletPathDepth);
                            }
                        }
                    });
                    walletAddressList.forEach(walletAddress -> {
                        //判断钱包地址是否存在并处理
                        WalletAddress queryWalletAddress = new WalletAddress();
                        queryWalletAddress.setWalletId(pkResult.getId());
                        queryWalletAddress.setWalletAddress(walletAddress.getWalletAddress());
                        WalletAddress walletAddressRst = commonDao.queryFirstBy(queryWalletAddress);
                        if (Objects.isNull(walletAddressRst)) {
                            walletAddress.setWalletId(pkResult.getId());
                            commonDao.insert(walletAddress);
                        }
                    });
                    //查询EOS账户是否在数据库存在并进行处理
                   /* if(eosAccountList != null && eosAccountList.size() != 0) {
                        eosAccountList.forEach(eosAccount -> {
                            EosAccount account = walletDao.queryEosAccount(eosAccount.getWalletAddress(), eosAccount.getEosAccount());
                            if(Objects.isNull(account)){
                                commonDao.insert(eosAccount);
                            }
                        });
                    }*/
                } else {           //如果主公钥地址不存在
                    //保存主公钥信息
                    commonDao.insert(hpkSave);
                    //保存钱包路径和深度
                    hdWalletPathList.forEach(hdWalletPath ->
                            hdWalletPath.setWalletId(hpkSave.getId()));
                    commonDao.batchInsert(hdWalletPathList, HdWalletPath.class);
                    //保存钱包地址
                    walletAddressList.forEach(walletAddress -> {
                        walletAddress.setWalletId(hpkSave.getId());
                    });
                    commonDao.batchInsert(walletAddressList, WalletAddress.class);
                    //新增EOS账户
                    /*if(eosAccountList != null && eosAccountList.size() != 0){
                        commonDao.batchInsert(eosAccountList,EosAccount.class);
                    }*/
                    if (needActivaty) {
                        userService.activationAccount(user.getId());//激活账号
                    }
                }
            }

        } else if (body.getType() == 0){ //删除钱包
            //主公钥信息都存在，而且主公钥状态是1（有效）
            Wallet pkQuery = new Wallet();
            pkQuery.setPublicKey(body.getPublicKey());
            pkQuery.setEquipmentNo(body.getEquipmentNo());
            Wallet pkResult = commonDao.queryFirstBy(pkQuery);

            if (Objects.nonNull(pkResult) && pkResult.getKeyStatus() == 1) {
                pkResult.setKeyStatus(0);
                pkResult.setUpdateTime(new Date());
                commonDao.update(pkResult);
            }
        }
    }

    private void manageSimpleWallet(NumbersManageWalletReqDTO body, HttpServletRequest request){
        boolean needActivaty = false;
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        if (user.getStatus() ==2) {
            needActivaty = true;
        }

        Equipment query = new Equipment();
        query.setEquipmentNo(body.getEquipmentNo());
        Equipment equipment = commonDao.queryFirstBy(query);

        //主公钥保存类
        Wallet walletSave = new Wallet();
        walletSave.setOwner(user.getId());
        walletSave.setCreateTime(new Date());
        walletSave.setEquipmentNo(body.getEquipmentNo());
        walletSave.setKeyStatus(1);
        walletSave.setPublicKey(body.getPublicKey());
        walletSave.setType(body.getIsHd() == 1 ? 0 : 1);//(需要临时性，需要改进)
        walletSave.setCreateIp(IpUtil.clientIpFrom(request));


        List<WalletAddress> walletAddressList = new ArrayList<>();
        body.getWalletInfoList().forEach(manageWalletInfo -> {
            WalletAddress walletAddressSave = new WalletAddress();
            walletAddressSave.setWalletAddress(manageWalletInfo.getWalletAddress());
            walletAddressSave.setCreatTime(new Date());
            walletAddressSave.setStatusCode(1);
            walletAddressSave.setType(manageWalletInfo.getType());
            walletAddressList.add(walletAddressSave);
        });

        if (body.getType() == 1) {
            if (Objects.isNull(equipment)) {//如果设备不存在就保存
                Equipment equipmentSave = new Equipment();
                equipmentSave.setCreatTime(new Date());
                equipmentSave.setEquipmentNo(body.getEquipmentNo());
                commonDao.insert(equipmentSave);

                commonDao.insert(walletSave);

                walletAddressList.forEach(walletAddress -> {
                    walletAddress.setEquipmentNo(equipmentSave.getEquipmentNo());
                    walletAddress.setWalletId(walletSave.getId());
                });
                commonDao.batchInsert(walletAddressList, WalletAddress.class);

                if (needActivaty) {
                    userService.activationAccount(user.getId());//激活账号
                }
            } else {
                final boolean finalNeedActivaty = needActivaty;
                walletAddressList.forEach(walletAddress -> {
                    //如果设备已经存在，先判断该钱包是否存在：如果不存在直接保存；如果存在
                    //而且状态是0，只需要把状态改成1。
                    WalletAddress wtQuery = new WalletAddress();
                    wtQuery.setWalletAddress(walletAddress.getWalletAddress());
                    wtQuery.setEquipmentNo(equipment.getEquipmentNo());
                    WalletAddress walletAddressInDB = commonDao.queryFirstBy(wtQuery);

                    if (Objects.isNull(walletAddressInDB)) {
                        commonDao.insert(walletSave);//单地址钱包保存（主公钥保存，测试主公钥为null-临时，便于钱包的创建ip保存）

                        walletAddress.setEquipmentNo(equipment.getEquipmentNo());
                        walletAddress.setWalletId(walletSave.getId());
                        commonDao.insert(walletAddress);
                        if (finalNeedActivaty) {
                            userService.activationAccount(user.getId());//激活账号
                        }
                    } else if (walletAddressInDB.getStatusCode() == 0){
                        walletAddressInDB.setStatusCode(1);
                        walletAddressInDB.setUpdateTime(new Date());
                        commonDao.update(walletAddressInDB);
                    }
                });
            }

        } else {
            //如果设备和钱包都存在，而且钱包状态是1时
            if (Objects.nonNull(equipment) ) {
                walletAddressList.forEach(walletAddress -> {
                    WalletAddress wtQuery = new WalletAddress();
                    wtQuery.setWalletAddress(walletAddress.getWalletAddress());
                    wtQuery.setEquipmentNo(equipment.getEquipmentNo());
                    WalletAddress walletAddressInDB = commonDao.queryFirstBy(wtQuery);

                    if (Objects.nonNull(walletAddressInDB) && walletAddressInDB.getStatusCode() == 1) {
                        walletAddressInDB.setStatusCode(0);
                        walletAddressInDB.setUpdateTime(new Date());
                        commonDao.update(walletAddressInDB);
                    }
                });
            }
        }
    }

    @Override
    public List<TokensRepDTO> getTokens() {
        Integer clientVersion = CurrentThreadData.clientVersion();
        List<Token> tokens = new ArrayList<>();
        if (clientVersion == null) {
            tokens = tokenRepository.findByVersionLessThanEqual(0);
        } else {
            tokens = tokenRepository.findByVersionLessThanEqual(clientVersion);
        }


        //对token进行序列排序
        tokens.sort(TokensRepDTO.order);

        List<TokensRepDTO> repDTOS = new ArrayList<>();
        tokens.forEach(token -> {
            TokensRepDTO repDTO = new TokensRepDTO();
            try {
                BeanUtils.copyProperties(repDTO, token);
                repDTOS.add(repDTO);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        return repDTOS;
    }

    @Override
    public GetTokensNewRepDTO getTokensNew(GetTokensNewReqDTO body) {
        GetTokensNewRepDTO res = new GetTokensNewRepDTO();

        Integer clientVersion = CurrentThreadData.clientVersion();
        PageRequest pageRequest = new PageRequest(body.getPageNo() - 1,
                body.getPageSize(), new Sort(Sort.Direction.ASC,"order"));

        Page<Token> tokenPage = new PageImpl<>(new ArrayList<>());
        String searcheWord = body.getSearchWord();

        String regex = "^[\\sA-Za-z0-9\u4e00-\u9fa5_\\-.]+$";
        if (StringUtils.isNotBlank(searcheWord)) {
            searcheWord = searcheWord.trim();
            if (!searcheWord.matches(regex)) {
                return res;
            }
            tokenPage = tokenRepository.
                    findByVersionLessThanEqualAndNameLikeOrFullNameLikeOrAddressOrChecksumAddressAllIgnoreCase(clientVersion,
                            searcheWord, searcheWord, searcheWord, searcheWord, pageRequest);
        } else {
            tokenPage = tokenRepository.findByVersionLessThanEqual(clientVersion, pageRequest);
        }

        List<TokensRepDTO> tokensRepDTOS = new ArrayList<>();
        List<Token> tokens = tokenPage.getContent();
        for (Token token : tokens) {
            TokensRepDTO repDTO = new TokensRepDTO();
            if (!token.getName().matches(regex)) {
                continue;
            }
            BeanCopier.getInstance().copyBean(token, repDTO);
            tokensRepDTOS.add(repDTO);
        }

        res.setContent(tokensRepDTOS);
        res.setPageTotal(tokenPage.getTotalPages());
        res.setPageNo(body.getPageNo());

        return res;
    }

    @Override
    public List<AssetsRepDTO> getAssets(WalletReqDTO body) {
        List<AssetsRepDTO> assetsRepDTOS = new ArrayList<>();
        if (body.getWalletAddress() == null || body.getWalletAddress().isEmpty()) {
           return assetsRepDTOS;
        }

        //将eth公钥hash转换成地址
        String addressHash = body.getWalletAddress().get(0);
        String ethAddress = AddressClient.toEtherAddressByHash(addressHash);

        JSONObject param = new JSONObject();
        param.put("pub_addr", ethAddress);
        if (Objects.nonNull(body.getTokens())) {
            param.put("tokens", body.getTokens());
        }
        JSONObject result = httpService.httpPostWithJson(Urls.BSM_ASSETS_URL, param);
        if (Objects.isNull(result) || result.isEmpty()) {
            logger.error("GET ASSETS ERROR");
            CustomException.response(Error.ERR_MSG_SERVICE_ERROR);
        } else if (result.getInt("code") != 0) {
            if (result.containsKey("msg") && result.getString("msg").contains("请求提交失败")) {
                CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
            } else {
                CustomException.response(Error.ERR_MSG_GET_ASSETS_FAIL);
            }
        }


        JSONArray assets = result.getJSONArray("data");
        for (int i = 0; i<assets.size(); i++){
            AssetsRepDTO assetsDTO = new AssetsRepDTO();
            JSONObject asset = assets.getJSONObject(i);

            //如果返回币种不支持，则不返回改币种的资产信息
            String tokenName = asset.getString("name");
            String value = asset.getString("asset");
            if (!SpecialTimedTask.TOKENS_NAME_KEY_MAP.containsKey(tokenName)) {
                logger.info("查询资产" + tokenName + "暂不支持");
                continue;
            }

            Token token = SpecialTimedTask.TOKENS_NAME_KEY_MAP.get(tokenName);
            try {
                if (Objects.nonNull(token)) {
                    BeanUtils.copyProperties(assetsDTO, token);
                } else {
                    assetsDTO.setName(tokenName);
                }

                assetsDTO.setValue(value);
                assetsDTO.setPrice(flatMoneyService.tokenPriceAdaptation(asset.getString("name")));
                assetsRepDTOS.add(assetsDTO);


                walletAddressDao.updateAddressBalance(addressHash, value, tokenName);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        //进行代币的权重排序
        Comparator<AssetsRepDTO> sortBean = (o1, o2) -> {
            if (o1.getOrder() == null || o2.getOrder() == null) {
                if (o1.getOrder() == null && o2.getOrder() !=null) {
                    return 1;
                }else if (o1.getOrder() != null && o2.getOrder() ==null) {
                    return -1;
                }else {
                    return 0;
                }
            } else {
                if (o1.getOrder() > o2.getOrder()) {
                    return 1;
                }else if (o1.getOrder() < o2.getOrder()) {
                    return  -1;
                }
                return 0;
            }

        };
        assetsRepDTOS.sort(sortBean);

        return assetsRepDTOS;
    }

    @Value("${net.work.type:test_reg}")
    private String networkType;
    @Override
    public List<AssetsRepDTO> getHdAssets(HdWalletReqDTO body) {

        List<AssetsRepDTO> assetsRepDTOS = Collections.synchronizedList(new ArrayList<>());

        if (body.getWalleList().isEmpty()) {
            return assetsRepDTOS;
        }

        List<HdWalletReqDTO.HdWalletReq> hdWallets = body.getWalleList();
        List<String> addressListE = new ArrayList<>();
        List<UTXOReqDTO.UTXOAddress> PubkHashListBCH = new ArrayList<>();
        List<UTXOReqDTO.UTXOAddress> PubkHashListBTC = new ArrayList<>();
        List<UTXOReqDTO.UTXOAddress> PubkHashListZCASH = Lists.newArrayList();
        List<String> addressEOSList = Lists.newArrayList();
        hdWallets.forEach(hdWallet -> {
            if (hdWallet.getCoinType() == 0 ||
                    hdWallet.getCoinType() == 145) { //如果是BTC和BCH

                if (hdWallet.getCoinType() == 145) {//BCH钱包需要转换成新钱包地址
                    UTXOReqDTO.UTXOAddress utxoAddress = new UTXOReqDTO.UTXOAddress();
                    utxoAddress.setType(hdWallet.getType());
                    utxoAddress.setPubkHash(hdWallet.getWalletAddress());
                    utxoAddress.setCoinType(CoinType.BCH.getCode());
                    PubkHashListBCH.add(utxoAddress);
                }else {
                    UTXOReqDTO.UTXOAddress utxoAddress = new UTXOReqDTO.UTXOAddress();
                    utxoAddress.setType(hdWallet.getType());
                    utxoAddress.setPubkHash(hdWallet.getWalletAddress());
                    utxoAddress.setCoinType(CoinType.BTC.getCode());
                    PubkHashListBTC.add(utxoAddress);
                }

            } else if (hdWallet.getCoinType() == 60) { //如果是ETH(暂时不转换钱包，再被调用的方法中会被转换)
                addressListE.add(hdWallet.getWalletAddress());
            } else if (hdWallet.getCoinType() == 194){
                addressEOSList.add(hdWallet.getWalletAddress());
            } else if (hdWallet.getCoinType() == 133) {
                UTXOReqDTO.UTXOAddress utxoAddress = new UTXOReqDTO.UTXOAddress();
                utxoAddress.setType(hdWallet.getType());
                utxoAddress.setPubkHash(hdWallet.getWalletAddress());
                utxoAddress.setCoinType(CoinType.ZEC.getCode());
                PubkHashListZCASH.add(utxoAddress);
            }
        });


        //获取钱包所有以太坊中的资产
        Future ethFuture = ThreadUtil.runOnCurrentOrOtherThread(() -> {
            if (!addressListE.isEmpty()) {
                List<AssetsRepDTO> ethAssetsRepDTO = this.getAssets(new WalletReqDTO(addressListE, body.getTokens()));
                assetsRepDTOS.addAll(ethAssetsRepDTO);
            }
        });


       // 获取钱包所有EOS资产
        Future eosFuture = ThreadUtil.runOnCurrentOrOtherThread(() -> {
            if(!addressEOSList.isEmpty()){
                AssetsRepDTO assetsRepDTO = eosAsssets(addressEOSList);
                assetsRepDTOS.add(assetsRepDTO);
            }
        });
        List<String> queryTokens = body.getTokens();
        Future btcFuture = ThreadUtil.runOnCurrentOrOtherThread(() -> {
            if (queryTokens == null || queryTokens.contains(CoinType.BTC.getName())) {
                AssetsRepDTO assetsBTC = arrangedBitAssets(PubkHashListBTC, CoinType.BTC);
                if (assetsBTC != null) {
                    assetsRepDTOS.add(assetsBTC);
                }
            }
        });

        Future bchFuture = ThreadUtil.runOnCurrentOrOtherThread(() -> {
            if (queryTokens == null || queryTokens.contains(CoinType.BCH.getName())) {
                AssetsRepDTO assetsBCH = arrangedBitAssets(PubkHashListBCH, CoinType.BCH);
                if (assetsBCH != null) {
                    assetsRepDTOS.add(assetsBCH);
                }
            }
        });

        Future whcFuture = ThreadUtil.runOnCurrentOrOtherThread(() -> {
            if (queryTokens == null || queryTokens.contains(CoinType.WHC.getName())) {
                AssetsRepDTO assetsWHC = arrangedBitAssets(PubkHashListBCH, CoinType.WHC);
                if (assetsWHC != null) {
                    assetsRepDTOS.add(assetsWHC);
                }
            }
        });

        Future usdtFuture = ThreadUtil.runOnCurrentOrOtherThread(() -> {
            if (queryTokens == null || queryTokens.contains(CoinType.usdt.getName())) {
                AssetsRepDTO assetsusdt = arrangedBitAssets(PubkHashListBTC, CoinType.usdt);
                if (assetsusdt != null) {
                    assetsRepDTOS.add(assetsusdt);
                }
            }
        });
        Future zcashFuture = ThreadUtil.runOnCurrentOrOtherThread(() -> {
            if (queryTokens == null || queryTokens.contains(CoinType.ZEC.getName())) {
                AssetsRepDTO assetszcash = arrangedBitAssets(PubkHashListZCASH, CoinType.ZEC);
                if (assetszcash != null) {
                    assetsRepDTOS.add(assetszcash);
                }
            }
        });

        //等待所有任务完成
        ThreadUtil.waitFutures(10000, true, btcFuture, bchFuture, whcFuture, ethFuture, eosFuture,
                usdtFuture,zcashFuture);

        logger.info("btcFuture:{}, bchFuture:{}, whcFuture:{}, ethFuture:{}, eosFuture:{},zcashFuture:{}",
                !btcFuture.isCancelled(), !bchFuture.isCancelled(), !whcFuture.isCancelled(),
                !ethFuture.isCancelled(), !eosFuture.isCancelled(), !usdtFuture.isCancelled(),!zcashFuture.isCancelled());

        //进行代币的权重排序
        Comparator<AssetsRepDTO> sortBean = (o1, o2) -> {
            if (o1.getOrder() == null || o2.getOrder() == null) {
                if (o1.getOrder() == null && o2.getOrder() !=null) {
                    return 1;
                }else if (o1.getOrder() != null && o2.getOrder() ==null) {
                    return -1;
                }else {
                    return 0;
                }
            } else {
                if (o1.getOrder() > o2.getOrder()) {
                    return 1;
                }else if (o1.getOrder() < o2.getOrder()) {
                    return  -1;
                }
                return 0;
            }

        };
        assetsRepDTOS.sort(sortBean);

        return assetsRepDTOS;
    }

    /**
     * 根据地址列表查询 eos资产
     */

    private AssetsRepDTO eosAsssets(List<String> eosAddressList){
        List<String> list=Lists.newArrayList();
        eosAddressList.forEach( public_Key ->{
            JSONObject object = new JSONObject();
            object.put("pub_key",public_Key);
            logger.info("查询eos账户:{}",object);
            JSONObject accounts = httpService.httpPostWithJson(Urls.EOS_CHAIN_ACCOUNT, object);
            logger.info("eos账户:{}",accounts);
            JSONArray data = accounts.getJSONArray("data");
            for(int i=0;i<data.size();i++){
                JSONObject o = data.getJSONObject(i);
                list.add(o.getString("account"));
            }
        });
        AssetsRepDTO dto = new AssetsRepDTO();
        Long eosAsset = 0L;
//        list.stream().forEach( account -> {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("code","eosio.token");
//            jsonObject.put("account",account);
//            jsonObject.put("symbol","EOS");
//            logger.info("eos资产查询请求开始:{}",jsonObject);
//            JSONObject asset = httpService.httpPostWithJson(Urls.EOS_ACCOUNT_ASSET, jsonObject);
//            if(asset == null || asset.getInt("code")!=0){
//                CustomException.response(Error.SERVER_EXCEPTION);
//            }
//            logger.info("eos资产请求数据结果:{}",asset);
//            JSONArray array = asset.getJSONArray("data");
//            eosAsset+=array.getJSONObject(0).getLong("amount")
//            if(array.size()!=0){
//                assetInside.setAsset(array.getJSONObject(0).getLong("amount")+"");
//            }else{
//                assetInside.setAsset("");
//            }
//
//        });
        for(String account : list){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code","eosio.token");
            jsonObject.put("account",account);
            jsonObject.put("symbol","EOS");
            logger.info("eos资产查询请求开始:{}",jsonObject);
            JSONObject asset = httpService.httpPostWithJson(Urls.EOS_ACCOUNT_ASSET, jsonObject);
            if(asset == null || asset.getInt("code")!=0){
                CustomException.response(Error.SERVER_EXCEPTION);
            }
            logger.info("eos资产请求数据结果:{}",asset);
            JSONArray array = asset.getJSONArray("data");
            if(array.size()!=0){
                eosAsset+=array.getJSONObject(0).getLong("amount");
            }
        }
        Token token = SpecialTimedTask.TOKENS_NAME_KEY_MAP.get("EOS");
        try {
            BeanUtils.copyProperties(dto,token);
            dto.setValue(eosAsset.toString());
            dto.setPrice(flatMoneyService.tokenPriceAdaptation("EOS"));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return dto;


    }
    /**
     * 根据utxo的列表与对应的状态整理出资产信息
     * (针对一种代币)
     * @return
     */
    private AssetsRepDTO  arrangedBitAssets(List<UTXOReqDTO.UTXOAddress> addressList, CoinType coinType) {
        Token token = SpecialTimedTask.TOKENS_NAME_KEY_MAP.get(coinType.getName());
        if (addressList == null || addressList.isEmpty() || token == null ) {
            return null;
        }

        AssetsRepDTO assetsDTO = new AssetsRepDTO();

        try {
            if (coinType.getName().equalsIgnoreCase(CoinType.WHC.getName()) ||
                    coinType.getName().equals(CoinType.usdt.getName())) {
                List<AddressReqDTO.Address> addresseList = new ArrayList<>();
                addressList.forEach(utxoAddress -> {
                    AddressReqDTO.Address address = new AddressReqDTO.Address();
                    address.setAddressHash(utxoAddress.getPubkHash());
                    address.setType(utxoAddress.getType());
                    addresseList.add(address);
                });

                assetsDTO = this.getContractTokenAssetsB(addresseList, coinType.getName());
            } else if (coinType.getName().equalsIgnoreCase(CoinType.ZEC.getName())){
                UTXOReqDTO param = new UTXOReqDTO();
                param.setAddressList(addressList);
                param.setTokenType(Arrays.asList(coinType.getName()));
                List<UTXORepDTO> utxoRepDTOS = getZcashUtxo(param);

                List<UTXORepDTO.UTXORep> utxoReps = utxoRepDTOS.get(0).getUTXOS();
                BigInteger asset = new BigInteger("0");
                for (int i = 0; i < utxoReps.size(); i++) {
                    asset = asset.add(BigInteger.valueOf(utxoReps.get(i).getUtxo()));
                }
                BeanUtils.copyProperties(assetsDTO, token);
                assetsDTO.setValue(asset.toString());
                assetsDTO.setPrice(flatMoneyService.tokenPriceAdaptation(coinType.getName()));
            } else {
                UTXOReqDTO param = new UTXOReqDTO();
                param.setAddressList(addressList);
                param.setTokenType(Arrays.asList(coinType.getName()));
                List<UTXORepDTO> utxoRepDTOS = getUTXO(param);

                List<UTXORepDTO.UTXORep> utxoReps = utxoRepDTOS.get(0).getUTXOS();
                BigInteger asset = new BigInteger("0");
                for (int i = 0; i < utxoReps.size(); i++) {
                    asset = asset.add(BigInteger.valueOf(utxoReps.get(i).getUtxo()));
                }
                BeanUtils.copyProperties(assetsDTO, token);
                assetsDTO.setValue(asset.toString());
                assetsDTO.setPrice(flatMoneyService.tokenPriceAdaptation(coinType.getName()));
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return assetsDTO;
    }

    /**
     * 获取B链合约代币地址的总资产
     * @param addressList
     * @return
     */
    AssetsRepDTO getContractTokenAssetsB(List<AddressReqDTO.Address> addressList,
                                         String tokenName){
        AddressReqDTO assetsReqDTO = new AddressReqDTO();
        assetsReqDTO.setAddressList(addressList);
        assetsReqDTO.setTokenName(tokenName);
        GetAddressAssetsRepDTO addressAssetsRepDTO = this.getAddressContractTokenAssetsB(assetsReqDTO);
        List<GetAddressAssetsRepDTO.GetAddressAssetInside> addressAssetInsides = addressAssetsRepDTO.getContent();

        if (addressAssetInsides == null || addressAssetInsides.isEmpty()) {
            return null;
        }

        BigInteger totalAssets = BigInteger.ZERO;
        for (int i = 0; i < addressAssetInsides.size(); i++) {
            totalAssets = totalAssets.add(new BigInteger(addressAssetInsides.get(i).getAsset())) ;
        }


        AssetsRepDTO result = new AssetsRepDTO();

        Token token = tokenRepository.findByName(tokenName);
        BeanCopier.getInstance().copyBean(token, result);

        result.setValue(totalAssets.toString());
        if (CoinType.usdt.getName().equals(tokenName)) {
            result.setPrice(1d);
        } else {
            result.setPrice(flatMoneyService.tokenPriceAdaptation(tokenName));
        }

        return result;
    }


    @Transactional
    @Override
    public void stopEquipmentWallet(StopWalletDTO body) {
        //普通钱包停用
        walletAddressDao.updateAddressStatus(body.getEquipmentNo(),0);
        //HD钱包停用
        walletDao.updatePKStatus(body.getEquipmentNo(), 0);
    }

//    @Transactional
//    @Override
//    public void resetEquipmentWallet(ResetWalletDTO body) {
//        ManageWalletReqDTO walletReqDTO = new ManageWalletReqDTO();
//        walletReqDTO.setEquipmentNo(body.getEquipmentNo());
//        walletReqDTO.setType(1);
//
//        if (Objects.nonNull(body.getWalletAddressList())) {
//            //普通钱包重置
//            body.getWalletAddressList().forEach(walletAddress -> {
//                walletReqDTO.setIsHd(0);
//                walletReqDTO.setWalletAddress(walletAddress);
////                this.manageWallet(walletReqDTO);
//            });
//        }
//
//        //HD钱包的主公钥进行激活
//        if (!StringUtils.isBlank(body.getPublicKey())) {
//            walletDao.updatePKStatus(body.getEquipmentNo(),1);
//        }
//
//
//        if (Objects.nonNull(body.getHdWalletList())) {
//            body.getHdWalletList().forEach(hdWallet -> {
//                walletReqDTO.setIsHd(1);
//                walletReqDTO.setWalletAddress(hdWallet.getHdWalletAddress());
//                walletReqDTO.setPublicKey(body.getPublicKey());
//                walletReqDTO.setWalletPathDepth(hdWallet.getWalletPathDepth());
////                this.manageHDWallet(walletReqDTO);
//            });
//        }
//
//    }

    @Value("${whc.confirm.num:1}")
    private Long WHC_CONFIRM_NUM;
    @Value("${confirm.num:6}")
    private Long CONFIRM_NUM;
    @Override
    public List<UTXORepDTO> getUTXO(UTXOReqDTO body) {
        List<String> addrList = new ArrayList<>();
        body.getAddressList().forEach(address -> {
            String scriptType = address.getType() == 0 ?
                    ScriptType.P2PKH.getScriptType() : ScriptType.P2SH.getScriptType();
            //先对钱包地址进行分类别转换
            if (address.getCoinType() == 145) { //如果是bch
                addrList.add(AddressClient.toCashAddressByPubkHash(
                        address.getPubkHash(),
                        scriptType,
                        networkType));
            } else if (address.getCoinType() == 0) { //如果是btc
                addrList.add(AddressClient.toLegacyAddressByPubkHash(
                     address.getPubkHash(),
                     scriptType,
                     networkType));
            }
        });

        JSONObject param = new JSONObject();
        param.put("addr_list", addrList);
        param.put("token_type",body.getTokenType());
        JSONObject result = httpService.httpPostWithJson(Urls.GET_UTXO_URL, param);
        checkResult(result);
        List<UTXORepDTO> resp = new ArrayList<>();
        JSONArray data = result.getJSONArray("data");
        for (int i = 0 ; i < data.size(); i++) {
            UTXORepDTO utxoRep = new UTXORepDTO();
            JSONObject utxoObj = data.getJSONObject(i);
            List<UTXORepDTO.UTXORep> utxoList = new ArrayList<>();
            JSONArray utxoAry = utxoObj.getJSONArray("data");
            String tokenName = utxoObj.getString("name");
            for (int j = 0; j < utxoAry.size(); j++) {
                JSONObject utxoinfo = utxoAry.getJSONObject(j);
               String blockHeightKey;
               Long needConfirm = 0l;
               if (CoinType.BCH.getName().equals(tokenName)) {
                  blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + CoinType.BCH.getCode();
                  needConfirm = WHC_CONFIRM_NUM;
               }  else {
                    blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + CoinType.valueOf(tokenName).getCode();
                    needConfirm = CONFIRM_NUM;
               }
                Long height = utxoinfo.getLong("block_height");
                String BCHStrHeight = stringRedisTemplate.opsForValue().get(blockHeightKey);
                Long bchHeight = Long.parseLong(BCHStrHeight);

                Long confirms = bchHeight - height + 1;
                if (confirms < 1) {
                    confirms = 1l;
                }
                if (height == null || height == 0 || (confirms > 0 && confirms < needConfirm)) {
                    boolean belongSelf = checkUtxoCreatBySelf(utxoinfo.getString("vout_txid"),
                            tokenName, addrList);
                    if (!belongSelf) {
                        continue;
                    }
                }

                UTXORepDTO.UTXORep utxoEty =  new UTXORepDTO.UTXORep();
                utxoEty.setUtxo(utxoinfo.getLong("value"));
                utxoEty.setWalletAddress(utxoinfo.getString("address"));
                utxoEty.setVoutScript(utxoinfo.getString("vout_pkscript"));
                utxoEty.setTanxIndex(utxoinfo.getInt("vout_index"));
                utxoEty.setTanxHash(utxoinfo.getString("vout_txid"));
                utxoEty.setStatus(utxoinfo.getInt("spend_type"));
                utxoList.add(utxoEty);
            }
            utxoRep.setUTXOS(utxoList);
            utxoRep.setTokenType(tokenName);

            resp.add(utxoRep);

            updateAddressBalance(utxoRep);
        }


        return resp;
    }

    public List<UTXORepDTO> getZcashUtxo(UTXOReqDTO body) {
        List<String> addrList = new ArrayList<>();
        body.getAddressList().forEach(address -> {
            String scriptType = address.getType() == 0 ?
                    ScriptType.P2PKH.getScriptType() : ScriptType.P2SH.getScriptType();
            addrList.add(AddressClient.toZcashAddressByPubkHash(address.getPubkHash(), scriptType, networkType));
        });
        JSONObject var1 = new JSONObject();
        var1.put("addr_list", addrList);
        logger.info("获取zcash UTXO请求参数:{}", var1);
        JSONObject var2 = httpService.httpPostWithJson(Urls.GET_ZCASH_UTXO_URL, var1);
        logger.info("获取zcash UTXO请求结果:{}", var2);
        checkResult(var2);
        List<UTXORepDTO> var3 = Lists.newArrayList();
        UTXORepDTO var6 = new UTXORepDTO();
        List<UTXORepDTO.UTXORep> var7 = new ArrayList<>();
        JSONArray var4 = var2.getJSONArray("data");
        for(int i = 0; i < var4.size(); i++){
            JSONObject var5 = var4.getJSONObject(i);
            String blockHeightKey;
            Long needConfirm = 0l;
            blockHeightKey = Constant.BLOCK_CURRENT_HEIGHT + CoinType.ZEC.getCode();
            needConfirm = CONFIRM_NUM;
            Long height = var5.getLong("block_height");
            String ZCASHStrHeight = stringRedisTemplate.opsForValue().get(blockHeightKey);
            Long bchHeight = Long.parseLong(ZCASHStrHeight);
            Long confirms = bchHeight - height + 1;
            if (confirms < 1) {
                confirms = 1l;
            }
            if (height == null || height == 0 || (confirms > 0 && confirms < needConfirm)) {
                boolean belongSelf = checkUtxoCreatBySelf(var5.getString("vout_txid"),
                        CoinType.ZEC.getName(), addrList);
                if (!belongSelf) {
                    continue;
                }
            }
            UTXORepDTO.UTXORep utxoEty =  new UTXORepDTO.UTXORep();
            utxoEty.setUtxo(var5.getLong("value"));
            utxoEty.setWalletAddress(var5.getString("address"));
            utxoEty.setVoutScript(var5.getString("vout_pkscript"));
            utxoEty.setTanxIndex(var5.getInt("vout_index"));
            utxoEty.setTanxHash(var5.getString("vout_txid"));
            utxoEty.setStatus(var5.getInt("spend_type"));
            var7.add(utxoEty);
        }
        var6.setTokenType(CoinType.ZEC.getName());
        var6.setUTXOS(var7);
        var3.add(var6);
        updateAddressBalance(var6);
        return var3;
    }

    private void checkResult(JSONObject result){
        if (Objects.isNull(result) || result.isEmpty()) {
            logger.error("GET UTXO ERROR");
            CustomException.response(Error.ERR_MSG_SERVICE_ERROR);
        } else if (result.getInt("code") != 0) {
            if (result.containsKey("msg") && result.getString("msg").contains("请求提交失败")) {
                CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
            } else {
                CustomException.response(result.getString("msg"));
            }
        }

    }
    /**
     * 更新地址余额
     */
    public void updateAddressBalance (UTXORepDTO request) {
        String tokenName = request.getTokenType();
        Map<String,List<UTXORepDTO.UTXORep>> addressUTXO = request.getUTXOS().stream().
                collect(Collectors.groupingBy(UTXORepDTO.UTXORep :: getWalletAddress));
        for (Map.Entry<String, List<UTXORepDTO.UTXORep>> entry : addressUTXO.entrySet()) {
            String address = entry.getKey();
            String addressHash = null;
            if (CoinType.valueOf(tokenName).getCode() != 133){
                addressHash = AddressClient.addressToHash(CoinType.valueOf(tokenName).getCode(),
                        address, networkType);
            } else {
                try {
                    addressHash = AddressClient.addressToZcashHash(CoinType.valueOf(tokenName).getCode(),
                            address, networkType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            BigInteger asset = new BigInteger("0");
            List<UTXORepDTO.UTXORep> utxoRepList = entry.getValue();
            for (int i = 0; i < utxoRepList.size(); i++) {
                asset = asset.add(BigInteger.valueOf(utxoRepList.get(i).getUtxo()));
            }

            walletAddressDao.updateAddressBalance(addressHash, asset.toString(), tokenName);
        }
    }

    /**
     * 判断utxo是否是由当前钱包发起的交易的找零
     * @param txid
     * @param tokenName
     * @param addressList
     * @return
     */
    private boolean checkUtxoCreatBySelf(String txid, String tokenName, List<String> addressList) {
        boolean rst = false;
        JSONObject getTxParam = new JSONObject();
        JSONObject txInfo;
        if (tokenName.equalsIgnoreCase(CoinType.ZEC.getName())){
            getTxParam.put("txid", txid);
            txInfo = httpService.httpPostWithJson(Urls.GET_ZCASH_TX_INFO, getTxParam);
        } else {
            getTxParam.put("txid", txid);
            getTxParam.put("token_type", tokenName);
            txInfo = httpService.httpPostWithJson(Urls.GET_TX_INFO, getTxParam);
        }
        if (txInfo.containsKey("code") && txInfo.getInt("code") != 0) {
            CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
        }
        List<String> vinAddressList = new ArrayList<>();
        JSONArray vin = txInfo.getJSONObject("data").getJSONArray("vin");
        for (int k = 0; k < vin.size(); k++) {
            vinAddressList.add(vin.getJSONObject(k).getString("input_address"));
        }
        if (addressList.containsAll(vinAddressList)) {
            rst = true;
        }
        return rst;
    }


    @Override
    public CheckTranxRepDTO checkTranx(CheckTranxReqDTO body) {
        List<String> addrList = new ArrayList<>();
        body.getAddressList().forEach(addr -> {
            String scriptType = addr.getType() == 0 ?
                    ScriptType.P2PKH.getScriptType() : ScriptType.P2SH.getScriptType();
            //对公钥hash进行分类转换
            switch (addr.getCoinType()){
                case 0:
                    addrList.add(AddressClient.toLegacyAddressByPubkHash(addr.getPubkHash(),
                            scriptType,
                            networkType));
                    break;
                case 145:
                    addrList.add(AddressClient.toCashAddressByPubkHash(addr.getPubkHash(),
                            scriptType,
                            networkType));
                    break;
                case 60:
                    addrList.add(AddressClient.toEtherAddressByHash(addr.getPubkHash()));
                    break;
                default:
                        break;
            }
        });
        JSONObject param = new JSONObject();
        param.put("addr_list", addrList);
        param.put("token_type", body.getTokenType());
        JSONObject result = httpService.httpPostWithJson(Urls.CHECK_TRANX_URL, param);

        if (Objects.isNull(result) || result.isEmpty()) {
            logger.error("CHECK TRANX ERROR");
            CustomException.response(Error.ERR_MSG_SERVICE_ERROR);
        } else if (result.getInt("code") != 0) {
            if (result.containsKey("msg") && result.getString("msg").contains("请求提交失败")) {
                CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
            } else {
                CustomException.response((result.getString("msg")));
            }
        }

        CheckTranxRepDTO resp = new CheckTranxRepDTO();
        JSONObject data = result.getJSONObject("data");
        resp.setAddressList(data.getJSONArray("addr_list"));
        resp.setResult(data.getJSONArray("result"));
        resp.setTokenType(data.getString("token_type"));

        return resp;
    }



    @Override
    public GetAddressAssetsRepDTO getAddressAssets(GetAddressAssetsReqDTO body) {
        GetAddressAssetsRepDTO rsp = new GetAddressAssetsRepDTO();

        if (CoinType.WHC.getName().equals(body.getTokenName()) ||
                CoinType.usdt.getName().equals(body.getTokenName())) {
            List<AddressReqDTO.Address> contractTokenAddressList = new ArrayList<>();
            body.getPubkHashList().forEach(addressHash -> {
                AddressReqDTO.Address contractTokenAddress = new AddressReqDTO.Address();
                contractTokenAddress.setType(body.getType());
                contractTokenAddress.setAddressHash(addressHash);
                contractTokenAddressList.add(contractTokenAddress);
            });
            AddressReqDTO addressReqDTO = new AddressReqDTO();
            addressReqDTO.setTokenName(body.getTokenName());
            addressReqDTO.setAddressList(contractTokenAddressList);
            return getAddressContractTokenAssetsB(addressReqDTO);

        }

        if (body.getPubkHashList() == null ||body.getPubkHashList().isEmpty())
            return rsp;

        List<String> addressList = new ArrayList<>();
        List<String> pubkListBTC = new ArrayList<>();
        List<String> pubkListBCH = new ArrayList<>();
        if (Arrays.asList(0, 145).contains(body.getCoinType())) {
            String scriptType = body.getType() == 0 ?
                    ScriptType.P2PKH.getScriptType() :
                    ScriptType.P2SH.getScriptType();
            if (body.getCoinType() == 0) {
                body.getPubkHashList().forEach(pubkHash -> {
                    addressList.add(AddressClient.toLegacyAddressByPubkHash(pubkHash,
                            scriptType, networkType));
                    pubkListBTC.add(pubkHash);
                });
            } else if (body.getCoinType() == 145) {
                body.getPubkHashList().forEach(pubkHash -> {
                    addressList.add(AddressClient.toCashAddressByPubkHash(pubkHash,
                           scriptType, networkType));
                    pubkListBCH.add(pubkHash);
                });
            }
        }
        if (CoinType.ZEC.getCode().equals(body.getCoinType())) {
            UTXOReqDTO param = new UTXOReqDTO();
            List<UTXOReqDTO.UTXOAddress> zcashList = Lists.newArrayList();
            body.getPubkHashList().forEach ( x -> {
                UTXOReqDTO.UTXOAddress utxoAddress = new UTXOReqDTO.UTXOAddress();
                utxoAddress.setType(body.getType());
                utxoAddress.setPubkHash(x);
                utxoAddress.setCoinType(body.getCoinType());
                zcashList.add(utxoAddress);
            });
            Map<String,BigInteger> assetMap = Maps.newHashMap();
            zcashList.forEach( x -> {
                String zCashAddress = AddressClient.toZcashAddressByPubkHash(x.getPubkHash(), x.getType() == 0 ? ScriptType.P2PKH.getScriptType() : ScriptType.P2SH.getScriptType(), networkType);
                assetMap.put(zCashAddress,BigInteger.valueOf(0l));
            });
            param.setAddressList(zcashList);
            param.setTokenType(Arrays.asList(body.getTokenName()));
            List<UTXORepDTO> utxoRepDTOS = getZcashUtxo(param);
            UTXORepDTO utxoRepDTO = utxoRepDTOS.get(0);
            List<GetAddressAssetsRepDTO.GetAddressAssetInside> zcashAssets = Lists.newArrayList();
            utxoRepDTO.getUTXOS().forEach ( x -> {
                BigInteger i = assetMap.get(x.getWalletAddress());
                if (i == null) {
                    assetMap.put(x.getWalletAddress(),BigInteger.valueOf(x.getUtxo()));
                } else {
                    assetMap.put(x.getWalletAddress(),i.add(BigInteger.valueOf(x.getUtxo())));
                }
            });
            for (Map.Entry<String,BigInteger> entry : assetMap.entrySet()){
                GetAddressAssetsRepDTO.GetAddressAssetInside in = new GetAddressAssetsRepDTO.GetAddressAssetInside();
                in.setAddress(entry.getKey());
                in.setAsset(entry.getValue().toString());
                zcashAssets.add(in);
            }
            rsp.setContent(zcashAssets);
            rsp.setTokenName(body.getTokenName());
            return rsp;
        }

        List<GetAddressAssetsRepDTO.GetAddressAssetInside> addressInfoList  =
                new ArrayList<>();


        if (body.getCoinType() == 60) { //如果是以太链的代币
            WalletReqDTO reqDTO = new WalletReqDTO();
            reqDTO.setTokens(Arrays.asList(body.getTokenName()));
            reqDTO.setWalletAddress(body.getPubkHashList());
            List<AssetsRepDTO> ethAssetsList = this.getAssets(reqDTO);
            ethAssetsList.forEach(assetsRepDTO -> {
                GetAddressAssetsRepDTO.GetAddressAssetInside addressInfo = new GetAddressAssetsRepDTO.GetAddressAssetInside();
                addressInfo.setAddress(AddressClient.toEtherAddressByHash(body.getPubkHashList().get(0)));
                addressInfo.setAsset(assetsRepDTO.getValue());
                addressInfoList.add(addressInfo);
            });
            rsp.setTokenName(body.getTokenName());

        } else if (Arrays.asList(0, 145).contains(body.getCoinType().intValue())) { //如果是B链上的代币
            UTXOReqDTO utxoReqDTO = new UTXOReqDTO();
            List<UTXOReqDTO.UTXOAddress> utxoAddressList = new ArrayList<>();
            if (CoinType.BTC.getCode().equals(body.getCoinType())) {
                pubkListBTC.forEach(pubk -> {
                    UTXOReqDTO.UTXOAddress utxoAddress = new UTXOReqDTO.UTXOAddress();
                    utxoAddress.setPubkHash(pubk);
                    utxoAddress.setCoinType(SpecialTimedTask.TOKENS_NAME_KEY_MAP.
                            get(body.getTokenName()).getCoinType());
                    utxoAddress.setType(body.getType());
                    utxoAddressList.add(utxoAddress);
                });
            } else {
                pubkListBCH.forEach(pubk -> {
                    UTXOReqDTO.UTXOAddress utxoAddress = new UTXOReqDTO.UTXOAddress();
                    utxoAddress.setCoinType(SpecialTimedTask.TOKENS_NAME_KEY_MAP.
                            get(body.getTokenName()).getCoinType());
                    utxoAddress.setType(body.getType());
                    utxoAddress.setPubkHash(pubk);
                    utxoAddressList.add(utxoAddress);
                });
            }
            utxoReqDTO.setAddressList(utxoAddressList);
            utxoReqDTO.setTokenType(Arrays.asList(body.getTokenName().toUpperCase()));
            List<UTXORepDTO> utxoRepDTOList = this.getUTXO(utxoReqDTO);

            //分组整理每个地址下的utxo的总和
            UTXORepDTO utxoRepDTO = utxoRepDTOList.get(0);
            Map<String, List<UTXORepDTO.UTXORep>> utxoRepMap = utxoRepDTO.getUTXOS().stream().collect(
                    Collectors.groupingBy(UTXORepDTO.UTXORep::getWalletAddress));

            addressList.forEach(address ->{
                BigInteger adrUtxo = new BigInteger("0");
                List<UTXORepDTO.UTXORep> utxoRepList = utxoRepMap.get(address);
                GetAddressAssetsRepDTO.GetAddressAssetInside addressInfo =
                        new GetAddressAssetsRepDTO.GetAddressAssetInside();
                if (utxoRepList != null) {
                    for (int i = 0; i < utxoRepList.size(); i++){
                        adrUtxo = adrUtxo.add(BigInteger.valueOf(utxoRepList.get(i).getUtxo()));
                    }
                }

                addressInfo.setAddress(address);
                addressInfo.setAsset(adrUtxo.toString());
                addressInfoList.add(addressInfo);
            });
            rsp.setTokenName(utxoRepDTO.getTokenType().toUpperCase());
        }


        rsp.setContent(addressInfoList);

        return rsp;
    }

    @Autowired
    private ThirdService thirdService;
    @Override
    public Map<String, Double> getTokenPrice(List<String> tokenList) {
        Map<String, Double> result = new HashMap<>();

        if (tokenList == null || tokenList.isEmpty()) {
//            Map<String, Token> tokenMap = SpecialTimedTask.TOKENS_NAME_KEY_MAP;
//            for (String tokenName : tokenMap.keySet()) {
//                result.put(tokenName, flatMoneyService.tokenPriceAdaptation(tokenName));
//            }
            String strSymbols = thirdService.getSupportTokenParam();
            List<String> symbols = Arrays.asList(strSymbols.split("\\|"));
            List<String> exchangeToken = new ArrayList<>();
            symbols.forEach(symbol ->{
                exchangeToken.add(symbol.split("/")[0]);
            });
            exchangeToken.forEach(tokenName -> {
                result.put(tokenName, flatMoneyService.tokenPriceAdaptation(tokenName));
            });

        } else {
            tokenList.forEach(tokenName ->{
                tokenName = tokenName.toUpperCase();
                result.put(tokenName, flatMoneyService.tokenPriceAdaptation(tokenName));
            });
        }
        return result;
    }

    @Override
    public List<EOSAccountDto> eosAcoountList(String public_Key) {
        List<EOSAccountDto> list=Lists.newArrayList();
        JSONObject object = new JSONObject();
        object.put("pub_key",public_Key);
        JSONObject accounts = httpService.httpPostWithJson(Urls.EOS_CHAIN_ACCOUNT, object);
        JSONArray data = accounts.getJSONArray("data");
        for(int i=0;i<data.size();i++){
            JSONObject o = data.getJSONObject(i);
            EOSAccountDto eosAccountDto = new EOSAccountDto();
            eosAccountDto.setAccount(o.getString("account"));
            eosAccountDto.setCreate_time(o.getString("create_time"));
            list.add(eosAccountDto);
        }
        return list;
    }

    @Override
    public CommonResult EOStran(EOStransactionReqDto dto) {
//        Tx tx = dto.getTransaction();
        JSONObject object = JSONObject.fromObject(dto);
        logger.info("开始EOS交易链上推送:{}",object);
        JSONObject jsonObject = httpService.httpPostWithJson(Urls.EOS_CHAIN_TRANSCTION, object);
        logger.info("EOS交易返回数据:{}",jsonObject);
        CommonResult result = new CommonResult();
        if(!jsonObject.containsKey("error")){
            result.setData(Maps.newHashMap().put("transaction_id",jsonObject.getString("transaction_id")));
        }else{
            JSONObject error = jsonObject.getJSONObject("error");
            result.setCode(error.getInt("code"));
            String name = error.getString("name");
            if(name.equals("account_name_exists_exception")){
                CustomException.response(Error.ERR_MSG_EOS_TRAN);
            }else{
                result.setMsg(error.getString("name"));
            }
        }
        return result;
    }

    @Override
    public EOStranactionRepDTO accountReal( EOSCreateAccountDTO dto) {
        EOSSignUtil esu = new EOSSignUtil(Urls.EOS_REAL);
        EOStranactionRepDTO repDTO = esu.getAccountReal(dto.getCreator(), dto.getNewAccount(), dto.getOwner(), dto.getActive(), dto.getBuyRam(),dto.getStakeNetQuantity(),dto.getTakeCpuQuantity(),dto.getTransfer());
        return repDTO;
    }

    @Override
    public EOStranactionRepDTO txReal(EOSTransactionDTO dto) {
        EOSSignUtil esu = new EOSSignUtil(Urls.EOS_REAL);
        EOStranactionRepDTO rep = esu.getTransReal(dto.getContractAccount(), dto.getFrom(), dto.getTo(), dto.getQuantity(), dto.getMemo());
        return rep;
    }



    @Data
    public static class ContractTokenAddressAssets {
        private Integer code;
        private String msg;
        private  List<AddressAsset> data;

        @Data
        class AddressAsset {
            private String address;
            private String balance;
        }
    }

    public GetAddressAssetsRepDTO getAddressContractTokenAssetsB(AddressReqDTO body) {
        GetAddressAssetsRepDTO result = new GetAddressAssetsRepDTO();
        Token contractToken = tokenRepository.findByName(body.getTokenName());

        List<AddressReqDTO.Address> addressHashList = body.getAddressList();
        if (addressHashList != null && !addressHashList.isEmpty()) {
            List<String> contractTokenAddress = new ArrayList<>();
            if (CoinType.WHC.getName().equals(body.getTokenName())) {
                addressHashList.forEach(address -> {
                    ScriptType scriptType = address.getType() == 0 ? ScriptType.P2PKH : ScriptType.P2SH;
                    contractTokenAddress.add(AddressClient.toCashAddressByPubkHash(address.getAddressHash(),
                            scriptType.getScriptType(), networkType));
                });
            } else if (CoinType.usdt.getName().equals(body.getTokenName())) {
                addressHashList.forEach(address -> {
                    ScriptType scriptType = address.getType() == 0 ? ScriptType.P2PKH : ScriptType.P2SH;
                    contractTokenAddress.add(AddressClient.toLegacyAddressByPubkHash(address.getAddressHash(),
                            scriptType.getScriptType(), networkType));
                });
            }


            String dataStr = null;
            ContractTokenAddressAssets contractTokenAddressAssets = new ContractTokenAddressAssets();
            if (CoinType.WHC.getName().equals(body.getTokenName())) {
                 dataStr =  OkHttpUtil.http(Urls.GET_WHC_ADDRESS_ASSET)
                        .param("addr_list", contractTokenAddress)
                        .param("property_id", contractToken.getOwnerTokenId())
                        .post();
            } else if (CoinType.usdt.getName().equals(body.getTokenName())) {
                dataStr =  OkHttpUtil.http(Urls.GET_usdt_ADDRESS_ASSET)
                        .param("addr_list", contractTokenAddress)
                        .param("property_id", contractToken.getOwnerTokenId())
                        .post();
            }


            try {
                contractTokenAddressAssets = new Gson().fromJson(dataStr, ContractTokenAddressAssets.class);
            } catch (Exception e) {
                logger.error("NEWSERVICE 返回数据异常");
                CustomException.response(Error.SERVER_EXCEPTION);
            }


            if (contractTokenAddressAssets.getCode() != 0) {
                logger.error(contractTokenAddressAssets.getMsg());
                CustomException.response(Error.SERVER_EXCEPTION);
            }


            List<GetAddressAssetsRepDTO.GetAddressAssetInside> contentList = new ArrayList<>();
            contractTokenAddressAssets.getData().forEach(addressAsset -> {
                GetAddressAssetsRepDTO.GetAddressAssetInside assetInside =
                        new GetAddressAssetsRepDTO.GetAddressAssetInside();
                assetInside.setAddress(addressAsset.getAddress());
                assetInside.setAsset(addressAsset.getBalance());
                contentList.add(assetInside);

                String addressHash = AddressClient.addressToHash(CoinType.valueOf(body.getTokenName()).getCode(),
                        addressAsset.getAddress(), networkType);
                walletAddressDao.updateAddressBalance(addressHash, addressAsset.getBalance(), body.getTokenName());
            });
            result.setTokenName(body.getTokenName());
            result.setContent(contentList);
        } else {
            CustomException.response(Error.ERR_MSG_PARAM_ERROR);
        }
        return result;
    }

    @Override
    public GetAddressAssetsRepDTO getEosAccountAssets(EosAccountAssetReqDTO dto) {
        GetAddressAssetsRepDTO repDTO = new GetAddressAssetsRepDTO();
        List<GetAddressAssetsRepDTO.GetAddressAssetInside> list=Lists.newArrayList();
        dto.getAddList().forEach(address ->{
            address.getAccountList().forEach( account -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code","eosio.token");
                jsonObject.put("account",account);
                jsonObject.put("symbol","EOS");
                logger.info("eos资产查询请求开始:{}",jsonObject);
                JSONObject asset = httpService.httpPostWithJson(Urls.EOS_ACCOUNT_ASSET, jsonObject);
                if(asset == null || asset.getInt("code")!=0){
                    CustomException.response(Error.SERVER_EXCEPTION);
                }
                logger.info("eos资产请求数据结果:{}",asset);
                GetAddressAssetsRepDTO.GetAddressAssetInside assetInside = new GetAddressAssetsRepDTO.GetAddressAssetInside();
                assetInside.setAddress(account);
                JSONArray array = asset.getJSONArray("data");
                if(array.size()!=0){
                    assetInside.setAsset(array.getJSONObject(0).getLong("amount")+"");
                }else{
                    assetInside.setAsset("");
                }
                list.add(assetInside);
            });
        });
        repDTO.setContent(list);
        repDTO.setTokenName(CoinType.EOS.getName());
        return repDTO;
    }

    @Override
    public CommonResult getEOSBaseBuyRam() {
        CommonResult result = new CommonResult();
        JSONObject jsonObject = httpService.httpGet(Urls.EOS_BASE_PAY, null);
        logger.info("eos新建账户最少buyram查询:{}",jsonObject);
        JSONObject data = jsonObject.getJSONObject("data");
        double unitramforeos = Double.parseDouble(data.getString("unitramforeos"));
        double uniteosforram = Double.parseDouble(data.getString("uniteosforram"));
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("unitramforeos",unitramforeos*4);
        map.put("uniteosforram",uniteosforram);
        result.setData(map);
        return result;
    }



}
