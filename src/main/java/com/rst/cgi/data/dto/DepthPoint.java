package com.rst.cgi.data.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Data
public class DepthPoint {
    @ApiModelProperty("询价")
    private String price;
    @ApiModelProperty("买入/卖出量")
    private String quantity;

    public DepthPoint(){}

    public DepthPoint(String price, String quantity){
        this.price = price;
        this.quantity = quantity;
    }

    public String toRedisString() {
        return price + "-" + quantity;
    }

    public static DepthPoint fromRedisString(String redisValue) {
        if (StringUtils.isEmpty(redisValue)) {
            return null;
        }

        String[] values = redisValue.split("-");
        if (values == null || values.length != 2) {
            return null;
        }

        return new DepthPoint(values[0], values[1]);
    }

    public static String toRedisString(List<DepthPoint> depthPoints) {
        if (depthPoints == null || depthPoints.isEmpty()) {
            return null;
        }

        StringBuilder redisString = new StringBuilder();
        depthPoints.forEach(depthPoint -> redisString.append(depthPoint.toRedisString()).append("&"));
        return redisString.toString();
    }

    public static List<DepthPoint> listFromRedisString(String redisValue) {
        if (StringUtils.isEmpty(redisValue)) {
            return null;
        }

        return Arrays.stream(redisValue.split("&"))
                     .map(redisString -> fromRedisString(redisString))
                     .filter(item -> item != null)
                     .collect(Collectors.toList());
    }
}
