package com.rst.cgi.service.impl;

import com.google.gson.Gson;
import com.rst.cgi.common.utils.OkHttpUtil;
import com.rst.cgi.conf.ExchangeConfig;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dto.DepthPoint;
import com.rst.cgi.data.dto.DexTopMarket;
import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.dto.SymbolBrief;
import com.rst.cgi.data.dto.response.GetSymbolDepthRes;
import com.rst.cgi.data.entity.KlinePoint;
import com.rst.cgi.data.entity.TradePoint;
import com.rst.cgi.service.MarketCacheService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hujia
 */
@Service("DexDotTopFetcher")
public class DexDotTopFetcher implements MarketCacheService.DataFetcher {
    private final Logger logger = LoggerFactory.getLogger(DexDotTopFetcher.class);
    private final static String SYMBOL_SEPARATOR = "_";
    private final static int RECENT_TRADE_MAX_COUNT = 200;
    private final static int RECENT_DEPTH_MAX_COUNT = 100;

    private final static String BASE_TOKEN = "ETH";

    @Override
    public List<TradePoint> fetchTradePoints(Symbol symbol, String exchange,
                                             Long startTime, Long endTime) {
        CustomException.response("Dex.Top暂时不支持按时间段拉取分时交易数据");

        return null;
    }

    @Override
    public List<TradePoint> fetchTradePoints(Symbol symbol, String exchange, int size) {
        int fetchSize = size;
        if (fetchSize == 0) {
            fetchSize = RECENT_TRADE_MAX_COUNT;
        }

        JSONObject result = JSONObject.fromObject(
                OkHttpUtil.http(ExchangeConfig.DEX_TOP.getApiHost() + "tradehistory")
                          .path(symbol.exchangeName(SYMBOL_SEPARATOR))
                          .path("" + fetchSize)
                          .get());

        if (!result.containsKey("records")) {
            logger.info("fetchTradePoints failed:{}", result);
            return null;
        }

        List<TradePoint> tradePoints = new ArrayList<>();
        JSONArray points = result.getJSONArray("records");
        for (int i = 0; i < points.size(); i++) {
            JSONObject point = points.getJSONObject(i);
            int type = "Sell".equals(point.getString("action")) ? 1 : 0;
            tradePoints.add(new TradePoint(
                    point.getLong("timeMs"),
                    point.getString("price"),
                    point.getString("amount"),
                    type));
        }

        return tradePoints;
    }

    @Override
    public List<KlinePoint> fetchKlinePoints(Symbol symbol, String exchange, String kInterval,
                                             Long startTime, Long endTime) {
        //小于一个跨度就不走fetch了，一般拉到一个重复数据
        if (endTime - startTime < KlinePoint.millSecondsOf(kInterval)) {
            return new ArrayList<>();
        }

        JSONObject result = JSONObject.fromObject(
                OkHttpUtil.http(ExchangeConfig.DEX_TOP.getApiHost() + "kline/history")
                          .param("symbol", symbol.exchangeName(SYMBOL_SEPARATOR))
                          .param("resolution", resolutionFrom(kInterval))
                          .param("from", startTime/1000)
                          .param("to", endTime/1000)
                          .get());

        if (result.containsKey("s") && "ok".equals(result.getString("s"))) {
            JSONArray t = result.getJSONArray("t");
            JSONArray c = result.getJSONArray("c");
            JSONArray o = result.getJSONArray("o");
            JSONArray h = result.getJSONArray("h");
            JSONArray l = result.getJSONArray("l");
            JSONArray v = result.getJSONArray("v");

            List<KlinePoint> klinePoints = new ArrayList<>();
            for (int i = 0; i < t.size(); i++) {
                KlinePoint klinePoint = new KlinePoint();
                klinePoint.setTimestamp(t.getLong(i) * 1000);
                klinePoint.setClose(c.getString(i));
                klinePoint.setOpen(o.getString(i));
                klinePoint.setHigh(h.getString(i));
                klinePoint.setLow(l.getString(i));
                klinePoint.setVolume(v.getString(i));
                klinePoints.add(klinePoint);
            }
            return klinePoints;
        } else {
            logger.info("fetchKlinePoints failed:{}", result);
        }

        return new ArrayList<>();
    }

    @Override
    public GetSymbolDepthRes fetchDepthPoints(Symbol symbol, String exchange, int limit) {
        int fetchSize = limit;
        if (fetchSize == 0) {
            fetchSize = RECENT_DEPTH_MAX_COUNT;
        }

        String api = ExchangeConfig.DEX_TOP.getApiHost() + "depth/" +
                symbol.exchangeName(SYMBOL_SEPARATOR) + "/" + fetchSize;

        JSONObject result = JSONObject.fromObject(
                OkHttpUtil.http(ExchangeConfig.DEX_TOP.getApiHost() + "depth")
                          .path(symbol.exchangeName(SYMBOL_SEPARATOR))
                          .path("" + fetchSize)
                          .get());

        if (result.containsKey("depth")) {
            GetSymbolDepthRes res = new GetSymbolDepthRes();
            res.setSymbol(symbol);
            res.setTimestamp(result.getJSONObject("depth").getLong("timeMs"));
            JSONArray asks = result.getJSONObject("depth").getJSONArray("asks");
            JSONArray bids = result.getJSONObject("depth").getJSONArray("bids");
            List<DepthPoint> askPoints = new ArrayList<>();
            List<DepthPoint> bidPoints = new ArrayList<>();
            for (int i = 0; i < asks.size(); i++) {
                askPoints.add(new DepthPoint(asks.getJSONObject(i).getString("price"),
                        asks.getJSONObject(i).getString("amount")));
            }

            for (int i = 0; i < bids.size(); i++) {
                bidPoints.add(new DepthPoint(bids.getJSONObject(i).getString("price"),
                        bids.getJSONObject(i).getString("amount")));
            }

            res.setAsks(askPoints);
            res.setBids(bidPoints);
            return res;
        } else {
            logger.info("fetchDepthPoints failed:{}", result);
        }

        return null;
    }

    private DexTopMarket dexTopMarket = null;
    private Map<String, DexTopMarket.DexToken> tokenMap;

    @Scheduled(fixedDelay = 15 * 60 *1000)
    public void updateDexTopMarket() {
        dexTopMarket = new Gson().fromJson(
                OkHttpUtil.http(ExchangeConfig.DEX_TOP.getApiHost()
                        + "market").get(), DexTopMarket.class);

        if (dexTopMarket == null) {
            return;
        }

          tokenMap = new HashMap<>();
//        dexTopMarket.getConfig().getCashTokens().forEach(
//                dexToken -> tokenMap.put(dexToken.getTokenId(), dexToken));
//        dexTopMarket.getConfig().getStockTokens().forEach(
//                dexToken -> tokenMap.put(dexToken.getTokenId(), dexToken));
//        dexTopMarket.getConfig().getDisabledTokens().forEach(
//                dexToken -> tokenMap.put(dexToken.getTokenId(), dexToken));
        List<DexTopMarket.DexToken> cashTokens = dexTopMarket.getConfig().getCashTokens();
        List<DexTopMarket.DexToken> disabledTokens = dexTopMarket.getConfig().getDisabledTokens();
        List<DexTopMarket.DexToken> stockTokens = dexTopMarket.getConfig().getStockTokens();
        if(cashTokens !=null && cashTokens.size() != 0){
            cashTokens.forEach(dexToken -> tokenMap.put(dexToken.getTokenId(), dexToken));
        }
        if(disabledTokens !=null && disabledTokens.size() != 0){
            disabledTokens.forEach(dexToken -> tokenMap.put(dexToken.getTokenId(), dexToken));
        }
        if(stockTokens !=null && stockTokens.size() != 0){
            stockTokens.forEach(dexToken -> tokenMap.put(dexToken.getTokenId(), dexToken));
        }
    }

    @Override
    public List<SymbolBrief> fetchSymbolData(String exchange) {
        if (dexTopMarket == null) {
            updateDexTopMarket();
        }

        List<SymbolBrief> symbolBriefs = new ArrayList<>();
        dexTopMarket.getConfig()
                    .getCashTokens()
                    .stream()
                    .map(item -> item.getTokenId()).forEach(token -> {
                        JSONObject result = JSONObject.fromObject(
                                OkHttpUtil.http(ExchangeConfig.DEX_TOP.getApiHost() + "pairlist")
                                          .path(token)
                                          .get());

                        if (result.containsKey("pairs")) {
                            JSONArray pairs = result.getJSONArray("pairs");

                            for (int i = 0; i < pairs.size(); i++) {
                                JSONObject symbolPair = pairs.getJSONObject(i);
                                Symbol symbol = Symbol.from(symbolPair.getString("pairId"), "_");
                                //<cashId>(16) <stockId>(16)
                                int cashTokenCode = tokenMap.get(symbol.getBaseAsset()).getTokenCode();
                                int stockTokenCode = tokenMap.get(symbol.getQuoteAsset()).getTokenCode();
                                long symbolId = ((cashTokenCode & 0xFFFF) << 16) | (stockTokenCode & 0xFFFF);

                                SymbolBrief symbolBrief = new SymbolBrief();
                                symbolBrief.setSymbol(symbol);
                                symbolBrief.setSymbolId(symbolId);
                                symbolBrief.setOpenPrice(symbolPair.getString("openPrice"));
                                symbolBrief.setLow24(symbolPair.getString("low24"));
                                symbolBrief.setHigh24(symbolPair.getString("high24"));
                                symbolBrief.setChangePercent24(symbolPair.getString("changePercent24"));
                                symbolBrief.setChange24(symbolPair.getString("change24"));
                                symbolBrief.setVolume24(symbolPair.getString("volume24"));
                                symbolBrief.setLastPrice(symbolPair.getString("lastPrice"));
                                symbolBriefs.add(symbolBrief);
                            }
                        } else {
                            logger.info("fetchSymbolData failed:{}", result);
                        }
                    });

        return symbolBriefs;
    }

    @Override
    public Object fetchConfig(String exchange) {
        if (dexTopMarket == null) {
            return dexTopMarket;
        }

        return dexTopMarket;
    }

    private String resolutionFrom(String kInterval) {
        if (kInterval.equals(KlinePoint.FIVE_MINUTES)) {
            return "5";
        } else if (kInterval.equals(KlinePoint.FIFTEEN_MINUTES)) {
            return "15";
        } else if (kInterval.equals(KlinePoint.HALF_HOURLY)) {
            return "30";
        } else if (kInterval.equals(KlinePoint.HOURLY)) {
            return "60";
        } else if (kInterval.equals(KlinePoint.DAILY)) {
            return "D";
        } else if (kInterval.equals(KlinePoint.WEEKLY)) {
            return "W";
        }

        return "";
    }
}
