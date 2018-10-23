package com.rst.cgi.service;

import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by mtb on 2018/3/29.
 */
public interface WalletService {
    /**
     * 钱包的添加删除
     * @param body
     */
    void manageWallet(NumbersManageWalletReqDTO body, HttpServletRequest request);

    /**
     * 获取系统支持的所有代币
     * @return
     */
    List<TokensRepDTO> getTokens();

    /**
     * 获取指定以太坊钱包地址的所有资产
     * @param body
     * @return
     */
    List<AssetsRepDTO> getAssets(WalletReqDTO body);

    /**
     * 获取指定HD钱包地址的所有资产
     * @param body
     * @return
     */
    List<AssetsRepDTO> getHdAssets(HdWalletReqDTO body);

    /**
     * 指定设备所有钱包重置为未添加状态
     * @param body
     */
    void stopEquipmentWallet(StopWalletDTO body);

//    void resetEquipmentWallet(ResetWalletDTO body);

    /**
     * 获取钱包地址下的BTC和BCH的UTXO
     * @param body
     * @return
     */
    List<UTXORepDTO> getUTXO(UTXOReqDTO body);

    /**
     * 检查地址列表中的地址在指定的代币下是否有过交易
     * @param body
     * @return
     */
    CheckTranxRepDTO checkTranx(CheckTranxReqDTO body);

    /**
     * 查询地址列表中每个地址对应的资产
     * @param body
     * @return
     */
    GetAddressAssetsRepDTO getAddressAssets(GetAddressAssetsReqDTO body);

    /**
     * 获取代币列表中所有代币对应的美元价格
     * @param tokenList
     * @return
     */
    Map<String,Double> getTokenPrice(List<String> tokenList);

    /**
     * 公钥查询链上EOS账户
     * @param publicKey
     * @return
     */
    List<EOSAccountDto> eosAcoountList(String publicKey);

    /**
     * 创建EOS账户/发起EOS交易
     */
     CommonResult EOStran(EOStransactionReqDto dto);

    /**
     * 创建EOS账户生成real
     * @param dto
     * @return
     */
    EOStranactionRepDTO accountReal(EOSCreateAccountDTO dto);

    /**
     * 发起交易生成real
     * @param dto
     * @return
     */
    EOStranactionRepDTO txReal(EOSTransactionDTO dto);


    /**
     * 获取B链上合约代币地址列表对应的资产
     * @param body
     * @return
     */
    GetAddressAssetsRepDTO getAddressContractTokenAssetsB(AddressReqDTO body);

    /**
     * 获取EOS账户资产
     * @param dto
     * @return
     */
    GetAddressAssetsRepDTO getEosAccountAssets(EosAccountAssetReqDTO dto);

    /**
     * 获取基础EOS账户BuyRam
     * @return
     */
    CommonResult getEOSBaseBuyRam();

    /**
     * 获取系统支持的所有代币(新)
     * @return
     */
    GetTokensNewRepDTO getTokensNew(GetTokensNewReqDTO body);

    /**
     * 获取Zcash UTXO
     * @param body
     * @return
     */
    List<UTXORepDTO> getZcashUtxo(UTXOReqDTO body);
}
