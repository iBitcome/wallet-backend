package com.rst.cgi.common.EOS;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Tx extends BaseVo {
    private Object expiration;

    private Long ref_block_num;

    private Long ref_block_prefix;

    private Long net_usage_words;

    private Long max_cpu_usage_ms;

    private Long delay_sec;

    private List<String> context_free_actions = new ArrayList();

    private List<TxAction> actions;

    private List<TxExtenstions> transaction_extensions = new ArrayList();
}
