package com.rst.cgi.controller;

import com.rst.cgi.common.constant.CommonPage;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.hbc.DecryptRequest;
import com.rst.cgi.common.hbc.EncryptResponse;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.controller.interceptor.LogHide;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.PageRep;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.*;
import com.rst.cgi.data.entity.Rate;
import com.rst.cgi.service.BlockChainService;
import com.rst.cgi.service.ETHNonceService;
import com.rst.cgi.service.TransactionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mtb on 2018/3/26.
 */
@Api(tags = "交易相关接口")
@RestController
@RequestMapping("/mobile/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ETHNonceService ethNonceService;

    @LogHide(value = LogHide.REP)
    @DecryptRequest
    @EncryptResponse
    @ApiOperation("查询指定钱包的所有交易记录")
    @PostMapping("/getAllTransaction")
    public CommonResult<CommonPage<GetAllTransactionRepDTO>> getAllTransaction(@RequestBody GetAllTransactionReqDTO body) {
        CommonResult<CommonPage<GetAllTransactionRepDTO>> rst = new CommonResult<>();
        rst.setData(transactionService.getAllTransaction(body));
        return rst;
    }

    @LogHide(value = LogHide.REP)
    @DecryptRequest
    @EncryptResponse
    @ApiOperation("查询指定HD钱包的所有交易记录")
    @PostMapping("/getHdAllTransaction")
    public CommonResult<CommonPage<GetAllTransactionRepDTO>> getHdAllTransaction(
            @RequestBody GetAllHdTransactionReqDTO body) {
        CommonResult<CommonPage<GetAllTransactionRepDTO>> rst = new CommonResult<>();
        CommonPage<GetAllTransactionRepDTO> hdAllTransaction = transactionService.getHdAllTransaction(body);
        rst.setData(hdAllTransaction);
        return rst;
    }



    @ApiOperation("获取和当前美元与其他国家币种的汇率")
    @PostMapping("/getAllRate")
    public CommonResult<List<Rate>> getAllRate() {
        CommonResult<List<Rate>> rst = new CommonResult<>();
        rst.setData(transactionService.getAllRate());
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation("获取当前区块最新高度")
    @PostMapping("/getBlockHeight")
    public CommonResult<Integer> getBlockHeight(@RequestBody GetBlockHeightReqDTO body) {
        CommonResult<Integer> rst = new CommonResult<>();
        rst.setData(transactionService.getBlockHeight(body));
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation("发起BTC/BCH交易并发布到链上")
    @PostMapping("/sendTransactionB")
    public CommonResult<String> sendTransactionB(
            @RequestBody SendtranxBReqDTO body) {

        if (CurrentThreadData.clientVersion() == null
                || CurrentThreadData.clientVersion() <= 1) {
            return CommonResult.make(Error.ERR_MSG_VERSION_TOO_OLD);
        }

        CommonResult<String> rst = new CommonResult<>();
        rst.setData(transactionService.sendTransactionB(body));
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation("获取交易矿工费")
    @PostMapping("/getTxFee")
    public CommonResult<List<GetTxFeeRepDTO>> getTxFee(
            @RequestBody GetTxFeeReqDTO body) {
        CommonResult<List<GetTxFeeRepDTO>> rst = new CommonResult<>();
        rst.setData(transactionService.getTxFee(body));
        return rst;
    }

    private BlockChainService blockChainService;

    @DecryptRequest
    @EncryptResponse
    @ApiOperation("查询指定的所有交易记录")
    @PostMapping("/getTransactionHistory")
    public CommonResult<TransactionHistoryRes> getTransactionHistory(@RequestBody TransactionHistoryReq body) {
        CommonResult<TransactionHistoryRes> rst = new CommonResult<>();
        rst.setData(blockChainService.getTransactionHistory(body));
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation("通过交易id查询的交易记录")
    @PostMapping("/getTransactionById")
    public CommonResult<TransactionRes> getTransactionById(@RequestBody GetTransactionByIdReqDTO body) {
        CommonResult<TransactionRes> rst = new CommonResult<>();
        rst.setData(transactionService.getTransactionById(body));
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "发起以太坊交易并发布到链上(包括充值)", notes = "返回数据说明：txId-交易hash")
    @PostMapping("/sendTransactionEth")
    public CommonResult<Map<String, String>> sendTransactionEth(
            @RequestBody SendTranxEReqDTO body) {
        CommonResult<Map<String, String>> rst = new CommonResult<>();
        rst.setData(transactionService.sendTransactionEth(body));
        return rst;
    }


    @ApiOperation(value = "获取指定钱包地址的nonce", notes = "返回数据说明：nonce(long)-该地址目前可用nonce")
    @GetMapping("/anon/getNonceE/{address}")
    public CommonResult<Map<String, Long>> getNonceE(
            @ApiParam("钱包地址") @PathVariable("address") String address) {
        CommonResult<Map<String, Long>> rst = new CommonResult<>();
        Map<String, Long> nonceMap = new HashMap<>();
        nonceMap.put("nonce", ethNonceService.nextNonce(address));
        rst.setData(nonceMap);
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取gasLimit", notes = "返回数据说明：gasLimit(long)-该交易预估gasLimit")
    @PostMapping("/anon/getGasLimit")
    public CommonResult<Map<String, Long>> getGasLimit(@RequestBody GetGasLimitReqDto body) {
        CommonResult<Map<String, Long>> rst = new CommonResult<>();
        rst.setData(transactionService.getGasLimit(body.getFromAddress(),
                body.getToAddress(), body.getTokenName()));
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取B链合约代币载荷数据",notes = "返回数据：{payload：完成组装的payload}")
    @PostMapping("/anon/getPayload")
    public CommonResult<Map<String, String>> getPayload(@RequestBody GetPayloadReqDTO body) {
        CommonResult<Map<String, String>> rst = new CommonResult<>();
        rst.setData(transactionService.getPayload(body));
        return rst;
    }



    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询WHC的燃烧记录",notes = "返回数据：{}")
    @PostMapping("/anon/getWHCBurnHistory")
    public CommonResult<List<WHCBurnHistoryRepDTO>> getWHCBurnHistory(@RequestBody WHCBurnHistoryReqDTO body) {
        CommonResult<List<WHCBurnHistoryRepDTO>> rst = new CommonResult<>();
        rst.setData(transactionService.getWHCBurnHistory(body));
        return rst;
    }



    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询网关兑换记录列表")
    @PostMapping("/getExRecord")
    public CommonResult<PageRep<GetExRecordRepDTO>> getExRecord(@RequestBody GetExRecordReqDTO body) {
        CommonResult<PageRep<GetExRecordRepDTO>> rst = new CommonResult<>();
        rst.setData(transactionService.getExRecord(body));
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询网关兑换详情")
    @PostMapping("/getExRecordInfo")
    public CommonResult<GetExInfoRepDTO> getExRecordInfo(@RequestBody GetExInfoReqDTO body) {
        CommonResult<GetExInfoRepDTO> rst = new CommonResult<>();
        rst.setData(transactionService.getExRecordInfo(body));
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "验证zcash地址是否有效",notes = "{address:z_sasfsdgdfjnghdfhfghfgj}")
    @PostMapping("/validZcashAddress")
    public CommonResult validZcashAddress(@RequestBody Map map){
        CommonResult commonResult = transactionService.validZcashAddress(map);
        return commonResult;
    }

    @ApiOperation(value = "获取zcash高度")
    @GetMapping("/zcashHeight")
    public CommonResult getZcashHeight(){
        CommonResult zcashHeight = transactionService.getZcashHeight();
        return zcashHeight;
    }
}
