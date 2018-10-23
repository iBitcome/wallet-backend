package com.rst.cgi.service.impl;

import com.alibaba.fastjson.JSONObject;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.utils.OkHttpUtil;
import com.rst.cgi.conf.ExchangeConfig;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mongo.KlinePointDao;
import com.rst.cgi.data.dao.mongo.TradePointDao;
import com.rst.cgi.data.dto.DepthPoint;
import com.rst.cgi.data.dto.DexTopMarket;
import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.dto.SymbolBrief;
import com.rst.cgi.data.dto.response.GetKlineRes;
import com.rst.cgi.data.dto.response.GetSymbolDepthRes;
import com.rst.cgi.data.dto.response.GetTradesRes;
import com.rst.cgi.data.entity.KlinePoint;
import com.rst.cgi.data.entity.TradePoint;
import com.rst.cgi.service.MarketCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Service
public class MarketCacheServiceImpl implements MarketCacheService {
    private final Logger logger = LoggerFactory.getLogger(MarketCacheServiceImpl.class);

    private static final int UPDATE_TRADE_INTERVAL = 60 * 1000;
    private static final int UPDATE_DEPTH_INTERVAL = 60 * 1000;
    private static final int UPDATE_SYMBOL_INTERVAL = 60 * 1000;

    private static final int MAX_REFRESH_INTERVAL = 10 * 1000;

    private static final String REDIS_KEY_SEPARATOR = "_";
    private static final String TRADE_MIN_TS_KEY_PREFIX = "tradeTsMin" + REDIS_KEY_SEPARATOR;
    private static final String TRADE_MAX_TS_KEY_PREFIX = "tradeTsMax" + REDIS_KEY_SEPARATOR;
    private static final String KLINE_MIN_TS_KEY_PREFIX = "klineTsMin" + REDIS_KEY_SEPARATOR;
    private static final String KLINE_MAX_TS_KEY_PREFIX = "klineTsMax" + REDIS_KEY_SEPARATOR;
    private static final String DEPTH_KEY_PREFIX = REDIS_KEY_SEPARATOR + "depthPrefix" + REDIS_KEY_SEPARATOR;
    private static final String TRADE_KEY_PREFIX = "tradePrefix" + REDIS_KEY_SEPARATOR;
    private static final String SYMBOL_KEY_PREFIX = "symbolBrief" + REDIS_KEY_SEPARATOR;

    /** 缓存存储器 **/
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private KlinePointDao klinePointDao;
    @Autowired
    private TradePointDao tradePointDao;
    /** 原始数据获取 **/
    private DataFetcher dataFetcher;

    @PostConstruct
    public void init() {
        this.dataFetcher = new CompositeDataFetcher();

        //clear redis cache
        stringRedisTemplate.delete(stringRedisTemplate.keys(KLINE_MAX_TS_KEY_PREFIX+"*"));
        stringRedisTemplate.delete(stringRedisTemplate.keys(KLINE_MIN_TS_KEY_PREFIX+"*"));
        stringRedisTemplate.delete(stringRedisTemplate.keys(TRADE_MAX_TS_KEY_PREFIX+"*"));
        stringRedisTemplate.delete(stringRedisTemplate.keys(TRADE_MIN_TS_KEY_PREFIX+"*"));
        stringRedisTemplate.delete(stringRedisTemplate.keys(SYMBOL_KEY_PREFIX+"*"));
        stringRedisTemplate.delete(stringRedisTemplate.keys("*"+DEPTH_KEY_PREFIX+"*"));
        stringRedisTemplate.delete(stringRedisTemplate.keys(TRADE_KEY_PREFIX+"*"));


    }

    @Override
    public GetTradesRes getTradePoints(Symbol symbol, String exchange,
                                       Long startTime, Long endTime) {
        long minTimestampInDb = minTsForTrade(symbol, exchange);
        long maxTimestampInDb = maxTsForTrade(symbol, exchange);

        GetTradesRes res = new GetTradesRes();
        res.setSymbol(symbol);

        if (maxTimestampInDb <= 0) {
            List<TradePoint> tradePoints = dataFetcher.fetchTradePoints(symbol, exchange, startTime, endTime);
            logger.info("dataFetcher ValueSize:{}, start:{}, end:{}", tradePoints.size(), startTime, endTime);
            asyncSaveToDb(exchange, symbol, tradePoints);

            res.setTradePoints(TradePoint.toAggTrades(tradePoints, TimeUnit.MILLISECONDS));
            return res;
        }

        List<TradePoint> tradePoints = new ArrayList<>();

        long end = Math.min(minTimestampInDb - 1, endTime);
        tradePoints.addAll(dataFetcher.fetchTradePoints(symbol, exchange, startTime, end));
        logger.info("dataFetcher ValueSize:{}, start:{}, end:{}", tradePoints.size(), startTime, end);

        //保证缓存的时间上的连续性
        if (end >= minTimestampInDb - 1) {
            asyncSaveToDb(exchange, symbol, tradePoints);
        }

        long start = Math.max(startTime, minTimestampInDb);
        end = Math.min(maxTimestampInDb, endTime);
        if (end > start) {
            tradePoints.addAll(tradePointsFromDb(exchange, symbol, start, end));
        }

        start = maxTimestampInDb;
        end = endTime;

        if (end > start) {
            List<TradePoint> additional = dataFetcher.fetchTradePoints(
                    symbol, exchange, maxTimestampInDb, endTime);
            logger.info("dataFetcher ValueSize:{}, start:{}, end:{}", additional.size(), start, end);

            asyncSaveToDb(exchange, symbol, tradePoints);

            tradePoints.addAll(additional.stream()
                                         .filter(tradePoint -> tradePoint.getTradeTime() > startTime)
                                         .collect(Collectors.toList()));
        }

        res.setTradePoints(TradePoint.toAggTrades(tradePoints, TimeUnit.MILLISECONDS));
        return res;
    }

    @Override
    public GetTradesRes getTradePoints(Symbol symbol, String exchange) {
        String redisKey = TRADE_KEY_PREFIX + exchange + REDIS_KEY_SEPARATOR + symbol.name();

        String tsStr = stringRedisTemplate.opsForValue().get("ts" + REDIS_KEY_SEPARATOR + redisKey);

        boolean forceUpdate = true;
        if (!StringUtils.isEmpty(tsStr)) {
            long ts = Long.parseLong(tsStr);
            if (System.currentTimeMillis() - ts < MAX_REFRESH_INTERVAL
                    && System.currentTimeMillis() > ts) {
                forceUpdate = false;
            }
        }

        List<TradePoint> tradePoints;
        if (forceUpdate) {
            tradePoints = updateTradePoints(symbol, exchange);
        } else {
            long redisSize = stringRedisTemplate.opsForList().size(redisKey);
            tradePoints = stringRedisTemplate.opsForList()
                                             .range(redisKey, 0 ,redisSize)
                                             .stream()
                                             .map(item -> TradePoint.fromRedisString(item))
                                             .collect(Collectors.toList());
        }

        tradePoints = tradePoints.stream()
                  .sorted((l, r) -> Long.compare(r.getTradeTime(), l.getTradeTime()))
                  .collect(Collectors.toList());


        return new GetTradesRes(symbol, tradePoints);
    }

    @Override
    public GetKlineRes getKlinePoints(Symbol symbol, String exchange,
                                      String kInterval, Long startTime, Long endTime) {
        long minTimestampInDb = minTsForKline(symbol, exchange, kInterval);
        long maxTimestampInDb = maxTsForKline(symbol, exchange, kInterval);

        Set<KlinePoint> klinePoints = new HashSet<>();

        do {
            if (maxTimestampInDb <= 0) {
                klinePoints.addAll(dataFetcher.fetchKlinePoints(
                        symbol, exchange, kInterval, startTime, endTime));

                logger.info("dataFetcher ValueSize:{}, start:{}, end:{}", klinePoints.size(), startTime, endTime);
                asyncSaveToDb(exchange, symbol, kInterval, klinePoints);
                break;
            }

            long end = Math.min(minTimestampInDb - 1, endTime);
            klinePoints.addAll(dataFetcher.fetchKlinePoints(symbol, exchange, kInterval, startTime, end));
            logger.info("dataFetcher ValueSize:{}, start:{}, end:{}", klinePoints.size(), startTime, end);

            //保证缓存的时间上的连续性
            if (end >= minTimestampInDb - 1) {
                asyncSaveToDb(exchange, symbol, kInterval, klinePoints);
            }

            long start = Math.max(startTime, minTimestampInDb);
            end = Math.min(maxTimestampInDb, endTime);
            if (end > start) {
                klinePoints.addAll(klinePointsFromDb(exchange, symbol, kInterval, start, end));
            }

            start = maxTimestampInDb;
            end = endTime;

            if (end > start) {
                List<KlinePoint> additional = dataFetcher.fetchKlinePoints(
                        symbol, exchange, kInterval, maxTimestampInDb, endTime);
                logger.info("dataFetcher ValueSize:{}, start:{}, end:{}", additional.size(), start, end);

                asyncSaveToDb(exchange, symbol, kInterval, additional.stream().collect(Collectors.toSet()));
                klinePoints.addAll(additional.stream()
                        .filter(klinePoint -> klinePoint.getTimestamp() > startTime)
                        .collect(Collectors.toList()));
            }
        } while (false);

        if (klinePoints == null || klinePoints.isEmpty()) {
            CustomException.response(Error.NO_DATA);
        }

        return new GetKlineRes(symbol, klinePoints);
    }

    @Override
    public GetSymbolDepthRes getDepthPoints(Symbol symbol, String exchange) {
        String keySuffix = DEPTH_KEY_PREFIX + exchange
                + REDIS_KEY_SEPARATOR + symbol.name() + REDIS_KEY_SEPARATOR;


        String tsStr = stringRedisTemplate.opsForValue().get("ts" + keySuffix);

        boolean forceUpdate = true;
        long ts = 0;
        if (!StringUtils.isEmpty(tsStr)) {
            ts = Long.parseLong(tsStr);
            if (System.currentTimeMillis() - ts < MAX_REFRESH_INTERVAL
                    && System.currentTimeMillis() > ts) {
                forceUpdate = false;
            }
        }

        if (forceUpdate) {
            GetSymbolDepthRes depthRes = dataFetcher.fetchDepthPoints(symbol, exchange, 0);
            saveToCache(exchange, depthRes);

            if (depthRes == null) {
                CustomException.response(Error.NO_DATA);
            }

            return depthRes;
        }

        long askSize = stringRedisTemplate.opsForList().size("asks" + keySuffix);
        long bidSize = stringRedisTemplate.opsForList().size("bids" + keySuffix);

        List<DepthPoint> asks = stringRedisTemplate.opsForList()
                                  .range("asks" + keySuffix, 0, askSize)
                                  .stream()
                                  .map(item -> DepthPoint.fromRedisString(item))
                                  .collect(Collectors.toList());

        List<DepthPoint> bids = stringRedisTemplate.opsForList()
                                                   .range("bids" + keySuffix, 0, bidSize)
                                                   .stream()
                                                   .map(item -> DepthPoint.fromRedisString(item))
                                                   .collect(Collectors.toList());

        return new GetSymbolDepthRes(symbol, ts, asks, bids);
    }

    @Override
    public List<SymbolBrief> getSymbolData(String exchange, String baseAsset, String quoteAsset) {
        String redisKey = SYMBOL_KEY_PREFIX + exchange;

        Long redisSize = stringRedisTemplate.opsForList().size(redisKey);

        List<SymbolBrief> symbolBriefs;
        if (redisSize == 0 || redisSize == null) {
            symbolBriefs = dataFetcher.fetchSymbolData(exchange);

            if (symbolBriefs != null && !symbolBriefs.isEmpty()) {
                JSONObject jsonObject=JSONObject.parseObject(OkHttpUtil.http(ExchangeConfig.DEX_TOP.getApiHost() + "tradingconfig").get());
                JSONObject tokens=jsonObject.getJSONObject("tradingConfig").getJSONObject("tokens");
                symbolBriefs.forEach( symbolBrief -> {
                    JSONObject object=tokens.getJSONObject(symbolBrief.getSymbol().getBaseAsset());
                    if(object!=null){
                        symbolBrief.setMinPlaceOrderValue(object.getDoubleValue("minPlaceOrderValue"));
                    }
                });
                stringRedisTemplate.delete(redisKey);
                stringRedisTemplate.opsForList().leftPushAll(redisKey,
                        symbolBriefs.stream().map(symbolBrief -> symbolBrief.toRedisString())
                                   .collect(Collectors.toList()));
            }

        } else {
            symbolBriefs = stringRedisTemplate.opsForList()
                                              .range(redisKey, 0, redisSize)
                                              .stream()
                                              .map(item -> SymbolBrief.fromRedisString(item))
                                              .collect(Collectors.toList());
            JSONObject jsonObject=JSONObject.parseObject(OkHttpUtil.http(ExchangeConfig.DEX_TOP.getApiHost() + "tradingconfig").get());
            JSONObject tokens=jsonObject.getJSONObject("tradingConfig").getJSONObject("tokens");
            symbolBriefs.forEach( symbolBrief -> {
                JSONObject object=tokens.getJSONObject(symbolBrief.getSymbol().getBaseAsset());
                if(object!=null){
                    symbolBrief.setMinPlaceOrderValue(object.getDoubleValue("minPlaceOrderValue"));
                }
            });

        }

        return symbolBriefs.stream()
                           .filter(symbolBrief -> baseAsset == null
                                   || baseAsset.equalsIgnoreCase(symbolBrief.getSymbol().getBaseAsset()))
                           .filter(symbolBrief -> quoteAsset == null
                                   || quoteAsset.equalsIgnoreCase(symbolBrief.getSymbol().getQuoteAsset()))
                           .collect(Collectors.toList());
    }

    @Override
    public Object getConfig(String exchange) {
        JSONObject jsonObject=JSONObject.parseObject(OkHttpUtil.http(ExchangeConfig.DEX_TOP.getApiHost() + "tradingconfig").get());
        JSONObject tokens=jsonObject.getJSONObject("tradingConfig").getJSONObject("tokens");
        DexTopMarket market = (DexTopMarket)dataFetcher.fetchConfig(exchange);
        List<DexTopMarket.DexToken> cashTokens = market.getConfig().getCashTokens();
        List<DexTopMarket.DexToken> disabledTokens = market.getConfig().getDisabledTokens();
        List<DexTopMarket.DexToken> stockTokens = market.getConfig().getStockTokens();
        if(cashTokens !=null && cashTokens.size() != 0){
            cashTokens.forEach( cashtoken -> {
                String tokenId=cashtoken.getTokenId();
                JSONObject object = tokens.getJSONObject(tokenId);
                if(object!=null){
                    cashtoken.setMinDepositAmount(object.getDoubleValue("minDepositAmount"));
                    cashtoken.setMinWithdrawAmount(object.getDoubleValue("minWithdrawAmount"));
                }
            });
        }
        if(stockTokens !=null && stockTokens.size() != 0){
            stockTokens.forEach( cashtoken -> {
                String tokenId=cashtoken.getTokenId();
                JSONObject object = tokens.getJSONObject(tokenId);
                if(object!=null){
                    cashtoken.setMinDepositAmount(object.getDoubleValue("minDepositAmount"));
                    cashtoken.setMinWithdrawAmount(object.getDoubleValue("minWithdrawAmount"));
                }
            });
        }

        if(disabledTokens !=null && disabledTokens.size() != 0){
           disabledTokens.forEach( cashtoken -> {
                String tokenId=cashtoken.getTokenId();
                JSONObject object = tokens.getJSONObject(tokenId);
                if(object!=null){
                    cashtoken.setMinDepositAmount(object.getDoubleValue("minDepositAmount"));
                    cashtoken.setMinWithdrawAmount(object.getDoubleValue("minWithdrawAmount"));
                }
            });
        }

        return market;
    }

    @Scheduled(fixedDelay = UPDATE_SYMBOL_INTERVAL)
    public void updateSymbolData() {
        Set<String> keys = stringRedisTemplate.keys(SYMBOL_KEY_PREFIX + "*");

        keys.forEach(key -> {
            String[] values = key.split(REDIS_KEY_SEPARATOR);
            List<SymbolBrief> symbolBriefs = dataFetcher.fetchSymbolData(values[1]);

            if (symbolBriefs != null && !symbolBriefs.isEmpty()) {
                stringRedisTemplate.delete(key);
                stringRedisTemplate.opsForList().leftPushAll(key,
                        symbolBriefs.stream().map(symbolBrief -> symbolBrief.toRedisString())
                                    .collect(Collectors.toList()));
            }
        });
    }

    @Scheduled(fixedDelay = UPDATE_DEPTH_INTERVAL)
    public void updateDepthPoints() {
        Set<String> keys = stringRedisTemplate.keys("ts" + DEPTH_KEY_PREFIX + "*");

        keys.forEach(key -> {
            String[] values = key.split(REDIS_KEY_SEPARATOR);
            GetSymbolDepthRes depthRes = dataFetcher.fetchDepthPoints(
                    Symbol.from(values[3]), values[2], 0);
            saveToCache(values[2], depthRes);
        });
    }

    @Scheduled(fixedDelay = UPDATE_TRADE_INTERVAL)
    public void updateTradePoints() {
        Set<String> keys = stringRedisTemplate.keys(TRADE_KEY_PREFIX + "*");

        keys.forEach(key -> {
            String[] values = key.split(REDIS_KEY_SEPARATOR);
            updateTradePoints(Symbol.from(values[2]), values[1]);
        });
    }

    private List<TradePoint> updateTradePoints(Symbol symbol, String exchange) {
        List<TradePoint> tradePoints = dataFetcher.fetchTradePoints(symbol, exchange, 0);

        if (tradePoints != null && !tradePoints.isEmpty()) {
            //排序
            tradePoints = tradePoints.stream()
                                     .sorted((l, r) -> Long.compare(r.getTradeTime(), l.getTradeTime()))
                                     .collect(Collectors.toList());

            String redisKey = TRADE_KEY_PREFIX + exchange + REDIS_KEY_SEPARATOR + symbol.name();
            stringRedisTemplate.delete(redisKey);
            stringRedisTemplate.opsForList().leftPushAll(redisKey,
                    tradePoints.stream()
                               .map(tradePoint -> tradePoint.toRedisString())
                               .collect(Collectors.toList()));
            stringRedisTemplate.opsForValue().set("ts" + REDIS_KEY_SEPARATOR + redisKey,
                    System.currentTimeMillis() + "");
        }

        return tradePoints;
    }

    private void saveToCache(String exchange, GetSymbolDepthRes depthRes) {
        if (depthRes == null) {
            return;
        }

        String keySuffix = DEPTH_KEY_PREFIX + exchange
                + REDIS_KEY_SEPARATOR + depthRes.getSymbol().name() + REDIS_KEY_SEPARATOR;

        if (depthRes != null
                && !depthRes.getAsks().isEmpty()
                && !depthRes.getBids().isEmpty()) {
            stringRedisTemplate.delete("asks" + keySuffix);
            stringRedisTemplate.delete("bids" + keySuffix);
            stringRedisTemplate.delete("ts" + keySuffix);
            stringRedisTemplate.opsForList().leftPushAll("asks" + keySuffix,
                    depthRes.getAsks().stream().map(depthPoint -> depthPoint.toRedisString())
                            .collect(Collectors.toList()));
            stringRedisTemplate.opsForList().leftPushAll("bids" + keySuffix,
                    depthRes.getBids().stream().map(depthPoint -> depthPoint.toRedisString())
                            .collect(Collectors.toList()));
            stringRedisTemplate.opsForValue().set("ts" + keySuffix, "" + depthRes.getTimestamp());
        }
    }

    private void setMinTsForTrade(Symbol symbol, String exchange, Long minTs) {
        stringRedisTemplate.opsForValue().set(
                TRADE_MIN_TS_KEY_PREFIX + exchange + REDIS_KEY_SEPARATOR + symbol.name(),
                minTs + "");
    }

    private void setMaxTsForTrade(Symbol symbol, String exchange, Long maxTs) {
        stringRedisTemplate.opsForValue().set(
                TRADE_MAX_TS_KEY_PREFIX + exchange + REDIS_KEY_SEPARATOR + symbol.name(),
                maxTs + "");
    }

    private void setMinTsForKline(Symbol symbol, String exchange, String kInterval, Long minTs) {
        String redisKey = KLINE_MIN_TS_KEY_PREFIX + exchange
                + REDIS_KEY_SEPARATOR + symbol.name() + REDIS_KEY_SEPARATOR + kInterval;
        stringRedisTemplate.opsForValue().set(redisKey, minTs + "");
    }

    private void setMaxTsForKline(Symbol symbol, String exchange, String kInterval, Long maxTs) {
        String redisKey = KLINE_MAX_TS_KEY_PREFIX + exchange
                + REDIS_KEY_SEPARATOR + symbol.name() + REDIS_KEY_SEPARATOR + kInterval;
        stringRedisTemplate.opsForValue().set(redisKey, maxTs + "");
    }

    private Long minTsForTrade(Symbol symbol, String exchange) {
        String redisKey = TRADE_MIN_TS_KEY_PREFIX + exchange + REDIS_KEY_SEPARATOR + symbol.name();
        String tsStr = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(tsStr)) {
            return Long.parseLong(tsStr);
        }

        TradePoint tradePoint = tradePointDao.findFirst(exchange, symbol.name());
        if (tradePoint != null) {
            stringRedisTemplate.opsForValue().set(redisKey, tradePoint.getTradeTime() + "");
            return tradePoint.getTradeTime();
        }

        return 0L;
    }

    private Long maxTsForTrade(Symbol symbol, String exchange) {
        String redisKey = TRADE_MAX_TS_KEY_PREFIX + exchange + REDIS_KEY_SEPARATOR + symbol.name();
        String tsStr = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(tsStr)) {
            return Long.parseLong(tsStr);
        }

        TradePoint tradePoint = tradePointDao.findLast(exchange, symbol.name());
        if (tradePoint != null) {
            stringRedisTemplate.opsForValue().set(redisKey, tradePoint.getTradeTime() + "");
            return tradePoint.getTradeTime();
        }

        return 0L;
    }

    private Long minTsForKline(Symbol symbol, String exchange, String kInterval) {
        String redisKey = TRADE_MIN_TS_KEY_PREFIX + exchange
                + REDIS_KEY_SEPARATOR + symbol.name() + REDIS_KEY_SEPARATOR + kInterval;
        String tsStr = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(tsStr)) {
            return Long.parseLong(tsStr);
        }

        KlinePoint klinePoint = klinePointDao.findFirst(exchange, symbol.name(), kInterval);
        if (klinePoint != null) {
            stringRedisTemplate.opsForValue().set(redisKey, klinePoint.getTimestamp() + "");
            return klinePoint.getTimestamp();
        }

        return 0L;
    }

    private Long maxTsForKline(Symbol symbol, String exchange, String kInterval) {
        String redisKey = TRADE_MAX_TS_KEY_PREFIX + exchange
                + REDIS_KEY_SEPARATOR + symbol.name() + REDIS_KEY_SEPARATOR + kInterval;
        String tsStr = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(tsStr)) {
            return Long.parseLong(tsStr);
        }

        KlinePoint klinePoint = klinePointDao.findLast(exchange, symbol.name(), kInterval);
        if (klinePoint != null) {
            stringRedisTemplate.opsForValue().set(redisKey, klinePoint.getTimestamp() + "");
            return klinePoint.getTimestamp();
        }

        return 0L;
    }

    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private void asyncSaveToDb(String exchange, Symbol symbol, final List<TradePoint> tradePoints) {
        if (tradePoints == null || tradePoints.isEmpty()) {
            return;
        }

        if (dbExecutor.isShutdown()) {
            dbExecutor = Executors.newSingleThreadExecutor();
        }

        logger.info("asyncSaveToDb:{}", tradePoints.size());

        dbExecutor.submit(() -> saveToDb(exchange, symbol, tradePoints));
    }

    private void saveToDb(String exchange, Symbol symbol, List<TradePoint> tradePoints) {
        if (tradePoints == null || tradePoints.isEmpty()) {
            return;
        }

        logger.info("TradePoint saveToDb:{}", tradePoints.size());
        tradePointDao.save(exchange, symbol.name(), TradePoint.toAggTrades(tradePoints, TimeUnit.MILLISECONDS));
        long maxTimestamp = 0;
        long minTimestamp = Long.MAX_VALUE;
        for (TradePoint tradePoint : tradePoints) {
            long timestamp = tradePoint.getTradeTime();
            if (timestamp > maxTimestamp) {
                maxTimestamp = timestamp;
            } else if (timestamp < minTimestamp) {
                minTimestamp = timestamp;
            }
        }

        long min = minTsForTrade(symbol, exchange);
        long max = maxTsForTrade(symbol, exchange);

        if (minTimestamp < min || min == 0) {
            setMinTsForTrade(symbol, exchange, minTimestamp);
        }

        if (maxTimestamp > max || max == 0) {
            setMaxTsForTrade(symbol, exchange, maxTimestamp);
        }

        logger.info("saveToDb min:{}, max:{}, ValueSize:{}, from:{}, to:{}",
                min, max, tradePoints.size(), minTimestamp, maxTimestamp);

    }

    private void asyncSaveToDb(String exchange, Symbol symbol, String kInterval,
                               final Set<KlinePoint> klinePoints) {
        if (klinePoints == null || klinePoints.isEmpty()) {
            return;
        }

        if (dbExecutor.isShutdown()) {
            dbExecutor = Executors.newSingleThreadExecutor();
        }

        logger.info("asyncSaveToDb:{}", klinePoints.size());

        dbExecutor.submit(() -> saveToDb(exchange, symbol, kInterval, klinePoints));
    }

    private void saveToDb(String exchange, Symbol symbol, String kInterval,
                          final Set<KlinePoint> klinePoints) {
        if (klinePoints == null || klinePoints.isEmpty()) {
            return;
        }

        logger.info("klinePoints saveToDb:{}", klinePoints.size());
        klinePointDao.save(exchange, symbol.name(), kInterval, klinePoints);

        long maxTimestamp = 0;
        long minTimestamp = Long.MAX_VALUE;
        for (KlinePoint klinePoint : klinePoints) {
            long timestamp = klinePoint.getTimestamp();
            if (timestamp > maxTimestamp) {
                maxTimestamp = timestamp;
            } else if (timestamp < minTimestamp) {
                minTimestamp = timestamp;
            }
        }

        long min = minTsForKline(symbol, exchange, kInterval);
        long max = maxTsForKline(symbol, exchange, kInterval);

        if (minTimestamp < min || min == 0) {
            setMinTsForTrade(symbol, exchange, minTimestamp);
        }

        if (maxTimestamp > max || max == 0) {
            setMaxTsForTrade(symbol, exchange, maxTimestamp);
        }

        logger.info("saveToDb min:{}, max:{}, ValueSize:{}, from:{}, to:{}",
                min, max, klinePoints.size(), minTimestamp, maxTimestamp);

    }

    public List<TradePoint> tradePointsFromDb(String exchange, Symbol symbol,
                                              Long startTime, Long endTime) {
        if (startTime > endTime) {
            return new ArrayList<>();
        }

        return tradePointDao.find(exchange, symbol.name(), startTime, endTime);
    }

    public List<KlinePoint> klinePointsFromDb(String exchange, Symbol symbol,
                                              String kInterval, Long startTime, Long endTime) {
        if (startTime > endTime) {
            return new ArrayList<>();
        }

        return klinePointDao.find(exchange, symbol.name(), kInterval, startTime, endTime);
    }
}
