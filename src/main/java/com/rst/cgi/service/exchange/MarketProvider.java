package com.rst.cgi.service.exchange;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface MarketProvider {
    @AliasFor(annotation = Service.class)
    String value() default "";
    String market() default "";

//    String packageName = "com.bitmain.eds.service.impl";
    String packageName = "com.rst.cgi.service.exchange.impl";
}
