package com.rst.cgi.common.utils;

import com.google.gson.Gson;
import com.rst.cgi.common.constant.Constant;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author hujia
 */
public class OkHttpUtil {
    private static OkHttpClient httpClient =
            new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();

    private static final Logger logger = LoggerFactory.getLogger(OkHttpUtil.class);

    /**
     * http post json
     * @param url
     * @param headers
     * @param data
     * @param <T>
     * @return
     */
    public static <T> String post(String url, Map<String, String> headers, T data) {
        String content = new Gson().toJson(data);
        logger.info("OkHttpUtil.post, header:{}, url:{}, content:{}", headers, url, content);
        RequestBody requestBody =
                FormBody.create(Constant.MEDIA_TYPE_JSON, content);

        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        if (headers != null && !headers.isEmpty()) {
            builder.headers(Headers.of(headers));
        }

        return executeHttpRequest(builder.build());
    }

    /**
     * http get
     * @param url
     * @param headers
     * @param params
     * @return
     */
    public static String get(String url, Map<String, String> headers, Map<String, Object> params) {
        logger.info("OkHttpUtil.get, header:{}, url:{}, content:{}", headers, url, params);
        Request.Builder builder = new Request.Builder().url(urlFrom(url, params));
        if (headers != null && !headers.isEmpty()) {
            builder.headers(Headers.of(headers));
        }

        return executeHttpRequest(builder.build());
    }

    private static String executeHttpRequest(Request request) {
        try {
            Response response = httpClient.newCall(request).execute();
            String result = response.body().string();
            logger.info("OkHttpUtil:{}, body length:{}", response, result.length());
            logger.debug("OkHttpUtil response body:{}", result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String urlFrom(String url, Map<String, Object> params) {
        return urlFrom(url, buildQueryString(params));
    }

    public static String urlFrom(String url, String queryString) {
        if (StringUtils.isEmpty(queryString)) {
            return url;
        }

        int position = url.indexOf("#");
        if (position != -1) {
            url = url.substring(0, position);
        }

        position = url.indexOf("?");
        if (position == -1) {
            return url + "?" + queryString;
        } else {
            return url + "&" + queryString;
        }
    }

    public static String buildQueryString(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder queryString = new StringBuilder();
        params.forEach((key, value) -> queryString.append(key).append("=").append(value).append("&"));
        queryString.replace(queryString.length() - 1, queryString.length(), "");

        return queryString.toString();
    }

    public static OkHttpUtil http(String url) {
        return new OkHttpUtil(url).header("User-Agent", "ibitcome");
    }

    private OkHttpUtil(String url) {
        this.url = url;
    }

    private Map<String, Object> params;
    private Map<String, String> headers;
    private String url;
    private StringBuilder path;

    public <T> String post(T data) {
        return OkHttpUtil.post(url(), headers, data);
    }

    public String post() {
        return OkHttpUtil.post(url(), headers, params);
    }

    public String get() {
        return OkHttpUtil.get(url(), headers, params);
    }

    public OkHttpUtil header(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            return this;
        }

        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
        return this;
    }

    public OkHttpUtil cookie(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            return this;
        }

        if (headers == null) {
            headers = new HashMap<>();
        }

        String cookie = headers.get("Cookie");
        if (StringUtils.isEmpty(cookie)) {
            cookie = name + "=" + value;
        } else {
            cookie += "; " + name + "=" + value;
        }

        headers.put("Cookie", cookie);
        return this;
    }

    public OkHttpUtil param(String key, Object value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return this;
        }

        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    public OkHttpUtil path(String path) {
        if (StringUtils.isEmpty(path)) {
            return this;
        }

        if (this.path == null) {
            this.path = new StringBuilder();
        }

        this.path.append("/").append(path);
        return this;
    }

    private String url() {
        if (path != null) {
            return url + path.toString();
        }

        return url;
    }
}
