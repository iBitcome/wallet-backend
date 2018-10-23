package com.rst.cgi.data.dto;


import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.rst.cgi.data.entity.BlockTransaction;
import com.rst.cgi.data.entity.Token;
import com.rst.cgi.data.entity.VIn;
import com.rst.cgi.data.entity.VOut;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EOSMessage {
    @SerializedName("tx_id")
    private String txId;
    @SerializedName("status")
    private Integer status;
    @SerializedName("value")
    private String value;
    @SerializedName("block_num")
    private Integer blockNum;
    @SerializedName("from")
    private String from;
    @SerializedName("to")
    private String to;
    @SerializedName("timestamp")
    private String timestamp;
    public BlockTransaction totransaction(Token token){
        BlockTransaction blockTransaction = new BlockTransaction();
        if(status == 0){
            blockTransaction.setStatus(BlockTransaction.PACKED);
        }else if(status == 1){
            blockTransaction.setStatus(BlockTransaction.CONFIRMED);
        }else{
            blockTransaction.setStatus(BlockTransaction.UNKNOWN);
        }
        blockTransaction.setTokenName(token.getName());
        blockTransaction.setHeight(blockNum);
        blockTransaction.setChannel("EOS");
        blockTransaction.setType(value);
        VIn vIn = new VIn();
        vIn.setAddress(from);
        List<VIn> inList =Lists.newArrayList();
        inList.add(vIn);
        blockTransaction.setVIns(inList);
        VOut vOut = new VOut();
        vOut.setAddress(to);
        List<VOut> outList =Lists.newArrayList();
        outList.add(vOut);
        blockTransaction.setVOuts(outList);
        blockTransaction.setTxId(txId);
        return blockTransaction;
    }
}
