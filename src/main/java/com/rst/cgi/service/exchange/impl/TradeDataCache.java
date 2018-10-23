package com.rst.cgi.service.exchange.impl;

import com.rst.cgi.data.dao.mongo.TradePointDao;
import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.entity.TradePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


/**
 * @author hujia
 */
@Component
@Scope("prototype")
public class TradeDataCache {
    private final Logger logger = LoggerFactory.getLogger(TradeDataCache.class);
    public interface TradeDataMaker {
        /**
         * 产生一段时间内的symbol的行情数据
         * @param symbol 交易对
         * @param startTime 起始时间戳ms
         * @param endTime 结束时间戳ms
         * @return 产生的行情数据列表
         */
        List<TradePoint> fetchTrades(Symbol symbol, Long startTime, Long endTime);
    }

    private static final String  REDIS_KEY_SEPARATOR = ".";
    private static final long MILL_SECOND_OF_ONE_HOUR = 1000 * 60 * 60;
    private static final long MIN_MILL_SECOND_TO_MAKE = 1000 * 10;
    private static final long MEM_CACHED_MILL_SECOND = 1000 * 60 * 8;
    private String marketName;

    private String keyForMinTSOfTradeInDb;
    private String keyForMaxTSOfTradeInDb;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private TradePointDao tradePointDao;
    private TradeDataMaker tradeMaker;
    private Set<Symbol> symbols;
    private Set<Symbol> usedSymbols;

    /** 缓存最近的行情数据在内存 */
    private Map<Symbol, List<TradePoint>> memcachedTrades;
    private Map<Symbol, Long> memcachedTS;

    public void init(String market, Set<Symbol> symbols, TradeDataMaker maker) {
        marketName = market;
        keyForMinTSOfTradeInDb = market + REDIS_KEY_SEPARATOR + "keyForMinTSOfTradeInDb" + REDIS_KEY_SEPARATOR;
        keyForMaxTSOfTradeInDb = market + REDIS_KEY_SEPARATOR + "keyForMaxTSOfTradeInDb" + REDIS_KEY_SEPARATOR;
        tradeMaker = (symbol, startTime, endTime) -> {
            if ((endTime - startTime) > MIN_MILL_SECOND_TO_MAKE) {
                return maker.fetchTrades(symbol, startTime, endTime);
            }

            return new ArrayList<>();
        };
        this.symbols = symbols;

        usedSymbols = new HashSet<>();
        memcachedTrades = new HashMap<>();
        memcachedTS = new HashMap<>();

        //clear db & cache for test
        clear();
    }

    public void clear() {
        clearCache();
    }

    public void clearCache() {
        redisTemplate.delete(redisTemplate.keys(marketName + REDIS_KEY_SEPARATOR + "keyFor" + "*"));
    }

    public void clearDb() {
        symbols.forEach(item -> tradePointDao.clear(marketName, item.name()));
    }

    //每小时
//    @Scheduled(cron = "0 0 */1 * * ?")
    public void makeTradeData() {
        logger.info("========makeTradeData--begin=========");
        usedSymbols.forEach(symbol -> {
            List<TradePoint> tradePoints = tradeMaker.fetchTrades(symbol,
                    maxTimestampInDb(symbol) + 1, System.currentTimeMillis());
            long minTimestampInDb = minTimestampInDb(symbol);
            tradePoints.addAll(tradeMaker.fetchTrades(symbol,
                    minTimestampInDb - MILL_SECOND_OF_ONE_HOUR,
                    minTimestampInDb - 1));

            saveToDb(symbol, tradePoints);

            //控制调用频率
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        logger.info("========makeTradeData--end=========");
    }

//    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void loadTradeDataToMem() {
        long current = System.currentTimeMillis();
        usedSymbols.forEach(symbol -> {
            memcachedTrades.put(
                    symbol, doGetTrades(symbol, current - MEM_CACHED_MILL_SECOND, current));
            memcachedTS.put(symbol, current);
        });
    }

    public List<TradePoint> getTrades(Symbol symbol, Long startTime, Long endTime) {
        List<TradePoint> result = tradesFromMem(symbol, startTime, endTime);

        if (result == null) {
            result = doGetTrades(symbol, startTime, endTime);
        }

        return result;
    }

    private List<TradePoint> doGetTrades(Symbol symbol, Long startTime, Long endTime) {
        usedSymbols.add(symbol);
        long minTimestampInDb = minTimestampInDb(symbol);
        long maxTimestampInDb = maxTimestampInDb(symbol);

        if (maxTimestampInDb <= 0) {
            List<TradePoint> tradePoints = tradeMaker.fetchTrades(symbol, startTime, endTime);
            logger.info("tradeMaker ValueSize:{}, start:{}, end:{}", tradePoints.size(), startTime, endTime);
            asyncSaveToDb(symbol, tradePoints);
            return tradePoints;
        }

        List<TradePoint> tradePoints = new ArrayList<>();

        long end = Math.min(minTimestampInDb - 1, endTime);
        tradePoints.addAll(tradeMaker.fetchTrades(symbol, startTime, end));
        logger.info("tradeMaker ValueSize:{}, start:{}, end:{}", tradePoints.size(), startTime, end);

        //保证缓存的时间上的连续性
        if (end >= minTimestampInDb - 1) {
            asyncSaveToDb(symbol, tradePoints);
        }

        long start = Math.max(startTime, minTimestampInDb);
        end = Math.min(maxTimestampInDb, endTime);
        if (end > start) {
            tradePoints.addAll(tradesFromDb(symbol, start, end));
        }

        start = maxTimestampInDb;
        end = endTime;

        if (end > start) {
            List<TradePoint> additional = tradeMaker.fetchTrades(symbol, maxTimestampInDb, endTime);
            logger.info("tradeMaker ValueSize:{}, start:{}, end:{}", additional.size(), start, end);

            asyncSaveToDb(symbol, additional);
            tradePoints.addAll(additional.stream()
                                         .filter(tradePoint -> tradePoint.getTradeTime() > startTime)
                                         .collect(Collectors.toList()));
        }
        return tradePoints;
    }

    private List<TradePoint> tradesFromMem(Symbol symbol, Long startTime, Long endTime) {
        Long ts = memcachedTS.get(symbol);

        if (ts != null && startTime > ts - MEM_CACHED_MILL_SECOND) {
            List<TradePoint> tradePoints = memcachedTrades.get(symbol);
            if (tradePoints != null && !tradePoints.isEmpty()) {
                return tradePoints.stream()
                                  .filter(tradePoint -> tradePoint.getTradeTime() >= startTime
                                          && tradePoint.getTradeTime() < endTime)
                                  .collect(Collectors.toList());
            }
        }

        return null;
    }

    public List<TradePoint> tradesFromDb(Symbol symbol, Long startTime, Long endTime) {
        if (startTime > endTime) {
            return new ArrayList<>();
        }

        return tradePointDao.find(marketName, symbol.name(), startTime, endTime);
    }

    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private void asyncSaveToDb(final Symbol symbol, final List<TradePoint> tradePoints) {
        if (tradePoints == null || tradePoints.isEmpty()) {
            return;
        }

        if (dbExecutor.isShutdown()) {
            dbExecutor = Executors.newSingleThreadExecutor();
        }

        logger.info("asyncSaveToDb:{}", tradePoints.size());

        dbExecutor.submit(() -> saveToDb(symbol, tradePoints));
    }

    private void saveToDb(Symbol symbol, List<TradePoint> tradePoints) {
        if (tradePoints == null || tradePoints.isEmpty()) {
            return;
        }

        logger.info("saveToDb:{}", tradePoints.size());
        tradePointDao.save(marketName, symbol.name(), tradePoints);

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

        long min = minTimestampInDb(symbol);
        long max = maxTimestampInDb(symbol);

        if (minTimestamp < min || min == 0) {
            redisTemplate.opsForValue().set(keyForMinTSOfTradeInDb + symbol.name(), minTimestamp + "");
        }

        if (maxTimestamp > max || max == 0) {
            redisTemplate.opsForValue().set(keyForMaxTSOfTradeInDb + symbol.name(), maxTimestamp + "");
        }

        logger.info("saveToDb min:{}, max:{}, ValueSize:{}, from:{}, to:{}",
                min, max, tradePoints.size(), minTimestamp, maxTimestamp);

    }

    private long minTimestampInDb(Symbol symbol) {
        return timestampInDb(true, symbol);
    }

    private long maxTimestampInDb(Symbol symbol) {
        return timestampInDb(false, symbol);
    }

    private long timestampInDb(boolean min, Symbol symbol) {
        String key = min ? keyForMinTSOfTradeInDb + symbol.name() : keyForMaxTSOfTradeInDb + symbol.name();

        long timestamp = 0;
        String value = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(value) || value.length() < 13) {
            TradePoint tradePoint = min ? tradePointDao.findFirst(marketName, symbol.name())
                    : tradePointDao.findLast(marketName, symbol.name());
            if (tradePoint != null) {
                timestamp = tradePoint.getTradeTime();
                redisTemplate.opsForValue().set(key, timestamp + "");
            }
        } else {
            timestamp = Long.parseLong(value);
        }

        return timestamp;
    }

}
