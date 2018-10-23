package com.rst.cgi.data.entity;

import com.rst.cgi.data.dto.Symbol;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author hujia
 */
@Data
@Document(collection = "DExTop_Place_Order")
public class PlaceOrderRecord {
    @Id
    @Field("_id")
    private ObjectId id;
    @ApiModelProperty("交易对")
    private Symbol symbol;
    @ApiModelProperty("交易价格")
    private String price;
    @ApiModelProperty("交易动作：0-买入，1-卖出")
    private int action;
    @ApiModelProperty("交易数量")
    private String amount;
    @ApiModelProperty("交易到期时间戳")
    private Long expireTimeSec;
    @ApiModelProperty("交易钱包地址")
    private String tradeAddr;
}
