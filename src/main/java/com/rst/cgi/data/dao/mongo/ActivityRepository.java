package com.rst.cgi.data.dao.mongo;

import com.rst.cgi.data.entity.ActivityInfo;
import com.rst.cgi.data.entity.AddressTxReady;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author mtb
 */
public interface ActivityRepository extends MongoRepository<ActivityInfo, ObjectId> {

    ActivityInfo findByAddressAndActivityName(String address, String activityName);
}
