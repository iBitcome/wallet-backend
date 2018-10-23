package com.rst.cgi.data.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Data
public class TradePoint {
    @Id
    @Field("_id")
    @ApiModelProperty("成交时间")
    private Long tradeTime;
    @ApiModelProperty("成交均价")
    private String price;
    @ApiModelProperty("成交量")
    private String quantity;
    @ApiModelProperty("买-0,卖-1,合并-2")
    private Integer type;

    public TradePoint() {}

    public TradePoint(long tradeTime, String price,
                      String quantity, Integer type) {
        this.price = price;
        this.tradeTime = tradeTime;
        this.quantity = quantity;
        this.type = type;
    }

    public String toRedisString() {
        return tradeTime + "-" + price + "-" + quantity + "-" + type;
    }

    public static TradePoint fromRedisString(String redisValue) {
        if (StringUtils.isEmpty(redisValue)) {
            return null;
        }

        String[] values = redisValue.split("-");
        if (values == null || values.length != 4) {
            return null;
        }

        return new TradePoint(Long.parseLong(values[0]), values[1],
                values[2], Integer.parseInt(values[3]));
    }

    public static List<TradePoint> toAggTrades(List<TradePoint> points, TimeUnit unit) {
        List<TradePoint> tradePoints = points.stream()
                     .collect(
                             Collectors.groupingBy(
                                     point -> unit.convert(point.getTradeTime(), TimeUnit.MILLISECONDS)))
                     .values()
                     .stream()
                     .map(item -> combine(item))
                     .collect(Collectors.toList());
        tradePoints.forEach(tradePoint -> tradePoint.setTradeTime(
                unit.toMillis(unit.convert(tradePoint.getTradeTime(), TimeUnit.MILLISECONDS))));
        return tradePoints;
    }

    public static TradePoint combine(List<TradePoint> points) {
        long tradeTime = 0;
        Double priceValue = 0.0;
        Double quantity = 0.0;

        for (TradePoint point : points) {
            Double currentQuantity = Double.parseDouble(point.quantity);
            quantity += currentQuantity;
            priceValue += Double.parseDouble(point.price) * currentQuantity;
            tradeTime += (point.tradeTime);
        }

        tradeTime /= points.size();
        return new TradePoint(tradeTime, "" + (priceValue / quantity),
                "" + quantity, 2);
    }
}
