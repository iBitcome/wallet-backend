package com.rst.cgi.controller;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.enums.Money;
import com.rst.cgi.common.hbc.DecryptRequest;
import com.rst.cgi.common.hbc.EncryptResponse;
import com.rst.cgi.common.utils.DateUtil;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.response.TokenPriceHistoryDTO;
import com.rst.cgi.data.entity.TokenPriceHistory;
import com.rst.cgi.data.entity.TradePoint;
import com.rst.cgi.service.FlatMoneyService;
import com.rst.cgi.service.TokenPriceHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 行情相关接口
 * @author huangxiaolin
 * @date 2018-05-17 下午7:45
 */
@Api(tags = "行情相关接口")
@RestController
@RequestMapping("/market")
public class MarketController {

//    @Autowired
//    private FlatMoneyService flatMoneyService;
    @Autowired
    private TokenPriceHistoryService tokenPriceHistoryService;
    @Autowired
    private FlatMoneyService flatMoneyService;

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "展示代币价格接口",notes = "json参数说明：paramMap={\"tokenName\":\"\"}")
    @PostMapping("/showTokenPrice")
    public CommonResult<TokenPriceHistoryDTO> showTokenPrice(@RequestBody Map<String, String> paramMap) {
        String tokenName = paramMap.get("tokenName").toUpperCase();
        if (StringUtils.isEmpty(tokenName)) {
            CustomException.response(Error.REQUEST_PARAM_INVALID);
        }
        //查询过去24小时的代币价格，这里是utc时间
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);

        DateFormat df = new SimpleDateFormat(DateUtil.DATE_TIME_PATTERN);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timeUtc = df.format(cal.getTime());
        List<TokenPriceHistory> tpList = tokenPriceHistoryService.findByTokenAndTimeUtc(tokenName, timeUtc);

//        List<TradePoint> tradePoints =  flatMoneyService.getAggTrades(tokenName,
//                cal.getTimeInMillis(),
//                System.currentTimeMillis(),
//                Money.USD.getCode());
//        List<TokenPriceHistory> tpList = tradePoints.stream().map(
//                tradePoint -> TradePointToTokenPrice(tradePoint, "FeiXiaoHao", tokenName))
//                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tpList)) {
            CustomException.response(Error.NO_DATA);
        }
        List<Map<String, Object>> dataList = new ArrayList<>(tpList.size());
        for (TokenPriceHistory tp : tpList) {
            Map<String, Object> map = new HashMap<>();
            map.put("tokenPrice", tp.getTokenPrice());
//            map.put("timeUtc", tp.getTimeUtc().getTime());
            map.put("timeUtc", tp.getTimeUtc().getTime() + 28800000L);//加上8个小时
            dataList.add(map);
        }
        CommonResult<TokenPriceHistoryDTO> result = new CommonResult<>();
        TokenPriceHistoryDTO tphd = new TokenPriceHistoryDTO();
        tphd.setTokenName(tokenName);
        tphd.setCurrentTokenPrice(flatMoneyService.tokenPriceAdaptation(tokenName));
        tphd.setHistoryData(dataList);
        //涨跌幅=(现价-上一个交易日收盘价)/上一个交易日收盘价*100%
        tphd.setIncrease("+0.20%");
        tphd.setTradeMarket(tpList.get(0).getTradeMarket());
        result.setData(tphd);
        return result;
    }


    /**
     * @param tradePoint
     * @param tradeMarket
     * @return
     */
    private TokenPriceHistory TradePointToTokenPrice(TradePoint tradePoint, String tradeMarket, String tokenName) {
        TokenPriceHistory tokenPriceHistory = new TokenPriceHistory();
        tokenPriceHistory.setTradeMarket(tradeMarket);
        tokenPriceHistory.setTokenPrice(Double.valueOf(tradePoint.getPrice()));
        tokenPriceHistory.setTimeUtc(new Date(tradePoint.getTradeTime()));
        tokenPriceHistory.setTokenFrom(tokenName);
        return tokenPriceHistory;
    }
}
