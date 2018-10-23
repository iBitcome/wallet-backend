package com.rst.cgi.controller.interceptor;

import com.rst.cgi.conf.security.CurrentThreadData;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author hujia
 * @date 2017/7/19
 */
@Aspect
@Component
public class LogAspect {
    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("within(com.rst.cgi.controller.*)")
    public void controllerLog() {}

    @Before("controllerLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {

        try {
            Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            String[] paramNames = methodSignature.getParameterNames();
            Object[] params = joinPoint.getArgs();

            if (paramNames == null || paramNames.length < params.length) {
                paramNames = new String[params.length];
                for (int i = 0; i < params.length; i++) {
                    paramNames[i] = "arg" + i;
                }
            }

            StringBuilder msg = new StringBuilder("【Call】" + signature.getName() + "(userId:" + CurrentThreadData.iBitID() + "):(");

            boolean hideLog = false;
            if (methodSignature.getMethod().isAnnotationPresent(LogHide.class)) {
                String value = methodSignature.getMethod().getAnnotation(LogHide.class).value();
                if (LogHide.REQ.equalsIgnoreCase(value) || StringUtils.isBlank(value)) {
                    msg.append(methodSignature.getParameterTypes()[0] + ")");
                    hideLog = true;
                }
            }
            if (!hideLog) {
                for (int i = 0; i < params.length - 1; i++) {
                    msg.append(paramNames[i] + ":" + params[i] + ",\n");
                }

                if (params.length > 0) {
                    msg.append(paramNames[params.length - 1] + ":" + params[params.length - 1] + ")");
                } else {
                    msg.append(")");
                }
            }

            logger.info(msg.toString());
            startTime.set(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterReturning(returning = "ret", pointcut = "controllerLog()")
    public void doAfterReturning(JoinPoint joinPoint, Object ret) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        if (methodSignature.getMethod().isAnnotationPresent(LogHide.class)) {
           String value = methodSignature.getMethod().getAnnotation(LogHide.class).value();
            if (LogHide.REP.equalsIgnoreCase(value) || StringUtils.isBlank(value))
            ret = ret.getClass();
        }

        logger.info("【Return】{}(userId:{},cost:{}ms):\n {}", joinPoint.getSignature().getName(),
                CurrentThreadData.iBitID(),
                System.currentTimeMillis() - startTime.get(), ret);

    }
}
