package com.rst.cgi.data.entity;

import com.rst.cgi.data.dto.Readable;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigInteger;

/**
 * Created by mtb on 2018/4/8.
 */
@Document(collection = "tranx")
public class Transaction extends Readable {
    @Id
    @Field("_id")
    private ObjectId objectId;
    private String from;//交易发起钱包地址
    private String to;//交易接受钱包地址
    private String contract;//协议地址
    private Double value;//交易金额（单位wei）
    private String hash;//交易hash
    private Integer type;//1-交易确认,2-交易被打包入块,3-交易未打包,4-最新区块高度
    private Long timestamp;//时间戳
    private Integer height;//所在区块高度
    private Boolean success;//交易是否成功
    private Integer tranxIndex;//交易在块中的顺序
    @Field("txFee")
    private Double txFee;//矿工费（单位wei）
    private String method;
    private TransactionInput input;


    public static class TransactionInput {
        @Field("_spender")
        private String spender;
        @Field("_value")
        private Double value;
        private String method;

        public String getSpender() {
            return spender;
        }

        public void setSpender(String spender) {
            this.spender = spender;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

    public TransactionInput getInput() {
        return input;
    }

    public void setInput(TransactionInput input) {
        this.input = input;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getTranxIndex() {
        return tranxIndex;
    }

    public void setTranxIndex(Integer tranxIndex) {
        this.tranxIndex = tranxIndex;
    }

    public Double getTxFee() {
        return txFee;
    }

    public void setTxFee(Double txFee) {
        this.txFee = txFee;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
