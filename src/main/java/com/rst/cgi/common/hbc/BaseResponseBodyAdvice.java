package com.rst.cgi.common.hbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Created by hujia on 2017/6/21.
 */
public class BaseResponseBodyAdvice implements ResponseBodyAdvice {
    protected final Logger log = LoggerFactory.getLogger(BaseResponseBodyAdvice.class);

    protected String type = "";

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        if (aClass == MappingJackson2HttpMessageConverter.class &&
                methodParameter.getMethod().isAnnotationPresent(EncryptResponse.class)) {
            EncryptResponse encryptResponse = methodParameter.getMethod().getAnnotation(EncryptResponse.class);
            if (encryptResponse.value().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        return body;
    }
}
