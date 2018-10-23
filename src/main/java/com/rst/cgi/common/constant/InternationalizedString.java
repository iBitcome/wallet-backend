package com.rst.cgi.common.constant;

import com.rst.cgi.conf.security.CurrentThreadData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hujia
 */
public class InternationalizedString {

//    @Configuration
//    public static class Config {
//        public static Integer language;
//
//        @Value("${server.language:0}")
//        public void setDataServer(Integer str) {
//            language = str;
//        }
//    }


    public static final Integer CHS = 0;
    public static final Integer ENG = 1;

    public Map<Integer, String> typeToMsg = new HashMap<>();

    public void put(Integer type, String message) {
        typeToMsg.put(type, message);
    }

    public String get() {
        Integer languageType = CurrentThreadData.language();
        return typeToMsg.get(languageType);
    }

    public static InternationalizedString make(String chs, String eng) {
        InternationalizedString msg = new InternationalizedString();
        msg.put(CHS, chs);
        msg.put(ENG, eng);
        return msg;
    }
}
