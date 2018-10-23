package com.rst.cgi.data.dao.mysql.sql;

import java.lang.annotation.*;

/**
 * Created by hujia on 2017/3/14.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = Encodes.class)
public @interface Encode {
    String value();
}
