package com.rst.cgi.common.hbc;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.service.EncryptDecryptService;
import com.sun.mail.util.BASE64DecoderStream;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Created by hujia on 2017/6/21.
 */
@ControllerAdvice(basePackages = "com.rst.cgi.controller")
@ConditionalOnProperty(name = "aes.enable", havingValue = "true", matchIfMissing = true)
public class AESRequestBodyAdvice extends BaseRequestBodyAdvice {
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private final Logger logger = LoggerFactory.getLogger(AESRequestBodyAdvice.class);

    @Autowired
    private EncryptDecryptService encryptDecryptService;

    @PostConstruct
    void init() {
        type = "aes";
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage,
                                           MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

        return new HttpInputMessage() {

            @Override
            public InputStream getBody() throws IOException {
                byte[] bodyBytes = null;
                String keyIndex = null;
                try {
                    bodyBytes = StreamUtils.copyToByteArray(
                            new BASE64DecoderStream(inputMessage.getBody()));

                    keyIndex = inputMessage.getHeaders().getFirst(
                            EncryptDecryptService.KEY_INDEX_HEADER);


                    byte[] data = encryptDecryptService.decrypt(bodyBytes, keyIndex);
                    return new ByteArrayInputStream(data);

                } catch (Exception e) {
                    logger.error("【解析错误】bodyBytes:{}, keyIndex:{}, method:{}",
                            bodyBytes == null ? "null" :new String(bodyBytes),
                            keyIndex,
                            parameter.getMethod().toString());
                    CustomException.response(Error.ERR_MSG_KEY_ERROR);
                }

                return inputMessage.getBody();
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        };
    }
}
