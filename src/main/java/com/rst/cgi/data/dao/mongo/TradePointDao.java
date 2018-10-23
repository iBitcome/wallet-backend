package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.TradePoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Component
public class TradePointDao {
    public static final String COLLECTION_NAME_MID = "_trade_point_";
    @Autowired
    private MongoTemplate mongoTemplate;

    public void clear(String exchange, String symbol) {
        mongoTemplate.dropCollection(collectionFrom(exchange, symbol));
    }

    public void save(String exchange, String symbol, List<TradePoint> tradePoints) {
        List<BatchUpdateDao.BathUpdateOptions> updateList =
                tradePoints.stream()
                           .map(tradePoint -> {
                               BatchUpdateDao.BathUpdateOptions updateOptions = new BatchUpdateDao.BathUpdateOptions();
                               updateOptions.setQuery(Query.query(Criteria.where("_id").is(tradePoint.getTradeTime())));
                               updateOptions.setUpdate(Update
                                       .update("_id", tradePoint.getTradeTime())
                                       .set("price", tradePoint.getPrice())
                                       .set("quantity", tradePoint.getQuantity())
                                       .set("type", tradePoint.getType()));
                               updateOptions.setUpsert(true);
                               updateOptions.setMulti(false);
                               return updateOptions;
                           }).collect(Collectors.toList());

        String collectionName = collectionFrom(exchange, symbol);
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
        }

        BatchUpdateDao.bathUpdate(mongoTemplate, collectionName, updateList);
    }

    public void save(String exchange, String symbol, TradePoint tradePoint) {
        String collectionName = collectionFrom(exchange, symbol);
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
            mongoTemplate.getCollection(collectionName).createIndex("_id");
        }

        tradePoint.setTradeTime(tradePoint.getTradeTime());

        mongoTemplate.save(tradePoint, collectionName);
    }

    public List<TradePoint> find(String exchange, String symbol, long begin, long end) {
        return mongoTemplate.find(
                new Query(new Criteria().andOperator(
                        Criteria.where("_id").gte(begin), Criteria.where("_id").lte(end))),
                TradePoint.class,
                collectionFrom(exchange, symbol));
    }

    public TradePoint findLast(String exchange, String symbol) {
        return mongoTemplate.findOne(
                new Query().with(new Sort(Sort.Direction.DESC, "_id")).limit(1),
                TradePoint.class,
                collectionFrom(exchange, symbol));
    }

    public TradePoint findFirst(String exchange, String symbol) {
        return mongoTemplate.findOne(
                new Query().with(new Sort(Sort.Direction.ASC, "_id")).limit(1),
                TradePoint.class,
                collectionFrom(exchange, symbol));
    }

    private String collectionFrom(String exchange, String symbol) {
        return exchange + COLLECTION_NAME_MID + symbol;
    }
}

