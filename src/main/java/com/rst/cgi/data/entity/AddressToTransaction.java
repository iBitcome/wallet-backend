package com.rst.cgi.data.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author hujia
 */
@Data
@Document(collection = "address_to_transaction")
public class AddressToTransaction {
    @Id
    @Field("_id")
    private ObjectId id;
    private String address;
    private Boolean rollOut;
    private String txId;
    private Integer status;
    private Long blockTime;
    private Long pendingTime;
    private Long confirmedTime;
    private String token;
}
