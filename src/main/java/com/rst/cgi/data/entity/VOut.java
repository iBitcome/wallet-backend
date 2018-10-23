package com.rst.cgi.data.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class VOut {
    @SerializedName("vout_value")
    private Long value;
    @SerializedName("output_address")
    private String address;
    @SerializedName("pk_script")
    private String script;
}
