package com.rst.cgi.data.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类
 * @author huangxiaolin
 * @date 2018-05-14 下午3:12
 */
@Data
public class UserEntity implements Entity, Serializable {

    private static final long serialVersionUID=8568033643008690350L;

    private Integer id;
    //用户邮箱
    private String email;
    //密码
    private String password;
    //登录名
    private String loginName;
    //电话号码
    private String phone;
    //用户昵称
    private String nickName;
    //用户创建时间
    private Date createTime;
    //用户最近一次更新时间
    private Date updateTime;
    //是否删除（1：删除，0：已导入钱包，2，未导入钱包）
    private Integer status = 0;
    //是否冻结（1：冻结中，0：未冻结）
    private Integer isFrozen = 0;
    private String headImgUrl;
    private String channel;
    private Integer hand;
    private Integer fingerPrint;
    private String handWord;
    private Integer handStatus;
    private Integer fingerStatus;
    private Integer handPath;
    //参与活动的钱包地址
    private String activeAddress;
    private Date invitedTime;

    public static final String SYSTEM_CHANNEL = "system";

    @Override
    public String table() {
        return "users";
    }
}
