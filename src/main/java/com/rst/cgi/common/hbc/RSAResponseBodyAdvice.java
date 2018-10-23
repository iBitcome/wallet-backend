package com.rst.cgi.common.hbc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rst.cgi.common.utils.AESUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hujia on 2016/12/15.
 */
@ControllerAdvice(basePackages = "com.rst.cgi.controller")
public class RSAResponseBodyAdvice extends BaseResponseBodyAdvice {

    private final Logger logger = LoggerFactory.getLogger(RSAResponseBodyAdvice.class);


    @PostConstruct
    void init() {
        type = "rsa";
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class aClass,
                                  ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {
        if (o == null) {
            return o;
        }
        Object ret = o;
        if (methodParameter.getMethod().isAnnotationPresent(EncryptResponse.class)) {
            Map<String, Object> map = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            try {
                String value = mapper.writeValueAsString(ret);
                HttpServletRequest request = ((ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes()).getRequest();
                String aesKey = (String)request.getAttribute("aes-random-key");
                map.put("sData", Base64.encodeBase64String(AESUtil.aesEncrypt(value.getBytes(), aesKey)));
                map.put("success", true);
            } catch (JsonProcessingException e) {
                map.put("success", false);
                map.put("msg", "secret error");
            }
            ret = map;
        }
        return ret;
    }
}
