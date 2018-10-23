package com.rst.cgi.data.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author hujia
 */
@Data
public class KlinePoint {
    @Id
    @Field("_id")
    @ApiModelProperty("开盘时间")
    private Long timestamp;
    @ApiModelProperty("开盘价")
    private String open;
    @ApiModelProperty("最高价")
    private String high;
    @ApiModelProperty("最低价")
    private String low;
    @ApiModelProperty("收盘价")
    private String close;
    @ApiModelProperty("成交总量")
    private String volume;

    public static final String ONE_MINUTE = "1m";
    public static final String THREE_MINUTES = "3m";
    public static final String FIVE_MINUTES = "5m";
    public static final String FIFTEEN_MINUTES = "15m";
    public static final String HALF_HOURLY = "30m";
    public static final String HOURLY = "1h";
    public static final String TWO_HOURLY = "2h";
    public static final String FOUR_HOURLY = "4h";
    public static final String SIX_HOURLY = "6h";
    public static final String EIGHT_HOURLY = "8h";
    public static final String TWELVE_HOURLY = "12h";
    public static final String DAILY = "1d";
    public static final String THREE_DAILY = "3d";
    public static final String WEEKLY = "1w";
    public static final String MONTHLY = "1M";

    public static long millSecondsOf(String kInterval) {
        long value = Long.parseLong(kInterval.substring(0, kInterval.length() - 1));
        String suffix = kInterval.substring(kInterval.length() - 1, kInterval.length());

        if (suffix.equalsIgnoreCase("h")) {
            return value * 60 * 60 * 1000;
        }

        if (suffix.equalsIgnoreCase("d")) {
            return value * 24 * 60 * 60 * 1000;
        }

        if (suffix.equals("m")) {
            return value  * 60 * 1000;
        }

        if (suffix.equals("M")) {
            return value * 28 * 24 * 60 * 60 * 1000;
        }

        if (suffix.equalsIgnoreCase("w")) {
            return value * 7 * 24 * 60 * 60 * 1000;
        }

        return 0;
    }
}
