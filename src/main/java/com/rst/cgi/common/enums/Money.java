package com.rst.cgi.common.enums;

import com.rst.cgi.conf.security.CurrentThreadData;

public enum Money {
    CNY ("CNY", "人民币", "RMB", "¥"),
    USD ("USD", "美元", "dollar", "$");
//    EUR ("€"),
//    HKD ("HK$"),
//    JPY ("¥"),
//    SGD ("S.$"),
//    CAD ("Can.$"),
//    GBP("￡");





    private final String symblo;
    private final String chineseName;
    private final String engName;
    private final String code;

    Money(String code, String chineseName, String engName, String symblo) {
        this.code = code;
        this.chineseName = chineseName;
        this.engName = engName;
        this.symblo = symblo;
    }

    public static String getSymblo(String shortName) {
        for (Money ms : Money.values()) {
            if (ms.toString().equalsIgnoreCase(shortName)) {
                return ms.symblo;
            }
        }
        return "";
    }

    public static Money getMoney(String shortName) {
        for (Money ms : Money.values()) {
            if (ms.toString().equalsIgnoreCase(shortName)) {
                return ms;
            }
        }
        return null;
    }

    public static Boolean isSupport(String shortName) {
        for (Money ms : Money.values()) {
            if (ms.toString().equalsIgnoreCase(shortName)) {
                return true;
            }
        }
        return false;
    }

    public String getSymblo() {
        return symblo;
    }

    public String getName() {
        Integer languageType =  CurrentThreadData.language();
        if (languageType == 0) {
            return chineseName;
        } else if (languageType == 1) {
            return engName;
        } else {
           return "";

        }
    }

    public String getCode() {
        return code;
    }
}
