package com.rst.cgi.data.entity;


import com.rst.cgi.common.utils.StringUtil;

/**
 * Created by hujia on 2017/6/28.
 */
public interface Entity {
    default String table() {
        return StringUtil.camelToUnderline(getClass().getSimpleName());
    }
}
