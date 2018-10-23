package com.rst.cgi.data.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Data
@Document(collection = "block_chain_transaction")
public class BlockTransaction {
    @Id
    @Field("_id")
    private String txId;
    private String TokenName;
    private Integer index;
    private Integer lockTime;
    private Integer status;
    private Integer height;
    private Long blockTime;
    private Long pendingTime;
    private Long confirmedTime;
    private List<VIn> vIns;
    private List<VOut> vOuts;
    private Boolean whcIsSuccess;//WHC交易是否有效
    private String whcFailReason;//WHC交易失败的原因
    private Integer typeInt;
    private String type;
    private String channel;
    private String tranxType = TRANSFER;//(正常转账交易/不存在则为其他交易)


    public static final int UNKNOWN = -2;
    public static final int FAILED = -1;
    public static final int PENDING = 0;
    public static final int PACKED = 1;
    public static final int CONFIRMED = 2;
    public static final int EXCHANGE_SUCCESS = 3;
    public static final String TX_CHANNEL_BEECOIN = "beecoin";
    public static final String TX_CHANNEL_OTHER = "other";

    public static final String TRANSFER = "transfer";
    public static final String OTHER = "other";

    public List<String> inAddressList() {
        return vIns.stream().map(in -> in.getAddress())
                   .filter(address -> !StringUtils.isEmpty(address))
                   .collect(Collectors.toList());
    }

    public List<String> outAddressList() {
        if(vOuts != null){
            return vOuts.stream().map(out -> out.getAddress())
                    .filter(address -> !StringUtils.isEmpty(address))
                    .collect(Collectors.toList());
        }else{
            List<String> nullList=new ArrayList();
            return nullList;
        }

    }

    public Long inTotalValue() {
        return vIns.stream().mapToLong(in -> in.getValue()).sum();
    }

    public Long outTotalValue() {
        return vOuts.stream().mapToLong(out -> out.getValue()).sum();
    }
}
