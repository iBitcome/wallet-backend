package com.rst.cgi.common.kafka;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
* @Description:
* @Author:  fengpan
* @Date:  2018/8/20 下午2:42
*/

public interface Formatter {
    String format(ILoggingEvent event);
}
