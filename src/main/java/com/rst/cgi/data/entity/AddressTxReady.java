package com.rst.cgi.data.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author hujia
 */
@Data
@Document(collection = "address_tx_ready")
public class AddressTxReady {
    @Id
    @Field("_id")
    private String atr;

    public AddressTxReady() {}
    public AddressTxReady(String atr) {this.atr = atr;}
}
