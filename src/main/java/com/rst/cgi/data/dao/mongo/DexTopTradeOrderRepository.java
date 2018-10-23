package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.PlaceOrderRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author matianbao
 */
public interface DexTopTradeOrderRepository extends MongoRepository<PlaceOrderRecord, Integer> {

}
