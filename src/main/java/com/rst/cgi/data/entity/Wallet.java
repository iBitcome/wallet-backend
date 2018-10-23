package com.rst.cgi.data.entity;

import com.rst.cgi.data.dao.mysql.sql.Ignore;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Date;

/**
 * 表的一行对应一个钱包实体
 * @author hujia
 */
@Data
public class Wallet implements Entity {
    private Integer id;
    private Integer owner;
    private String equipmentNo;
    private String publicKey;
    private Date createTime;
    private Date updateTime;
    private Integer keyStatus;
    private Integer type;
    private String name;
    private String desc;
    private String faceId;
    private String createIp;
    private Integer createType;

    public String getName() {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        return new String(Base64.getDecoder().decode(name));
    }

    public void setName(String name) {
        if (StringUtils.isEmpty(name)) {
            this.name = name;
        }
        this.name = Base64.getEncoder().encodeToString(name.getBytes());
    }

    @Ignore
    public static final int AVAILABLE = 1;
    @Ignore
    public static final int UNAVAILABLE = 0;
}