package com.rst.cgi.common.hbc;

import com.rst.cgi.common.utils.AESUtil;
import com.rst.cgi.common.utils.RSAUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sun.misc.BASE64Decoder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.PrivateKey;
import java.util.Arrays;

/**
 * Created by hujia on 2016/12/15.
 */
@ControllerAdvice(basePackages = "com.rst.cgi.controller")
public class RSARequestBodyAdvice extends BaseRequestBodyAdvice {
    private PrivateKey PRIVATE_RSA_KEY = null;
    private final Logger logger = LoggerFactory.getLogger(RSAResponseBodyAdvice.class);

    @PostConstruct
    void init() {
        type = "rsa";
        PRIVATE_RSA_KEY = RSAUtil.getRstPrivateKey();
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage,
                                           MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

        return new HttpInputMessage() {
            @Override
            public InputStream getBody() throws IOException {
                String bodyString = StreamUtils.copyToString(inputMessage.getBody(), DEFAULT_CHARSET);
                logger.info(bodyString);
                String[] bodyData = bodyString.split("&");
                if (bodyData.length == 3) {
                    byte[] enData = new BASE64Decoder().decodeBuffer(bodyData[0]);
                    byte[] enKey = new BASE64Decoder().decodeBuffer(bodyData[1]);
                    byte[] sign = new BASE64Decoder().decodeBuffer(bodyData[2]);
                    byte[] aesKey = RSAUtil.decryptData(enKey, PRIVATE_RSA_KEY);
                    byte[] data = AESUtil.aesDecrypt(enData, aesKey);
                    byte[] cmpSign = DigestUtils.sha1(data);
                    if (Arrays.equals(cmpSign, sign)) {
                        HttpServletRequest request = ((ServletRequestAttributes)
                                RequestContextHolder.getRequestAttributes()).getRequest();
                        request.setAttribute("aes-random-key", new String(aesKey));
                        return new ByteArrayInputStream(data);
                    } else {
                        logger.info("签名与参数不符合");
                    }
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
