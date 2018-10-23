package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.DexTopUserInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author hujia
 */
public interface DexTopUserInfoRepository extends MongoRepository<DexTopUserInfo, Integer> {
    /**
     * 根据用户id查找配置信息
     * @param userId
     * @return
     */
    DexTopUserInfo findByUserId(int userId);
}
