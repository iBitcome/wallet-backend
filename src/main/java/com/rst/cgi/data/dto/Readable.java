package com.rst.cgi.data.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by hujia on 2017/7/13.
 */
public class Readable {
    protected transient ThreadLocal<Gson> gson = ThreadLocal.withInitial(
            ()-> new GsonBuilder().serializeNulls().create());

    @Override
    public String toString() {
        return gson.get().toJson(this);
    }
}
