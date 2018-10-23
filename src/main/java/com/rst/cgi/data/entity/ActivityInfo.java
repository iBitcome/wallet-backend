package com.rst.cgi.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = "activity_info")
@Setter
@Getter
public class ActivityInfo {
    @Id
    @Field("_id")
    private ObjectId id;
    private String TokenName;
    private Long tokenCode;
    private String address;
    private Long createTime;
    private String activityName;
}
