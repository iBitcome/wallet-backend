package com.rst.cgi.common.hbc;

import com.google.gson.GsonBuilder;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.service.EncryptDecryptService;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hujia on 2017/6/21.
 */
@ControllerAdvice(basePackages = "com.rst.cgi.controller")
@ConditionalOnProperty(name = "aes.enable", havingValue = "true", matchIfMissing = true)
public class AESResponseBodyAdvice extends BaseResponseBodyAdvice {

    @Autowired
    private EncryptDecryptService encryptDecryptService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private final Logger logger = LoggerFactory.getLogger(AESResponseBodyAdvice.class);

    @PostConstruct
    void init() {
        type = "aes";
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
            String value = new GsonBuilder().serializeNulls().create().toJson(o);
            logger.info("request headers:{}", JSONObject.fromObject(serverHttpRequest.getHeaders()));
            String keyIndex = serverHttpRequest.getHeaders().getFirst(EncryptDecryptService.KEY_INDEX_HEADER);
            if (StringUtils.isBlank(keyIndex) || !stringRedisTemplate.hasKey(keyIndex)) {
                CustomException.response(Error.ERR_MSG_KEY_ERROR);
            }

            map.put("sData", Base64.encodeBase64String(encryptDecryptService.encrypt(value.getBytes(), keyIndex)));
            map.put("success", true);
            ret = map;
        }

        return ret;
    }
}
