package com.rst.cgi.common.hbc;

import com.rst.cgi.service.SpecialAuthService;
import com.rst.thrift.tools.SpringUtil;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * Created by hujia on 2017/3/30.
 */
public class SpecialAuthInterceptor implements HandlerInterceptor {

    public SpecialAuthInterceptor() {
        specialAuthService = SpringUtil.getBean(SpecialAuthService.class);
    }

    @Autowired
    private SpecialAuthService specialAuthService;

    private String tokenName = "x-inner-token";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (handler != null &&
                handler instanceof HandlerMethod){
            Method method = ((HandlerMethod) handler).getMethod();
            if (method.isAnnotationPresent(DisableInnerAuth.class)) {
                return true;
            }
        }

        String token = request.getHeader(tokenName);
        if (token == null || token.isEmpty()) {
            token = request.getParameter(tokenName);
        }

        if (token == null) {
            response.sendError(HttpStatus.SC_UNAUTHORIZED,
                    "required parameter or header 'x-inner-token' is not present");
            return false;
        }

        boolean ret = specialAuthService.isInnerServer(token);
        if (!ret) {
            response.sendError(HttpStatus.SC_UNAUTHORIZED,
                    "ACCESS DENIED: x-inner-token is incorrect");
        }

        return ret;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {

    }
}
