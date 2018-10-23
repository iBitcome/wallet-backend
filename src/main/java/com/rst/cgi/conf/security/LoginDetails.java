package com.rst.cgi.conf.security;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author hujia
 * @date 2017/2/28
 */
@Data
public class LoginDetails<T> implements Serializable {
    private static final long serialVersionUID = -4053993921417119390L;

    public static final int AUTH_TYPE_AUTO = 0;
    public static final int AUTH_TYPE_PWD = 1;
    public static final int AUTH_TYPE_EMAIL_CODE = 2;
    /**access only for inner server*/
    public static final int AUTH_TYPE_USER_ID = 3;

    private T data;

    private int type = AUTH_TYPE_PWD;

    public LoginDetails() {}

    public LoginDetails(int type) {
        this.type = type;
        this.data = null;
    }

    public LoginDetails(int type, T data) {
        this.type = type;
        this.data = data;
    }

    public boolean supportHttp(int type) {
        if (type == AUTH_TYPE_PWD
                || type == AUTH_TYPE_EMAIL_CODE) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "LoginDetails: {type:" + type + ", data:" + data + "}";
    }
}
