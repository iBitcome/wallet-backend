package com.rst.cgi.common.utils;

import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 日期工具类
 * @author huangxiaolin
 * @date 2017-09-14 下午4:03
 */
public class DateUtil {

    /**
     * 日期格式化字符串，不包括时分秒
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 时间格式化字符串，不包括秒
     */
    public static final String TIME_NOSECONDS_PATTERN = "yyyy-MM-dd HH:mm";
    /**
     * 时间格式化字符串，包括秒
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**一天的开始时间*/
    public static final String DAY_BEGIN_TIME = " 00:00:00";
    /**一天的结束时间*/
    public static final String DAY_END_TIME = " 23:59:59";


    /**
     * 格式化时间，返回时间字符串格式，如果日期对象为null则返回null
     * @param date：日期对象
     * @return 格式化后的日期字符串：yyyy-MM-dd HH:mm:ss
     * @author huangxiaolin
     * @date 2017-09-14 16:06
     */
    public static String formateTime(Date date) {
        return formatDateTime(date, DATE_TIME_PATTERN);
    }

    /**
     * 格式化时间，返回日期字符串格式，如果日期对象为null则返回null
     * @param date：日期值
     * @return 格式化后的日期字符串：yyyy-MM-dd
     * @author huangxiaolin
     * @date 2017-09-14 16:06
     */
    public static String formateDate(Date date) {
        return formatDateTime(date, DATE_PATTERN);
    }

    /**
     * 格式化日期时间
     * @author huangxiaolin
     * @date 2018-01-05 15:19
     * @param date 日期，为null则返回null
     * @param Pattern 格式字符串
     */
    public static String formatDateTime(Date date, String Pattern) {
        if (date == null) {
            return null;
        }
        DateFormat df = null;
        if (StringUtils.isEmpty(Pattern)) {
            df = new SimpleDateFormat(DATE_TIME_PATTERN);
        } else {
            df = new SimpleDateFormat(Pattern);
        }
        return df.format(date);
    }

    /**
     * 获取格式化时间utc
     * @author huangxiaolin
     * @date 2018-05-18 10:41
     */
    public static String formateTimeUtc(Date date) {
        if (date == null) date = new Date();
        DateFormat df = new SimpleDateFormat(DATE_TIME_PATTERN);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }


    /**
     * 根据字符串得到日期，字符串的格式为：yyyy-MM-dd HH:mm:ss或者yyyy-MM-dd HH:mm或者yyyy-MM-dd
     * <p>如果字符为null或者不能解析为日期，则返回null</p>
     * @param timeStr 日期字符串
     * @return 字符串表示的日期值
     * @author huangxiaolin
     * @date 2017-09-15 17:02
     */
    public static Date parseTime(String timeStr) {
        if (StringUtils.isEmpty(timeStr)) {
            return null;
        }
        try {
            int len = timeStr.length();
            if (len == 19) {
                //字符串格式：yyyy-MM-dd HH:mm:ss
                return new SimpleDateFormat(DATE_TIME_PATTERN).parse(timeStr);
            } else if (len == 16) {
                // yyyy-MM-dd HH:mm
                return new SimpleDateFormat(TIME_NOSECONDS_PATTERN).parse(timeStr);
            } else if (len == 10) {
                //字符串格式：yyyy-MM-dd
                return new SimpleDateFormat(DATE_PATTERN).parse(timeStr);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据日期对象得到某年某月的第一天代表的日期
     * @return 返回某年某月的第一天, 格式为：yyyy-MM-dd
     * @author huangxiaolin
     * @date 2017-09-26 10:20
     */
    public static String getMonthFirstDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        String monthStr = month < 10 ? ("0" + month) : String.valueOf(month);
        return year + "-" + monthStr + "-01";
    }

    /**
     * 获取某天的最后时间
     * @author hxl
     * 2018/5/29 上午9:55
     */
    public static Date getDayEndTime(Date date) {
        String endTime = formateDate(date) + DAY_END_TIME;
        return parseTime(endTime);
    }


}
