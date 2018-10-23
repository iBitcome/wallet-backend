package com.rst.cgi.data.dao.mongo;
import com.rst.cgi.data.entity.RememberMe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author hujia
 * @date 2017/6/15
 */
@Repository
public interface RememberMeRepository extends MongoRepository<RememberMe, Long> {
    /**
     * 根据token获取用户信息
     * @param token
     * @return
     */
    RememberMe findByToken(String token);
}
