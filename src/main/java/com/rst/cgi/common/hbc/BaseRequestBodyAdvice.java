package com.rst.cgi.common.hbc;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Created by hujia on 2017/6/21.
 */
public class BaseRequestBodyAdvice extends RequestBodyAdviceAdapter {
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    protected String type = "";

    @Override
    public boolean supports(MethodParameter methodParameter,
                            Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (methodParameter.getMethod().isAnnotationPresent(DecryptRequest.class)) {
            DecryptRequest decryptRequest = methodParameter.getMethod().getAnnotation(DecryptRequest.class);
            if (decryptRequest.value().equalsIgnoreCase(type)) {
                return true;
            }
        }

        return false;
    }
}
