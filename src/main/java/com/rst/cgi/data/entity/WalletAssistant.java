package com.rst.cgi.data.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletAssistant {
    private String typeCn;
    private String typeEn;
    private int typeSort;
    private String questionCn;
    private String questionEn;
    private int msgSort;
    private Integer questionId;
    private String keyWord;
}
