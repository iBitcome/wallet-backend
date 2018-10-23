package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.AddressTxReady;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author hujia
 */
public interface ATRRepository extends MongoRepository<AddressTxReady, String> {
    /**
     * 查找指定的索引数据
     * @param atrList
     * @return
     */
    List<AddressTxReady> findAllByAtrIn(List<String> atrList);
}
