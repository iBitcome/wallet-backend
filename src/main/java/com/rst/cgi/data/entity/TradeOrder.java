package com.rst.cgi.data.entity;

import com.rst.cgi.conf.ExchangeConfig;
import com.rst.cgi.data.dto.Symbol;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.sf.json.JSONObject;
/**
 * @author hujia
 */
@Data
public class TradeOrder {
    @ApiModelProperty("订单id")
    private String orderId;
    @ApiModelProperty("交易对")
    private Symbol symbol;
    @ApiModelProperty("订单类型：0-买，1-卖")
    private Integer action;
    @ApiModelProperty("价格")
    private String price;
    @ApiModelProperty("总量")
    private String amountTotal;
    @ApiModelProperty("已经成交总量")
    private String amountFilled;
    @ApiModelProperty("成交均价")
    private String filledAveragePrice;
    @ApiModelProperty("订单状态：0-未成交，1-部分成交，2-全部成交，3-取消，4-过期, 5-部分过期，6-部分撤销，-1-其他状态")
    private Integer status;
    @ApiModelProperty("订单创建时间：毫秒时间戳")
    private Long createTimeMs;
    @ApiModelProperty("订单更新时间：毫秒时间戳")
    private Long updateTimeMs;
    @ApiModelProperty("订单过期时间：秒时间戳")
    private Long expireTimeSec;
    @ApiModelProperty("订单nonce：发出时的时间戳值")
    private String nonce;

    public static final int ACTION_SELL = 1;
    public static final int ACTION_BUY = 0;
    public static final String DEX_TOP_ACTION_BUY = "Buy";
    public static final String DEX_TOP_ACTION_SELL = "Sell";

    public static Integer fromDexTopAction(String action) {
        if (DEX_TOP_ACTION_BUY.equalsIgnoreCase(action)) {
            return ACTION_BUY;
        } else if (DEX_TOP_ACTION_SELL.equalsIgnoreCase(action)) {
            return ACTION_SELL;
        }
        return null;
    }

    public static String toDexTopAction(Integer action) {
        if (ACTION_BUY == action) {
            return DEX_TOP_ACTION_BUY;
        } else if (ACTION_SELL == action) {
            return DEX_TOP_ACTION_SELL;
        }

        return null;
    }

    public static final int STATUS_FILLED = 2;
    public static final int STATUS_UNFILLED = 0;
    public static final int STATUS_PARTIALLY_FILLED = 1;
    public static final int STATUS_CANCELLED = 3;
    public static final int STATUS_EXPIRED = 4;
    public static final int STATUS_PARTIALLY_EXPIRED = 5;
    public static final int STATUS_PARTIALLY_CANCELLED = 6;

    public static final String DEX_TOP_STATUS_FILLED = "Filled";
    public static final String DEX_TOP_STATUS_UNFILLED = "UnFilled";
    public static final String DEX_TOP_STATUS_PARTIALLY_FILLED = "PartiallyFilled";
    public static final String DEX_TOP_STATUS_CANCELLED = "Cancelled";
    public static final String DEX_TOP_STATUS_PARTIALLY_CANCELLED = "PartiallyCancelled";
    public static final String DEX_TOP_STATUS_EXPIRED = "Expired";
    public static final String DEX_TOP_STATUS_PARTIALLY_EXPIRED = "PartiallyExpired";

    public static Integer fromDexTopStatus(String status) {
        if (DEX_TOP_STATUS_FILLED.equalsIgnoreCase(status)) {
            return STATUS_FILLED;
        } else if (DEX_TOP_STATUS_UNFILLED.equalsIgnoreCase(status)) {
            return STATUS_UNFILLED;
        } else if (DEX_TOP_STATUS_PARTIALLY_FILLED.equalsIgnoreCase(status)) {
            return STATUS_PARTIALLY_FILLED;
        } else if (DEX_TOP_STATUS_CANCELLED.equalsIgnoreCase(status)) {
            return STATUS_CANCELLED;
        } else if (DEX_TOP_STATUS_EXPIRED.equalsIgnoreCase(status)) {
            return STATUS_EXPIRED;
        } else if (DEX_TOP_STATUS_PARTIALLY_EXPIRED.equalsIgnoreCase(status)) {
            return STATUS_PARTIALLY_EXPIRED;
        } else if (DEX_TOP_STATUS_PARTIALLY_CANCELLED.equalsIgnoreCase(status)) {
            return STATUS_PARTIALLY_CANCELLED;
        }

        return -1;
    }

    public static String toDexTopStatus(Integer status) {
        if (STATUS_FILLED == status) {
            return DEX_TOP_STATUS_FILLED;
        } else if (STATUS_UNFILLED == status) {
            return DEX_TOP_STATUS_UNFILLED;
        } else if (STATUS_PARTIALLY_FILLED == status) {
            return DEX_TOP_STATUS_PARTIALLY_FILLED;
        } else if (STATUS_CANCELLED == status) {
            return DEX_TOP_STATUS_CANCELLED;
        } else if (STATUS_EXPIRED == status) {
            return DEX_TOP_STATUS_EXPIRED;
        } else if (STATUS_PARTIALLY_EXPIRED == status) {
            return DEX_TOP_STATUS_PARTIALLY_EXPIRED;
        } else if (STATUS_PARTIALLY_CANCELLED == status) {
            return DEX_TOP_STATUS_PARTIALLY_CANCELLED;
        }

        return null;
    }

    public static TradeOrder parseDexTopJson(JSONObject order) {
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setAction(
                TradeOrder.fromDexTopAction(order.getString("action")));
        tradeOrder.setStatus(
                TradeOrder.fromDexTopStatus(order.getString("status")));
        tradeOrder.setAmountFilled(order.getString("amountFilled"));
        tradeOrder.setAmountTotal(order.getString("amountTotal"));
        tradeOrder.setCreateTimeMs(order.getLong("createTimeMs"));
        tradeOrder.setExpireTimeSec(order.getLong("expireTimeSec"));
        tradeOrder.setFilledAveragePrice(order.getString("filledAveragePrice"));
        tradeOrder.setUpdateTimeMs(order.getLong("updateTimeMs"));
        tradeOrder.setNonce(order.getString("nonce"));
        tradeOrder.setPrice(order.getString("price"));
        tradeOrder.setSymbol(Symbol.from(order.getString("pairId"),
                ExchangeConfig.DEX_TOP.getSymbolSeparator()));
        tradeOrder.setOrderId(order.getString("orderId"));
        return tradeOrder;
    }

}
