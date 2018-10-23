package com.rst.cgi.data.dto.request;

import lombok.Data;

/**
 * @author hujia
 */
@Data
public class ForgetPwdReq {
    private String email;
    private String code;
    private String password;
}
