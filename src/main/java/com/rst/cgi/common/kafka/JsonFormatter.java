package com.rst.cgi.common.kafka;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import com.alibaba.fastjson.JSONObject;
import com.rst.cgi.common.constant.ServerMainUris;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
* @Description:  日志json格式化
* @Author:  fengpan
* @Date:  2018/8/20 下午2:37
*/


public class JsonFormatter implements Formatter {

    private boolean expectJsonMessage = false;
    private boolean includeMethodAndLineNumber = false;
    private Map extraPropertiesMap = null;

    public String format(ILoggingEvent event) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("level", event.getLevel().levelStr);
        jsonObject.put("class", event.getLoggerName());
        jsonObject.put("content", event.getFormattedMessage());
        jsonObject.put("timestamp", event.getTimeStamp());
        jsonObject.put("thread", event.getThreadName());
        jsonObject.put("host", ServerMainUris.CGI_NAME);

        if (includeMethodAndLineNumber) {
            StackTraceElement[] callerDataArray = event.getCallerData();
            if (callerDataArray != null && callerDataArray.length > 0) {
                StackTraceElement stackTraceElement = callerDataArray[0];
                jsonObject.put("method", stackTraceElement.getMethodName());
                jsonObject.put("line", stackTraceElement.getLineNumber() + "");
            }
        }

        IThrowableProxy proxy = event.getThrowableProxy();
        if(proxy != null){
            jsonObject.put("exceptionmessage",proxy.getMessage());
            jsonObject.put("exception",proxy.getClassName());
            jsonObject.put("exceptionClassArray",proxy.getStackTraceElementProxyArray());
        }

        if (this.extraPropertiesMap != null) {
            jsonObject.putAll(extraPropertiesMap);
        }

        return jsonObject.toJSONString();
    }

    public boolean getExpectJsonMessage() {
        return expectJsonMessage;
    }

    public void setExpectJsonMessage(boolean expectJsonMessage) {
        this.expectJsonMessage = expectJsonMessage;
    }

    public boolean getIncludeMethodAndLineNumber() {
        return includeMethodAndLineNumber;
    }

    public void setIncludeMethodAndLineNumber(boolean includeMethodAndLineNumber) {
        this.includeMethodAndLineNumber = includeMethodAndLineNumber;
    }

    public void setExtraProperties(String thatExtraProperties) {
        final Properties properties = new Properties();
        try {
            properties.load(new StringReader(thatExtraProperties));
            Enumeration enumeration = properties.propertyNames();
            extraPropertiesMap = new HashMap();
            while(enumeration.hasMoreElements()){
                String name = (String)enumeration.nextElement();
                String value = properties.getProperty(name);
                extraPropertiesMap.put(name,value);
            }
        } catch (IOException e) {
            System.out.println("There was a problem reading the extra properties configuration: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
