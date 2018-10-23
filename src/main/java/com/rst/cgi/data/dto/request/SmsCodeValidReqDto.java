package com.rst.cgi.data.dto.request;

import lombok.Data;

@Data
public class SmsCodeValidReqDto {
    private String phone;
    private String inviticode;
}
