package com.rst.cgi.data.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashSet;
import java.util.Set;

/**
 * @author hujia
 */
@Document(collection = "DexTop_user_info")
@Data
public class DexTopUserInfo {
    @Id
    @Field("_id")
    private Integer userId;
    private String account;
    private String password;
    private Set<BindAddress> bindAddresses;
    private Long updateTimestamp;

    @Data
    public static class BindAddress {
        private String token;
        private String address;
    }

    public void addBindAddress(String token, String address) {
        if (bindAddresses == null) {
            bindAddresses = new HashSet<>();
        }

        BindAddress bindAddress = new  BindAddress();
        bindAddress.setAddress(address);
        bindAddress.setToken(token);
        bindAddresses.add(bindAddress);
    }
}
