package com.rst.cgi.controller.interceptor;

import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.utils.WebUtil;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author hujia
 * @date 2017/3/31
 */
public class SessionRenewalInterceptor implements HandlerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(SessionRenewalInterceptor.class);
    @Autowired
    UserService userService;


    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        WebUtil.handleHeaderData(request);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            //保证当前session至少可以继续存活60分钟
            /*int liveTime = (int)((System.currentTimeMillis() - session.getCreationTime()) / 1000);
            int minLiveTime = liveTime + 60 * 60;
            if (minLiveTime > session.getMaxInactiveInterval()) {
                session.setMaxInactiveInterval(minLiveTime);
            }*/
            session.setMaxInactiveInterval(Constant.DEFAULT_SESSION_LIVE_TIME);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        CurrentThreadData.clear();
    }
}
