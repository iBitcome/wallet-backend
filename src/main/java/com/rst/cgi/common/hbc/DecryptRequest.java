package com.rst.cgi.common.hbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hujia on 2016/12/15.
 * 注解的方法会先解密http body,再做数据参数的校验绑定
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DecryptRequest {
    String value() default "aes";
}
