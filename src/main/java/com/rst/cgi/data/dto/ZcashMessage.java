package com.rst.cgi.data.dto;

import com.google.gson.annotations.SerializedName;
import com.rst.cgi.common.EOS.Tx;
import com.rst.cgi.data.entity.BlockTransaction;
import com.rst.cgi.data.entity.Token;
import com.rst.cgi.data.entity.VIn;
import com.rst.cgi.data.entity.VOut;
import lombok.Data;

import java.util.List;

@Data
public class ZcashMessage {
    @SerializedName("block_hash")
    private String blockHash;
    @SerializedName("block_height")
    private Integer blockHeight;
    @SerializedName("block_time")
    private Long BlockTime;
    @SerializedName("txid")
    private String Txid;
    @SerializedName("txid_index")
    private Integer TxidIndex;
    @SerializedName("version")
    private Integer version;
    @SerializedName("size")
    private Integer size;
    @SerializedName("locktime")
    private Long lockTime;
    @SerializedName("status")
    private Integer Status;
    @SerializedName("confirm")
    private Long comfirm;
    @SerializedName("vin")
    private List<VIn> vin;
    @SerializedName("vout")
    private List<VOut> vout;

    public BlockTransaction toTransaction(Token token) {
        BlockTransaction blockTransaction = new BlockTransaction();
        blockTransaction.setTxId(Txid);
        blockTransaction.setTokenName(token.getName());
        blockTransaction.setBlockTime(BlockTime);
        blockTransaction.setHeight(blockHeight);
        if(Status == 0){
            blockTransaction.setStatus(BlockTransaction.PENDING);
        } else if (Status == 1){
            blockTransaction.setStatus(BlockTransaction.CONFIRMED);
        } else if (Status == 3){
            blockTransaction.setStatus(BlockTransaction.PACKED);
        } else if (Status == 2){
            blockTransaction.setStatus(BlockTransaction.FAILED);
        }
        blockTransaction.setVIns(vin);
        blockTransaction.setVOuts(vout);
        return blockTransaction;
    }

}
