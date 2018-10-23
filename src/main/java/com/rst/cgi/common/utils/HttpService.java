package com.rst.cgi.common.utils;

import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.service.SpecialAuthService;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by hujia on 2017/2/28.
 */
@Service
public class HttpService {

    private final Logger logger = LoggerFactory.getLogger(HttpService.class);
    /**
     * httpPost
     *
     * @param url       路径
     * @param jsonParam 参数
     * @return
     */
    public JSONObject httpPostWithJson(String url, JSONObject jsonParam) {
        return httpPost(url, buildEntityWithJson(jsonParam));
    }

    public JSONObject httpPost(String url, Map<String, Object> params) {
        return httpPost(url, buildEntityWithParam(params));
    }

    public HttpEntity buildEntityWithParam(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        List<NameValuePair> pairList = new ArrayList<>(params.size());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry
                        .getValue().toString());
                pairList.add(pair);
            }
        }

        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList, "utf-8");
            return entity;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpEntity buildEntityWithJson(JSONObject jsonParam) {
        if (jsonParam == null) {
            return null;
        }

        StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json;charset=UTF-8");
        return entity;
    }

    /**
     * post请求
     *
     * @param url        url地址
     * @param httpEntity 参数
     * @return
     */
    public JSONObject httpPost(String url, HttpEntity httpEntity) {
        if (url.startsWith("http://")) {
            return post(HttpClients.createDefault(), url, httpEntity);
        } else if (url.startsWith("https://")) {
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(createSSLConnSocketFactory())
                    .build();
            return post(httpClient, url, httpEntity);
        }

        return buildCommonError(new JSONObject(), -1, "your url must start with http:// or https://");
    }

    private JSONObject post(HttpClient client, String url, HttpEntity httpEntity) {
        JSONObject ret = new JSONObject();
        HttpPost method = new HttpPost(url);
        boolean otherError = false;
        String otherErrorMsg = null;

        try {
            if (null != httpEntity) {
                method.setEntity(httpEntity);
                JSONObject param = JSONObject.fromObject(EntityUtils.toString(httpEntity));
                if (param.containsKey("contentType")) {
                    method.addHeader("content-type",param.getString("contentType") +
                    ";charset=UTF-8");
                }
            }

            method.addHeader("x-inner-token", SpecialAuthService.magicToken);

            HttpResponse result = client.execute(method);

            url = URLDecoder.decode(url, "UTF-8");

            /**读取服务器返回过来的json字符串数据**/
            String str = EntityUtils.toString(result.getEntity());
            /**把json字符串转换成json对象**/
            ret = JSONObject.fromObject(str);


            if (result.getStatusLine().getStatusCode() != 200) {
                buildCommonError(ret, -1001, result.getStatusLine().toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            buildCommonError(ret, -1, "post请求提交失败(" + url + ")" + "[" + e.toString() + "]");
        }

        return ret;
    }


    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public JSONObject httpGet(String url, Map<String, Object> param) {
        logger.info("httpGet url:{},param:{}", url, param);
        //get请求返回结果
        JSONObject ret = new JSONObject();
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            if (Objects.nonNull(param)) {
                for (Map.Entry<String, Object> entry : param.entrySet()) {
                    url = url + "&" + entry.getKey() + "=" + entry.getValue();
                }
                url = url.replaceFirst("&", "?");
            }
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            /**读取服务器返回过来的json字符串数据**/
            String str = EntityUtils.toString(response.getEntity());
            /**把json字符串转换成json对象**/
            ret = JSONObject.fromObject(str);
            if (response.getStatusLine().getStatusCode() != 200) {
                buildCommonError(ret, -1001, response.getStatusLine().toString());
            }
        } catch (Exception e) {
            buildCommonError(ret, -1, "get请求提交失败(" + url + ")" + "[" + e.toString() + "]");
        }
        return ret;
    }

    private JSONObject buildCommonError(final JSONObject result,
                                        final int code, final String errorMsg) {
        result.put("msg", errorMsg);
        result.put("code", code);
        result.put("success", false);
        return result;
    }

    /**
     * 创建SSL安全连接
     *
     * @return
     */
    private SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();

            sslsf = new SSLConnectionSocketFactory(sslContext, (s, sslSession) -> true);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
    }

    /**
     * 发送post json请求
     * @author huangxiaolin
     * @date 2018-04-27 18:12
     * @param param 请求参数
     * @return 返回http请求成功响应的json数据格式，如果请求失败则抛出CustomException异常
     */
    public JSONObject postJSON(String url, Map<String, Object> param) {
        return postJSON(url, null, param);
    }

    /**
     * 发送post json请求
     * @author huangxiaolin
     * @date 2018-04-27 18:12
     * @param headMap 请求头
     * @param param 请求参数
     * @return 返回http请求成功响应的json数据格式，如果请求失败则抛出CustomException异常
     */
    public JSONObject postJSON(String url, Map<String, String> headMap, Map<String, Object> param) {
        JSONObject jsonObject = postJSONForResult(url, headMap, param, false);
        if (jsonObject.getInt(Constant.CODE_KEY) == Constant.SUCCESS_CODE) {
            return jsonObject.getJSONObject(Constant.MESSAGE_KEY);
        } else {
            CustomException.response(Error.NO_DATA);
        }
        return null;
    }

    /**
     * 发送post json请求，通过该方法可以实现在http请求失败情况下的逻辑处理
     * @author huangxiaolin
     * @date 2018-05-08 17:51
     * @param headMap 请求头
     * @param param 请求参数
     * @param ignoreResponseLog 是否日志记录响应信息
     * @return 这里返回的JSONObject并不是http请求成功响应的json数据，而是包装了json数据的JSONObject
     */
    public JSONObject postJSONForResult(String url, Map<String, String> headMap, Map<String, Object> param, boolean ignoreResponseLog) {
        String jsonParam = JSONObject.fromObject(param).toString();
        //logger.info("Http post url:{}, param:{}", url, jsonParam);
        StringEntity entity = new StringEntity(jsonParam, Constant.CHARSET_UTF8);
        entity.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);//设置json请求头
        HttpPost post = new HttpPost(url);
        if (entity != null) {
            post.setEntity(entity);
        }
        setRequestHeader(post, headMap);
        return getResponse(url, post, ignoreResponseLog);
    }

    /**
     * 发送http get请求
     * @author huangxiaolin
     * @date 2018-04-25 14:47
     */
    public JSONObject doGet(String url, Map<String, String> param) {
        return doGet(url, null, param);
    }

    /**
     * 发送http get请求
     * @author huangxiaolin
     * @date 2018-05-08 16:53
     */
    public JSONObject doGet(String url, Map<String, String> headMap, Map<String, String> param) {
        JSONObject jsonObject = doGetForResult(url, headMap, param, false);
        if (jsonObject.getInt(Constant.CODE_KEY) == Constant.SUCCESS_CODE) {
            return jsonObject.getJSONObject(Constant.MESSAGE_KEY);
        } else {
            CustomException.response(Error.NO_DATA);
        }
        return null;
    }

    /**
     * @author hxl
     * 2018/5/24 下午8:32
     */
    public JSONObject doGetForResult(String url, Map<String, String> param) {
        return doGetForResult(url, null, param, false);
    }

        /**
         * 发送http get请求
         * @author huangxiaolin
         * @date 2018-04-20 15:25
         * @param headMap 请求头
         * @param param 请求参数
         * @param ignoreResponseLog 是否日志记录响应信息
         */
    public JSONObject doGetForResult(String url, Map<String, String> headMap, Map<String, String> param, boolean ignoreResponseLog) {
        //logger.info("Http url:{},param:{}", url, param);
        //没有请求参数
        if (!CollectionUtils.isEmpty(param)) {
            StringBuilder urlStr = new StringBuilder(url);
            urlStr.append("?");
            try {
                //拼接参数
                for (Map.Entry<String, String> entry : param.entrySet()) {
                    urlStr.append(entry.getKey())
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(), Constant.CHARSET_UTF8))
                            .append("&");
                }
                url = urlStr.substring(0, urlStr.length()-1);
            } catch (UnsupportedEncodingException e) {
                logger.error("{}", e);
            }
        }
        //logger.info("Http Get url:{}", url);
        HttpGet get = new HttpGet(url);
        setRequestHeader(get, headMap);
        return getResponse(url, get, ignoreResponseLog);
    }

    /**
     * 获取http响应信息
     * @author huangxiaolin
     * @date 2018-04-27 18:08
     * @return 这里返回的JSONObject并不是http请求成功响应的json数据，而是包装了json数据的JSONObject
     */
    private JSONObject getResponse(String url, HttpUriRequest request, boolean ignoreResponseLog) {
        HttpResponse response = null;
        HttpClient httpClient = getHttpClient(url);
        JSONObject jsonObject = new JSONObject();
        try {
            response = httpClient.execute(request);
            String result = EntityUtils.toString(response.getEntity(), Constant.CHARSET_UTF8);
            if (response.getStatusLine().getStatusCode() == 200) {
                //if (!ignoreResponseLog) {
                    //logger.info("Http请求成功，响应数据：{}", result);
                //}
                jsonObject.put(Constant.CODE_KEY, Constant.SUCCESS_CODE);//标示响应成功
                jsonObject.put(Constant.MESSAGE_KEY, result);//返回的响应内容放到msg解析
            } else {
                logger.error("Http请求失败，状态行：{}，响应数据：{}", response.getStatusLine().toString(), result);
                jsonObject.put(Constant.CODE_KEY, 400);
                jsonObject.put(Constant.MESSAGE_KEY, result);
            }
        } catch (IOException e) {
            logger.error("Http请求错误：{}", e);
            jsonObject.put(Constant.CODE_KEY, Constant.ERROR_CODE);
            jsonObject.put(Constant.MESSAGE_KEY, e.toString());
        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpClient);
        }
        return jsonObject;
    }

    /**
     * 设置Http请求头地址
     * @author huangxiaolin
     * @date 2018-01-05 10:12
     */
    private void setRequestHeader(HttpRequestBase request, Map<String, String> headMap) {
        if (headMap == null || headMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : headMap.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取http客户端对象
     * @author huangxiaolin
     * @date 2018-04-20 15:17
     */
    private HttpClient getHttpClient(String url) {
        CloseableHttpClient httpClient = null;
        //设置超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(1000).setConnectTimeout(3000)
                .setSocketTimeout(30000).build();
        if (url.startsWith("http://")) {
            httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        } else if (url.startsWith("https://")) {
            httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .setSSLSocketFactory(createSSLConnSocketFactory())
                    .build();
        } else {
            throw new IllegalArgumentException("http请求协议不匹配");
        }
        return httpClient;
    }

}
