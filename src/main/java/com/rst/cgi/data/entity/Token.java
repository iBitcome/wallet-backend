package com.rst.cgi.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by mtb on 2018/4/12.
 */
@Document(collection = "tokens")
@Getter
@Setter
public class Token implements Entity {
    @Id
    @Field("_id")
    private ObjectId objectId;
    private String name;
    private String address;
    @Field("checksum_address")
    private String checksumAddress;
    private Integer decimal;
    private String fullName;
    private Double order;
    @Field("coin_type")
    private Integer coinType;
    private String chainName;
    @Field("owner_token_id")
    private Long ownerTokenId;
    private Integer version;
    private Long tokenCode;
    private Long aliasCode;
    private Integer appId;
}
