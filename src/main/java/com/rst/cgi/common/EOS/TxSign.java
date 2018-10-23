package com.rst.cgi.common.EOS;

import lombok.Data;

@Data
class TxSign extends BaseVo {

    public TxSign() {

    }

    public TxSign(String chain_id, Tx transaction) {
        this.chain_id = chain_id;
        this.transaction = transaction;
    }

    private String chain_id;

    private Tx transaction;

}

