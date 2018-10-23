package com.rst.cgi.common.utils;

import com.google.gson.Gson;
import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.service.EncryptDecryptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * web 相关工具类
 * @author huangxiaolin
 * @date 2018-05-16 下午5:12
 */
public class WebUtil {
    private static final Logger logger = LoggerFactory.getLogger(WebUtil.class);
    /**
     * 跨域支持
     * @author huangxiaolin
     * @date 2018-05-16 17:13
     */
    public static void enableCORS(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (StringUtils.isEmpty(origin)) {
            origin = "http://127.0.0.1:9798/";
        }
        response.setCharacterEncoding(Constant.CHARSET_UTF8);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setHeader("Access-Control-Allow-Headers", EncryptDecryptService.KEY_INDEX_HEADER);
        response.setHeader("Access-Control-Allow-Methods", "OPTIONS,GET,POST");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Expose-Headers", EncryptDecryptService.KEY_INDEX_HEADER);
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    /**
     * 响应json结果,增加了跨越支持
     * @author hxl
     * 2018/5/21 下午1:37
     */
    public static void writeJSON(HttpServletRequest request,
                                 HttpServletResponse response, CommonResult<?> result)
            throws IOException {
        enableCORS(request, response);
        PrintWriter writer = response.getWriter();
        writer.print(new Gson().toJson(result));
        writer.flush();
    }

    /**
     * 响应json格式，错误信息提示，增加了跨越支持
     * @author hxl
     * 2018/5/18 下午4:47
     */
    public static void writeJSON(HttpServletRequest request,
                                 HttpServletResponse response, Error error) throws IOException {
        writeJSON(request, response, CommonResult.make(error));
        CurrentThreadData.clear();
    }

    public static void printRequestHeaders(HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            System.out.println(name + "=" + request.getHeader(name));
        }

    }

    public static void handleHeaderData(HttpServletRequest request) {
        Enumeration<String> e = request.getHeaderNames();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder = stringBuilder.append("{");
        while (e.hasMoreElements()) {
            String headerName = e.nextElement();
            Enumeration<String> headerVlues = request.getHeaders(headerName);
            while (headerVlues.hasMoreElements()) {
                stringBuilder = stringBuilder.append(headerName + ":").append(headerVlues.nextElement()).append(" ");
            }
        }

        stringBuilder = stringBuilder.append("}");
        logger.info("request headers:{}", stringBuilder.toString());

        if (StringUtils.isEmpty(request.getHeader(Constant.LANGUAGE_TYPE))){
            CurrentThreadData.setLanguage(0);
        } else {
            CurrentThreadData.setLanguage(Integer.valueOf(request.getHeader(Constant.LANGUAGE_TYPE)));
        }


        CurrentThreadData.setIBitID(request.getHeader("x-auth-token"));

        try {
            String clientVersion = request.getHeader(Constant.CLIENT_VERSION);
            if (!StringUtils.isEmpty(clientVersion)) {
                String[] values = clientVersion.split("-");
                if (values.length > 1) {
                    CurrentThreadData.setClientPlatform(values[0]);
                    CurrentThreadData.setClientVersion(Integer.parseInt(values[1]));
                } else {
                    CurrentThreadData.setClientVersion(Integer.parseInt(values[0]));
                }
            }
        } catch (Exception taierr) {
//            err.printStackTrace();
        }
    }

}
