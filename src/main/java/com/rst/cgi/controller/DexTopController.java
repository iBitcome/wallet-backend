package com.rst.cgi.controller;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.hbc.DecryptRequest;
import com.rst.cgi.common.hbc.EncryptResponse;
import com.rst.cgi.conf.ExchangeConfig;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.DexTopMarket;
import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.dto.SymbolBrief;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.*;
import com.rst.cgi.data.entity.TradeOrder;
import com.rst.cgi.data.entity.UserEntity;
import com.rst.cgi.service.MarketCacheService;
import com.rst.cgi.service.TradeCenterService;
import com.rst.cgi.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hujia
 */
@Api(tags = "Dex.Top交易所接口")
@RestController
@RequestMapping("/dex")
public class DexTopController {
    @Autowired
    private MarketCacheService marketCacheService;
    @Autowired
    private UserService userService;

    @ApiOperation(value = "获取交易所配置信息")
    @GetMapping("/anon/getConfig")
    public CommonResult<DexTopMarket> getConfig() {
        return CommonResult.make((DexTopMarket)
                marketCacheService.getConfig(ExchangeConfig.DEX_TOP.getName()));
    }

    @ApiOperation(value = "获取交易对信息-http://cgi.com/list/symbol?baseAsset=ETH&quoteAsset=LOOM")
    @GetMapping("/anon/list/symbol")
    public CommonResult<List<SymbolBrief>> listSymbol(
            @ApiParam("交易对的基础代币,不传则list所有")
            @RequestParam(value = "baseAsset", required = false) String baseAsset,
            @ApiParam("交易对的商品代币,不传则list所有")
            @RequestParam(value = "quoteAsset", required = false) String quoteAsset) {
        //fetch data from dex.top
        return CommonResult.make(
                marketCacheService.getSymbolData(ExchangeConfig.DEX_TOP.getName(), baseAsset, quoteAsset));
    }

    @ApiOperation(value = "获取k线数据-http://cgi.com/ETH/BTC/kline/1h?from=1211322&to=2323244")
    @GetMapping("/anon/{baseAsset}/{quoteAsset}/kline/{interval}")
    public CommonResult<GetKlineRes> getKlinePoints(
            @ApiParam("交易对的基础代币") @PathVariable("baseAsset") String baseAsset,
            @ApiParam("交易对的商品代币") @PathVariable("quoteAsset") String quoteAsset,
            @ApiParam("k线的周期") @PathVariable("interval") String interval,
            @ApiParam("数据起始点") @RequestParam(value = "from") Long fromTime,
            @ApiParam("数据结束点") @RequestParam("to") Long toTime) {
        //fetch data from dex.top
        return CommonResult.make(marketCacheService.getKlinePoints(
                        new Symbol(baseAsset, quoteAsset), ExchangeConfig.DEX_TOP.getName(),
                        interval, fromTime, toTime));
    }

    @ApiOperation(value = "获取分时交易数据-http://cgi.com/ETH/BTC/trades?from=1211322&to=2323244")
    @GetMapping("/anon/{baseAsset}/{quoteAsset}/trades")
    public CommonResult<GetTradesRes> getTrades(
            @ApiParam("交易对的基础代币") @PathVariable("baseAsset") String baseAsset,
            @ApiParam("交易对的商品代币") @PathVariable("quoteAsset") String quoteAsset,
            @ApiParam("数据起始点") @RequestParam("from") Long fromTime,
            @ApiParam("数据结束点") @RequestParam("to") Long toTime) {
        //fetch data from dex.top
        return CommonResult.make(marketCacheService.getTradePoints(
                new Symbol(baseAsset, quoteAsset), ExchangeConfig.DEX_TOP.getName(),
                fromTime, toTime));
    }

    @ApiOperation(value = "获取最新分时交易数据-http://cgi.com/anon/ETH/BTC/trades/current")
    @GetMapping("/anon/{baseAsset}/{quoteAsset}/trades/current")
    public CommonResult<GetTradesRes> getTrades(
            @ApiParam("交易对的基础代币") @PathVariable("baseAsset") String baseAsset,
            @ApiParam("交易对的商品代币") @PathVariable("quoteAsset") String quoteAsset) {
        //ftch data from dex.top
        return CommonResult.make(marketCacheService.getTradePoints(
                new Symbol(baseAsset, quoteAsset), ExchangeConfig.DEX_TOP.getName()));
    }

    @ApiOperation(value = "获取交易深度-http://cgi.com/ETH/BTC/trades?from=1211322&to=2323244")
    @GetMapping("/anon/{baseAsset}/{quoteAsset}/depth/current")
    public CommonResult<GetSymbolDepthRes> getDepth(
            @ApiParam("交易对的基础代币") @PathVariable("baseAsset") String baseAsset,
            @ApiParam("交易对的商品代币") @PathVariable("quoteAsset") String quoteAsset) {
        //fetch data from dex.top
        return CommonResult.make(marketCacheService.getDepthPoints(
                new Symbol(baseAsset, quoteAsset), ExchangeConfig.DEX_TOP.getName()));
    }

    @Autowired
    private TradeCenterService tradeCenterService;

    @ApiOperation(value = "测试用(调用订单相关接口前需先绑定信息)：绑定交易所信息：账号、密码、绑定的地址")
    @PostMapping("/anon/bind")
    public CommonResult bind(
            @ApiParam("交易对的基础代币") @RequestBody BindExchangeAccountReq req) {
        //fetch data from dex.top
        tradeCenterService.bind(req);
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "委托交易")
    @PostMapping("/placeOrder")
    public CommonResult<TradeOrder> placeOrder(
            @ApiParam("交易对的基础代币") @RequestBody PlaceOrderReq req) {
        //fetch data from dex.top
        return CommonResult.make(
                tradeCenterService.placeOrder(req));
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "取消交易")
    @PostMapping("/cancleOrder")
    public CommonResult cancleOrder(@RequestBody CancelOrderReq req) {
        //fetch data from dex.top
        tradeCenterService.CancelOrder(req);
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "提现")
    @PostMapping("/withdraw")
    public CommonResult withdraw(@RequestBody WithdrawReq req) {
        //fetch data from dex.top
        tradeCenterService.withdraw(req);
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "提现")
    @PostMapping("/connect")
    public CommonResult connect(@RequestBody ConnectExchangeReq req) {
        //fetch data from dex.top
        tradeCenterService.connect(req);
        return new CommonResult();
    }

    @ApiOperation(value = "测试交易所连接")
    @GetMapping("/testConnect/{address}")
    public CommonResult testConnect(@ApiParam("交易地址") @PathVariable("address") String address) {
        //fetch data from dex.top
        tradeCenterService.isConnected(address);
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取订单详情")
    @PostMapping("/getOrderDetail")
    public CommonResult<TradeOrder> getOrderDetail(@RequestBody GetOrderDetailReq req) {
        //fetch data from dex.top
        return CommonResult.make(
                tradeCenterService.getOrderDetail(req));
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取订单列表")
    @PostMapping("/getOrderList")
    public CommonResult<GetOrderListRes> getOrderList(@RequestBody GetOrderListReq req) {
        //fetch data from dex.top
        return CommonResult.make(
                tradeCenterService.getOrderList(req));
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "标记交易对为自选或取消")
    @PostMapping("/markSymbol")
    public CommonResult<GetOrderListRes> markSymbol(@RequestBody MarkSymbolReq req) {
        //fetch data from dex.top
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        tradeCenterService.markSymbol(user.getId(), req);
        return new CommonResult();
    }

    @ApiOperation(value = "获取收藏的交易对列表")
    @GetMapping("/getFavoriteSymbols")
    public CommonResult<List<SymbolBrief>> getFavoriteSymbols() {
        //fetch data from dex.top
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }

        return CommonResult.make(
                tradeCenterService.getFavoriteSymbols(user.getId()));
    }

    @ApiOperation(value = "获取交易余额情况")
    @GetMapping("/balances/{address}")
    public CommonResult<GetBalanceRes> getBalances(
            @ApiParam("交易对的基础代币") @PathVariable("address") String address) {
        //fetch data from dex.top
        return CommonResult.make(
                tradeCenterService.getBalances(address));
    }


    @ApiOperation("查询充值记录")
    @GetMapping("/getRechargeRecord/{type}/{address}")
    public CommonResult<List<RechargeRecordRepDTO>> getRechargeRecord(
            @ApiParam("记录类（1：充值记录，2：提现记录，0:查询所有充值与体现记录") @PathVariable(value = "type") Integer type,
            @ApiParam("交易地址") @PathVariable(value = "address") String address
    ) {
        return CommonResult.make(
                tradeCenterService.getRechargeRecord(address, type));
    }

    @ApiOperation("查询授权记录所在高度")
    @GetMapping("/approveHeight/{token}/{address}")
    public CommonResult<Integer> getApproveHeight(
            @ApiParam("代币简称") @PathVariable(value = "token") String token,
            @ApiParam("地址") @PathVariable(value = "address") String address
    ) {
        return CommonResult.make(
                tradeCenterService.getApproveHeight(token, address));
    }
}
