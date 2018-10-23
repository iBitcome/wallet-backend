package com.rst.cgi.controller.interceptor;

import com.google.common.collect.Lists;
import com.rst.cgi.common.utils.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class TestControllerInterceptor implements HandlerInterceptor {
    @Value("${zookeeper.server.enable}")
//    @Value("${money.rate}")
    private boolean enable;

    private final Logger logger = LoggerFactory.getLogger(TestControllerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        if(enable){
            logger.info("请求Ip:{}",IpUtil.clientIpFrom(request));
            String ip = IpUtil.clientIpFrom(request);
            BufferedReader bufferedReader = new BufferedReader(new FileReader("ipWhiteList.txt"));
            String data;
            List<String> list=Lists.newArrayList();
            while((data=bufferedReader.readLine())!=null){
                list.add(data);
            }
            bufferedReader.close();
            if(list.contains(ip)){
                return true;
            }else{
                response.sendError(400,"当前无权访问");
                return false;
            }
        }else{
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
