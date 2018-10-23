package com.rst.cgi.data.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class VIn {
    @SerializedName("pre_txid")
    private String txId;
    @SerializedName("pre_txid_vout")
    private long outIndex;
    @SerializedName("pre_vout_value")
    private long value;
    @SerializedName("sequence")
    private long sequence;
    @SerializedName("input_address")
    private String address;
    @SerializedName("signature_script")
    private String signatureScript;
}
