package com.rst.cgi.service.impl;

import com.google.common.collect.Lists;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mysql.CommonDao;
import com.rst.cgi.data.dto.DepthLimit;
import com.rst.cgi.data.dto.TokenData;
import com.rst.cgi.data.dto.WalletData;
import com.rst.cgi.data.dto.request.AddAddressReq;
import com.rst.cgi.data.dto.request.SyncWalletDataReq;
import com.rst.cgi.data.dto.request.UpdateWalletUserInfoReq;
import com.rst.cgi.data.dto.response.UserInfo;
import com.rst.cgi.data.entity.*;
import com.rst.cgi.service.BlockChainService;
import com.rst.cgi.service.PushDeviceService;
import com.rst.cgi.service.UserService;
import com.rst.cgi.service.WalletDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Service
public class WalletDataServiceImpl implements WalletDataService {
    private final Logger logger = LoggerFactory.getLogger(WalletDataService.class);
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private UserService userService;

    @Autowired
    private BlockChainService blockChainService;
    @Autowired
    private PushDeviceService pushDeviceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletData syncWalletData(Integer user, SyncWalletDataReq request, String requestIp) {
        String operator = request.getOperator();
        WalletData data = request.getWalletData();

        //获取系统中已存在的钱包
        Wallet walletCondition = new Wallet();
        walletCondition.setPublicKey(request.getWalletData().getIdentify());
        Wallet wallet = commonDao.queryFirstBy(walletCondition);

        int userId;
        if (wallet == null || wallet.getOwner() == null) {
            if (user != null) {
                userId = user;
            } else {
                userId = userService.getUser(userService.createUser()).getId();
            }
        } else {
            userId = wallet.getOwner();
        }

        /**
         * 更新钱包信息
         */
        boolean update = true;
        if (wallet == null) {
            wallet = new Wallet();
            if (request.getOperation() != SyncWalletDataReq.OP_UPDATE) {
                wallet.setCreateType(request.getOperation());
            }
            wallet.setCreateIp(requestIp);
            wallet.setCreateTime(new Date());
            update = false;
        }

        wallet.setOwner(userId);
        wallet.setPublicKey(data.getIdentify());
        wallet.setUpdateTime(new Date(data.getUpdateTime()));
        wallet.setType(data.getType());
        wallet.setDesc(data.getDesc());
//        wallet.setName(data.getName());
        wallet.setFaceId(data.getFaceId());
        wallet.setKeyStatus(Wallet.AVAILABLE);

        if (!StringUtils.isEmpty(data.getOwnerDevice())) {
            wallet.setEquipmentNo(data.getOwnerDevice());
        }

        if (update) {
            commonDao.update(wallet);
        } else {
            commonDao.insert(wallet);
        }

        Wallet savedWallet = commonDao.queryFirstBy(walletCondition);
        if (!StringUtils.isEmpty(request.getWalletData().getOwnerDevice())) {
            pushDeviceService.addAvailableDeviceIds(request.getWalletData().getOwnerDevice(),
                    Arrays.asList(savedWallet.getId()));
        }

        /**
         * 更新钱包的地址和深度信息
         */
        if (data.getTokenData() != null && !data.getTokenData().isEmpty()) {
            List<TokenData> tokenDatasToSync = new ArrayList<>();
            data.getTokenData().forEach(tokenData -> {
                if (tokenData.getDepthLimits() != null) {
                    tokenData.getDepthLimits().forEach(depthLimit -> {
                        HdWalletPath walletPath = new HdWalletPath();
                        walletPath.setWalletId(savedWallet.getId());
                        walletPath.setPath(depthLimit.getParent());
                        walletPath = commonDao.queryFirstBy(walletPath);
                        boolean updatePath = true;
                        if (walletPath == null) {
                            updatePath = false;
                            walletPath = new HdWalletPath();
                            walletPath.setWalletId(savedWallet.getId());
                        }

                        walletPath.setDepth(depthLimit.getMaxIndex());
                        walletPath.setPath(depthLimit.getParent());
                        walletPath.setToken(tokenData.getName());

                        if (updatePath) {
                            commonDao.update(walletPath);
                        } else {
                            commonDao.insert(walletPath);
                        }
                    });
                }

                if (tokenData.getAddresses() != null) {
                    TokenData tokenDataToSync = updateAddress(
                            savedWallet.getId(), tokenData.getName(), tokenData.getAddresses());

                    if (tokenDataToSync.getAddresses() != null) {
                        tokenDatasToSync.add(tokenDataToSync);
                    }
                }
            });

            blockChainService.asyncUpdateTxHistoryFromBlockChain(tokenDatasToSync);
        }

        return getWalletData(savedWallet.getId());
    }

    @Override
    public void updateWalletUserInfo(UpdateWalletUserInfoReq request) {
        Wallet walletCondition = new Wallet();
        walletCondition.setPublicKey(request.getWalletId());
        walletCondition.setKeyStatus(Wallet.AVAILABLE);
        Wallet wallet = commonDao.queryFirstBy(walletCondition);

        if (wallet == null) {
            CustomException.response(Error.ERR_MSG_WALLET_NOT_EXIST);
        }

        if (wallet.getOwner() != null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(wallet.getOwner());
            userEntity.setPhone(request.getInfo().getPhone());
            userEntity.setEmail(request.getInfo().getEmail());
            userEntity.setNickName(request.getInfo().getNickName());
            commonDao.update(userEntity);
        } else {
            CustomException.response(Error.ERR_MSG_WALLET_NOT_EXIST);
        }
    }

    @Override
    public void addWalletAddress(AddAddressReq req) {
        Wallet walletCondition = new Wallet();
        walletCondition.setPublicKey(req.getWalletId());
        Wallet wallet = commonDao.queryFirstBy(walletCondition);
        if (wallet == null) {
            CustomException.response(Error.ERR_MSG_WALLET_NOT_EXIST);
        }

        if (req.getAddresses() != null) {
            List<TokenData> tokenDatasToSync = new ArrayList<>();

            TokenData tokenDataToSync =
                    updateAddress(wallet.getId(), req.getName(), req.getAddresses());

            if (tokenDataToSync.getAddresses() != null) {
                tokenDatasToSync.add(tokenDataToSync);
            }

            blockChainService.asyncUpdateTxHistoryFromBlockChain(tokenDatasToSync);
        }
    }

    private TokenData updateAddress(int walletId, String token,
                                    List<TokenData.Address> addresses) {
        TokenData tokenDataToSync = new TokenData();
        tokenDataToSync.setName(token);
        tokenDataToSync.setDepthLimits(null);

        addresses.forEach(address -> {
            WalletAddress walletAddress = new WalletAddress();
            walletAddress.setWalletId(walletId);
            walletAddress.setWalletAddress(address.getHash());
            walletAddress.setType(address.getType());
            WalletAddress found = commonDao.queryFirstBy(walletAddress);

            if (found == null || !token.equalsIgnoreCase(found.getToken())) {
                walletAddress.setToken(token);
                walletAddress.setSyncStatus(WalletAddress.STATUS_UN_SYNCHRONIZED);
                walletAddress.setCreatTime(new Date());
                walletAddress.setUpdateTime(new Date());
                commonDao.insert(walletAddress);
                //eos账户保存
                List<String> eosAccountList = address.getEosAccountList();
                if(eosAccountList != null && eosAccountList.size() != 0 ){
                    insertEOSAccount(eosAccountList,address.getHash());
                }
            }else if(found != null && found.getToken().equalsIgnoreCase("EOS")){
                //eos账户保存
                List<String> eosAccountList = address.getEosAccountList();
                List<String> notFound = Lists.newArrayList();
                if(eosAccountList != null && eosAccountList.size() != 0 ){
                    eosAccountList.forEach( eos -> {
                        EosAccount eosAccount = new EosAccount();
                        eosAccount.setWalletAddress(address.getHash());
                        eosAccount.setEosAccount(eos);
                        EosAccount eosfound = commonDao.queryFirstBy(eosAccount);
                        if(eosfound == null){
                           notFound.add(eos);
                        }
                    });
                }
                if(notFound != null && notFound.size() != 0){
                    insertEOSAccount(notFound,address.getHash());
                }
            }else {
                //历史数据兼容性考虑
                if (found.getToken() == null) {
                    found.setToken(token);
                    commonDao.update(found);
                }
            }

            if (walletAddress.getSyncStatus() == null ||
                    walletAddress.getSyncStatus() != WalletAddress.STATUS_SYNCHRONIZED) {
                tokenDataToSync.addAddress(address.getHash(),
                        address.getType(), address.getHdPath(), address.getEthNonce());
            }
        });

        return tokenDataToSync;
    }

    private void insertEOSAccount(List<String> eosAccountList,String hash){
        List<EosAccount> eosAccountList2=Lists.newArrayList();
        eosAccountList.forEach( account ->{
            EosAccount eosaccount = new EosAccount();
            eosaccount.setWalletAddress(hash);
            eosaccount.setEosAccount(account);
            eosaccount.setCreateTime(new Date());
            eosAccountList2.add(eosaccount);
        });
        commonDao.batchInsert(eosAccountList2,EosAccount.class);
    }

    @Override
    public UserInfo getUserInfo(String walletId) {
        Wallet walletCondition = new Wallet();
        walletCondition.setPublicKey(walletId);
        walletCondition.setKeyStatus(Wallet.AVAILABLE);
        Wallet wallet = commonDao.queryFirstBy(walletCondition);

        if (wallet == null || wallet.getOwner() == null) {
            return null;
        }

        return userService.getUser(wallet.getOwner());
    }

    @Override
    public void deleteWalletData(int userId, String publicKey) {
        Wallet walletCondition = new Wallet();
        walletCondition.setPublicKey(publicKey);
        walletCondition.setOwner(userId);
        Wallet wallet = commonDao.queryFirstBy(walletCondition);
        if (wallet == null) {
            CustomException.response(Error.ERR_MSG_WALLET_NOT_EXIST);
        }
        wallet.setKeyStatus(Wallet.UNAVAILABLE);

        if (!StringUtils.isEmpty(wallet.getEquipmentNo())) {
            pushDeviceService.deleteAvailableDeviceIds(
                    wallet.getEquipmentNo(), Arrays.asList(wallet.getId()));
        }

        commonDao.update(wallet);
    }

    @Override
    public void deleteWalletData(int walletId) {
        Wallet walletCondition = new Wallet();
        walletCondition.setId(walletId);
        Wallet wallet = commonDao.queryFirstBy(walletCondition);
        if (wallet == null) {
            CustomException.response(Error.ERR_MSG_WALLET_NOT_EXIST);
        }
        wallet.setKeyStatus(Wallet.UNAVAILABLE);
        commonDao.update(wallet);
    }

    @Override
    public WalletData getWalletData(int userId, String publicKey) {
        Wallet walletCondition = new Wallet();
        walletCondition.setPublicKey(publicKey);
        walletCondition.setOwner(userId);
        Wallet wallet = commonDao.queryFirstBy(walletCondition);
        return getWalletData(wallet);
    }

    @Override
    public WalletData getWalletData(int walletId) {
        Wallet walletCondition = new Wallet();
        walletCondition.setId(walletId);
        Wallet wallet = commonDao.queryFirstBy(walletCondition);
        return getWalletData(wallet);
    }

    @Override
    public long getLastUpdateTime(int userId, String publicKey) {
        Wallet walletCondition = new Wallet();
        walletCondition.setOwner(userId);
        walletCondition.setPublicKey(publicKey);
        Wallet wallet = commonDao.queryFirstBy(walletCondition);
        return wallet.getUpdateTime().getTime();
    }

    @Override
    public long getLastUpdateTime(int walletId) {
        Wallet walletCondition = new Wallet();
        walletCondition.setId(walletId);
        Wallet wallet = commonDao.queryFirstBy(walletCondition);
        return wallet.getUpdateTime().getTime();
    }

    private WalletData getWalletData(Wallet wallet) {
        if (wallet == null) {
            CustomException.response(Error.ERR_MSG_WALLET_NOT_EXIST);
        }

        WalletData data = new WalletData();
        data.setName(wallet.getName());
        data.setDesc(wallet.getDesc());
        data.setName(wallet.getPublicKey());
        data.setFaceId(wallet.getFaceId());
        data.setType(wallet.getType());
        data.setUpdateTime(wallet.getUpdateTime().getTime());

        WalletAddress walletAddressCondition = new WalletAddress();
        walletAddressCondition.setWalletId(wallet.getId());
        List<WalletAddress> walletAddresses = commonDao.queryBy(walletAddressCondition);

        HdWalletPath walletPathCondition = new HdWalletPath();
        walletPathCondition.setWalletId(wallet.getId());
        List<HdWalletPath> walletPaths = commonDao.queryBy(walletPathCondition);

        Map<String, List<HdWalletPath>> tokenToPaths =
                walletPaths.stream().filter(item -> item.getToken() != null)
                           .collect(Collectors.groupingBy(item -> item.getToken()));
        List<TokenData> tokenDataList =
                walletAddresses.stream().filter(item -> item.getToken() != null)
                       .collect(Collectors.groupingBy(item -> item.getToken()))
                       .entrySet()
                       .stream()
                       .map(entry -> {
                           List<HdWalletPath> hdWalletPaths = tokenToPaths.get(entry.getKey());
                           List<DepthLimit> paths = new ArrayList<>();
                           if (hdWalletPaths != null) {
                               paths = hdWalletPaths.stream()
                                                    .map(path ->
                                                            new DepthLimit(path.getPath(), path.getDepth()))
                                                    .collect(Collectors.toList());
                           }

                           List<TokenData.Address> addresses =
                                   entry.getValue()
                                        .stream()
                                        .map(walletAddress ->
                                                new TokenData.Address(
                                                        walletAddress.getWalletAddress(), walletAddress.getType(),
                                                        walletAddress.getHdPath(), walletAddress.getEthNonce()))
                                        .collect(Collectors.toList());

                           return new TokenData(entry.getKey(), addresses, paths);
                       }).collect(Collectors.toList());

        data.setTokenData(tokenDataList);

        data.setOwner(userService.getUser(wallet.getOwner()));

        return data;
    }
}
