package com.rst.cgi.common.enums;

import java.lang.annotation.*;

/**
 * 用于实体类的字段，标注该注解的字段不会映射表的列字段
 * 即标注该注解的字段新增修改时忽略
 * @author huangxiaolin
 * @date 2017-12-29 下午5:15
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ignore {
}
