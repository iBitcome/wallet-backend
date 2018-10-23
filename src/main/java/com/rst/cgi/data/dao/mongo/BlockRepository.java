package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.BlockTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author hujia
 */
public interface BlockRepository extends MongoRepository<BlockTransaction, String> {
    /**
     * 根据发送地址寻找交易记录
     * @param txIdList
     * @return
     */
    List<BlockTransaction> findAllByTxIdIn(List<String> txIdList);

    /**
     * 根据接收地址寻找交易记录
     * @param txId
     * @return
     */
    BlockTransaction findByTxId(String txId);
}
