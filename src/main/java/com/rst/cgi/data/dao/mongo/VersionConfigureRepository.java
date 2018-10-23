package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.VersionConfigure;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VersionConfigureRepository extends MongoRepository<VersionConfigure, String> {
    VersionConfigure findByPlatform(String plateform);
}
