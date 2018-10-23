package com.rst.cgi.controller.interceptor;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.data.dto.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;

/**
 * @author hujia
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({RuntimeException.class,SQLException.class})
    @ResponseBody
    public CommonResult<String> exceptionHandler(RuntimeException e){
        logger.error("exceptionHandler {}", e);
        return CommonResult.make(Error.SERVER_EXCEPTION);
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public CommonResult<String> exceptionHandler(CustomException e){
        logger.info("CustomException code:{},msg:{}", e.getError().getCode(), e.getError().getMsg());
        return CommonResult.make(e.getError());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public CommonResult<String> exceptionHandler(HttpMessageNotReadableException e){
        logger.error("exceptionHandler {}",e);
        return CommonResult.make(Error.HTTP_CONTENT_INVALID);
    }
}
