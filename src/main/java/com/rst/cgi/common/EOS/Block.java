package com.rst.cgi.common.EOS;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Block {

    @JsonProperty("timestamp")
    private Date timestamp;

    @JsonProperty("producer")
    private String producer;

    @JsonProperty("confirmed")
    private Long confirmed;

    @JsonProperty("previous")
    private String previous;

    @JsonProperty("transaction_mroot")
    private String transactionMroot;

    @JsonProperty("action_mroot")
    private String actionMroot;

    @JsonProperty("schedule_version")
    private String scheduleVersion;

    @JsonProperty("id")
    private String id;

    @JsonProperty("block_num")
    private Long blockNum;

    @JsonProperty("ref_block_prefix")
    private Long refBlockPrefix;

    public Block() {

    }
}

