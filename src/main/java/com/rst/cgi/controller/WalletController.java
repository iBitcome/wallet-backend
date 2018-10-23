package com.rst.cgi.controller;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.constant.ServerMainUris;
import com.rst.cgi.common.crypto.Converter;
import com.rst.cgi.common.crypto.ECDSAUtil;
import com.rst.cgi.common.hbc.DecryptRequest;
import com.rst.cgi.common.hbc.EncryptResponse;
import com.rst.cgi.common.utils.BeanCopier;
import com.rst.cgi.common.utils.DHUtil;
import com.rst.cgi.common.utils.IpUtil;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.controller.interceptor.LogHide;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.WalletData;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.*;
import com.rst.cgi.data.entity.UserEntity;
import com.rst.cgi.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.util.*;

/**
 * Created by mtb on 2018/3/27.
 */
@Api(tags = "钱包相关接口")
@RestController
@RequestMapping("/mobile/wallet")
public class WalletController {

    @Autowired
    private EncryptDecryptService encryptDecryptService;
    @Autowired
    private WalletService walletService;



    @ApiOperation(value = "获取加密私钥，返回内容为私钥，" +
            "response header包含后续加密请求必备的头部：TOKEN-CODE。")
    @PostMapping("/getKey")
    public CommonResult<String> getKey(HttpServletResponse response) {
        String keyIndex = UUID.randomUUID().toString();
        response.setHeader(EncryptDecryptService.KEY_INDEX_HEADER, keyIndex);
        CommonResult<String> ret = new CommonResult<>();
        ret.setOK();
        ret.setData(encryptDecryptService.getKey(
                keyIndex, true));
        return ret;
    }

    @ApiOperation(value = "获取加密私钥，返回内容为私钥，" +
            "response header包含后续加密请求必备的头部：TOKEN-CODE。")
    @GetMapping("/dh/getKey/{pubKey}")
    public CommonResult<DHGetKeyRes> dhGetKey(
            @ApiParam("客户端本地即时生成DH公私钥，传输公钥以获取服务端公钥进而在本地生成AES密钥")
            @PathParam("pubKey") String pubKey, HttpServletResponse response) {
        try {
            byte[] pubKeyBytes = Converter.hexStringToByteArray(pubKey);
            DHUtil.Key key = DHUtil.initKey(Converter.hexStringToByteArray(pubKey));
            String keyIndex = Converter.byteArrayToHexString(Converter.sha256(pubKeyBytes));
            String aesKey = encryptDecryptService.getKey(keyIndex, false);

            if (StringUtils.isEmpty(aesKey)) {
                aesKey = new String(DHUtil.aesKeyFrom(pubKeyBytes, key.getPriv()));
                encryptDecryptService.saveKey(keyIndex, aesKey);
            } else {
                /**
                 * 这个条件下，客户端可以无需从服务器获取，直接用sha256(pubKey)作为keyIndex；
                 * 故而这里是可以直接返回错误码而防止重放攻击;
                 * 这里不做处理的原因是客户端处理会更简单，但是推荐返回错误码以防重放攻击
                 */
            }

            response.setHeader(EncryptDecryptService.KEY_INDEX_HEADER, keyIndex);

            //防止中间人攻击，客户端需通过签名鉴别服务器身份
            String signature = Converter.byteArrayToHexString(ECDSAUtil.signEth(
                    Converter.sha256(key.getPub()),
                    Converter.hexStringToByteArray(ServerMainUris.CGI_PRIVATE_KEY)).encodeToDER());

            DHGetKeyRes res = new DHGetKeyRes();
            res.setPubKey(Converter.byteArrayToHexString(key.getPub()));
            res.setSignature(signature);
            return CommonResult.make(res);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.make(Error.SERVER_EXCEPTION);
        }
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "新增或删除设备钱包")
    @PostMapping("/manageWallet")
    public CommonResult manageWallet(@RequestBody ManageWalletReqDTO body, HttpServletRequest request){
        CommonResult rst = new CommonResult();
        List<NumbersManageWalletReqDTO.ManageWalletInfo> WalletInfos = new ArrayList<>();
        NumbersManageWalletReqDTO.ManageWalletInfo walletInfo =
                new NumbersManageWalletReqDTO.ManageWalletInfo();
        walletInfo.setWalletAddress(body.getWalletAddress());
        walletInfo.setWalletPathDepth(body.getWalletPathDepth());
        walletInfo.setType(body.getAddressType());
        WalletInfos.add(walletInfo);

        NumbersManageWalletReqDTO reqDTO = new NumbersManageWalletReqDTO();
        reqDTO.setWalletInfoList(WalletInfos);
        BeanCopier.getInstance().copyBean(body, reqDTO);

        walletService.manageWallet(reqDTO, request);
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "批量新增或删除设备钱包")
    @PostMapping("/numbersManageWallet")
    public CommonResult numbersManageWallet(@RequestBody NumbersManageWalletReqDTO body, HttpServletRequest request){
        CommonResult rst = new CommonResult();
        walletService.manageWallet(body, request);
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "指定设备所有钱包重置为未添加状态")
    @PostMapping("/stopEquipmentWallet")
    public CommonResult stopEquipmentWallet(@RequestBody StopWalletDTO body){
        CommonResult rst = new CommonResult();
        walletService.stopEquipmentWallet(body);
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "初始化设备的钱包推送状态")
    @PostMapping("/initWallet")
    public CommonResult initWallet(@RequestBody InitWalletDTO body){
        CommonResult rst = new CommonResult();
        pushDeviceService.initAvailableDevices(body);
        return rst;
    }

    @LogHide(value = LogHide.REP)
    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取目前系统中支持的币种以及进制指数")
    @GetMapping("/getTokens")
    public CommonResult<List<TokensRepDTO>> getTokens(){
        CommonResult<List<TokensRepDTO>> rst = new CommonResult();
        rst.setData(walletService.getTokens());
        return rst;
    }

    @LogHide(value = LogHide.REP)
    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取目前系统代币（新）")
    @PostMapping("/getTokensNew")
    public CommonResult<GetTokensNewRepDTO> getTokensNew(@RequestBody GetTokensNewReqDTO body){
        CommonResult<GetTokensNewRepDTO> rst = new CommonResult();
        rst.setData(walletService.getTokensNew(body));
        return rst;
    }

    @ApiOperation(value = "获取目前系统中支持的币种的价格")
    @GetMapping("/anon/getTokenPrice")
    public CommonResult<Map<String, Double>> getTokenPrice(
            @ApiParam("需要查询的token集合（不传查询所有") @RequestParam(required = false) List<String> tokenList){
        CommonResult<Map<String, Double>> rst = new CommonResult();
        rst.setData(walletService.getTokenPrice(tokenList));
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取指定以太坊钱包地址的所有资产")
    @PostMapping("/getAssets")
    public CommonResult<List<AssetsRepDTO>> getAssets(@RequestBody WalletReqDTO body){
        CommonResult<List<AssetsRepDTO>> rst = new CommonResult();
        rst.setData(walletService.getAssets(body));
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取指定HD钱包地址的所有资产")
    @PostMapping("/getHdAssets")
    public CommonResult<List<AssetsRepDTO>> getHdAssets(@RequestBody HdWalletReqDTO body){
        CommonResult<List<AssetsRepDTO>> rst = new CommonResult();
        rst.setData(walletService.getHdAssets(body));
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取钱包地址下的BTC和BCH的UTXO")
    @PostMapping("/getUTXO")
    public CommonResult<List<UTXORepDTO>> getUTXO(@RequestBody UTXOReqDTO body){
        CommonResult<List<UTXORepDTO>> rst = new  CommonResult();
        rst.setData(walletService.getUTXO(body));
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取钱包地址下的ZCASH的UTXO")
    @PostMapping("/getZcashUTXO")
    public CommonResult<List<UTXORepDTO>> getZcashUTXO(@RequestBody UTXOReqDTO body){
        CommonResult<List<UTXORepDTO>> rst = new  CommonResult();
        rst.setData(walletService.getZcashUtxo(body));
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "检查地址列表中的地址在指定的代币下是否有过交易")
    @PostMapping("/checkTranx")
    public CommonResult<CheckTranxRepDTO> checkTranx(@RequestBody CheckTranxReqDTO body){
        CommonResult<CheckTranxRepDTO> rst = new  CommonResult();
        rst.setData(walletService.checkTranx(body));
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询地址列表中每个地址对应的资产")
    @PostMapping("/getAddressAssets")
    public CommonResult<GetAddressAssetsRepDTO> getAddressAssets(@RequestBody GetAddressAssetsReqDTO body){
        CommonResult<GetAddressAssetsRepDTO> rst = new  CommonResult();
        rst.setData(walletService.getAddressAssets(body));
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询地址列表中每个地址对应的WHC资产")
    @PostMapping("/getAddressWHCAssets")
    public CommonResult<GetAddressAssetsRepDTO> getAddressWHCAssets(@RequestBody AddressReqDTO body){
        CommonResult<GetAddressAssetsRepDTO> rst = new  CommonResult();
        rst.setData(walletService.getAddressContractTokenAssetsB(body));
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询B链地址列表中每个地址对应的资产")
    @PostMapping("/getAddressContractTokenAssetsB")
    public CommonResult<GetAddressAssetsRepDTO> getAddressContractTokenAssetsB(@RequestBody AddressReqDTO body){
        CommonResult<GetAddressAssetsRepDTO> rst = new  CommonResult();
        rst.setData(walletService.getAddressContractTokenAssetsB(body));
        return rst;
    }



    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询地址列表中每个账户对应的EOS资产")
    @PostMapping("/getEosAccountAssets")
    public CommonResult<GetAddressAssetsRepDTO> getAddressEOSAssets(@RequestBody EosAccountAssetReqDTO body){
        CommonResult<GetAddressAssetsRepDTO> rst = new  CommonResult();
        rst.setData(walletService.getEosAccountAssets(body));
        return rst;
    }

    @Autowired
    private WalletDataService dataSyncService;
    @Autowired
    private UserService userService;

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "同步钱包数据")
    @PostMapping("/syncWallets")
    public CommonResult<WalletData> syncWalletData(
            @RequestBody SyncWalletDataReq request,  HttpServletRequest servletRequest) {
        Integer userId = null;
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user != null) {
            userId = user.getId();
        }

        return CommonResult.make(
                dataSyncService.syncWalletData(userId, request, IpUtil.clientIpFrom(servletRequest)));
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "解除钱包与设备的关联关系")
    @PostMapping("/deleteWallet")
    public CommonResult<WalletData> deleteWallet(
            @RequestBody DeleteWalletReq request) {
        pushDeviceService.deleteAvailableDevices(
                request.getDevice(), Arrays.asList(request.getWalletId()));
        return new CommonResult<>();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "更新钱包用户数据")
    @PostMapping("/updateWalletUserInfo")
    public CommonResult updateWalletUserInfo(
            @RequestBody UpdateWalletUserInfoReq request) {
        dataSyncService.updateWalletUserInfo(request);
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "批量获取钱包用户数据")
    @PostMapping("/getWalletUserInfo")
    public CommonResult<Map<String, UserInfo>> getWalletUserInfo(
            @RequestBody GetWalletUserInfoReq request) {
        Map<String, UserInfo> result = new HashMap<>();

        request.getWalletIds().forEach(
                walletId -> result.put(walletId, dataSyncService.getUserInfo(walletId)));

        return CommonResult.make(result);
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "批量添加钱包地址")
    @PostMapping("/addWalletAddress")
    public CommonResult addWalletAddress(
            @RequestBody AddAddressReq request) {
        dataSyncService.addWalletAddress(request);
        return new CommonResult();
    }

    @Autowired
    private PushDeviceService pushDeviceService;

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "更新本地接收推送的钱包列表（所有）")
    @PostMapping("/updatePushInfo")
    public CommonResult updatePushInfo(
            @RequestBody UpdatePushInfoReq request) {
        pushDeviceService.updateAvailableDevices(request.getDevice(), request.getWalletIds());
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "从本地设备增加一些钱包，开始接收推送")
    @PostMapping("/addPushInfo")
    public CommonResult addPushInfo(
            @RequestBody UpdatePushInfoReq request) {
        pushDeviceService.addAvailableDevices(request.getDevice(), request.getWalletIds());
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询EOS buyram")
    @RequestMapping(value ="/eosBuyRam",method = RequestMethod.POST)
    public CommonResult getEOSAccountBuyRam(){
        CommonResult result = walletService.getEOSBaseBuyRam();
        return result;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "公钥查询EOS账户")
    @RequestMapping(value ="/checkAccount",method = RequestMethod.POST)
    public CommonResult<List<EOSAccountDto>> getEOSAccount(@ApiParam(value = "公钥",required = true)@RequestParam String publicKey){
        CommonResult<List<EOSAccountDto>> result = new CommonResult<>();
        List<EOSAccountDto> accountList = walletService.eosAcoountList(publicKey);
        result.setData(accountList);
        return result;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "新建EOS账户/eos转账交易")
    @RequestMapping(value = "/createTran",method = RequestMethod.POST)
    public CommonResult addEOSAccount(@ApiParam(value = "创建账户或者交易 json",required = true) @RequestBody EOStransactionReqDto dto){
        CommonResult result = walletService.EOStran(dto);
        return result;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "新建EOS账户生成real")
    @RequestMapping(value = "/acReal",method = RequestMethod.POST)
    public CommonResult getEOSReal(@ApiParam(value = "创建账户")@RequestBody EOSCreateAccountDTO dto){
        CommonResult result = new CommonResult();
        result.setData(walletService.accountReal(dto));
        return result;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "EOS转账交易账户生成real")
    @RequestMapping(value = "/txReal",method = RequestMethod.POST)
    public CommonResult getTranReal(@ApiParam(value = "发起交易")@RequestBody EOSTransactionDTO dto){
        CommonResult result = new CommonResult();
        result.setData(walletService.txReal(dto));
        return result;
    }

}
