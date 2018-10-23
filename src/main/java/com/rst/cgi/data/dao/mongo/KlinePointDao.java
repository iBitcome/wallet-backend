package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.KlinePoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Component
public class KlinePointDao {
    public static final String COLLECTION_NAME_MID = "_kline_point_";
    @Autowired
    private MongoTemplate mongoTemplate;

    public void clear(String exchange, String symbol, String kInterval) {
        mongoTemplate.dropCollection(collectionFrom(exchange, symbol, kInterval));
    }

    public void save(String exchange, String symbol, String kInterval, Set<KlinePoint> klinePoints) {
        List<BatchUpdateDao.BathUpdateOptions> updateList =
                klinePoints.stream()
                           .map(klinePoint -> {
                               BatchUpdateDao.BathUpdateOptions updateOptions = new BatchUpdateDao.BathUpdateOptions();
                               updateOptions.setQuery(Query.query(Criteria.where("_id").is(klinePoint.getTimestamp())));
                               updateOptions.setUpdate(Update
                                       .update("_id", klinePoint.getTimestamp())
                                       .set("open", klinePoint.getOpen())
                                       .set("high", klinePoint.getHigh())
                                       .set("low", klinePoint.getLow())
                                       .set("close", klinePoint.getClose())
                                       .set("volume",klinePoint.getVolume()));
                               updateOptions.setUpsert(true);
                               updateOptions.setMulti(false);
                               return updateOptions;
                           }).collect(Collectors.toList());

        String collectionName = collectionFrom(exchange, symbol, kInterval);
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
        }

        BatchUpdateDao.bathUpdate(mongoTemplate, collectionName, updateList);
    }

    public void save(String exchange, String symbol, String kInterval, KlinePoint klinePoint) {
        String collectionName = collectionFrom(exchange, symbol, kInterval);
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
            mongoTemplate.getCollection(collectionName).createIndex("_id");
        }

        mongoTemplate.save(klinePoint, collectionName);
    }

    public List<KlinePoint> find(String exchange, String symbol, String kInterval, long begin, long end) {
        return mongoTemplate.find(
                new Query(new Criteria().andOperator(
                        Criteria.where("_id").gte(begin), Criteria.where("_id").lte(end))),
                KlinePoint.class,
                collectionFrom(exchange, symbol, kInterval));
    }

    public KlinePoint findLast(String exchange, String symbol, String kInterval) {
        return mongoTemplate.findOne(
                new Query().with(new Sort(Sort.Direction.DESC, "_id")).limit(1),
                KlinePoint.class,
                collectionFrom(exchange, symbol, kInterval));
    }

    public KlinePoint findFirst(String exchange, String symbol, String kInterval) {
        return mongoTemplate.findOne(
                new Query().with(new Sort(Sort.Direction.ASC, "_id")).limit(1),
                KlinePoint.class,
                collectionFrom(exchange, symbol, kInterval));
    }

    private String collectionFrom(String exchange, String symbol, String kInterval) {
        return exchange + COLLECTION_NAME_MID + symbol + "." + kInterval;
    }
}
