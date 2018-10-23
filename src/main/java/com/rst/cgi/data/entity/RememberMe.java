package com.rst.cgi.data.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 *
 * @author hujia
 * @date 2017/6/15
 */
@Data
@Document(collection = "remember_me")
public class RememberMe {
    @Id
    @Field("_id")
    private Long userId;
    private String token;
    private Long rememberTime;
}
