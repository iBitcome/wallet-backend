package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import lombok.Data;

@Data
public class EOSTransactionDTO extends Readable {
    private String contractAccount;
    private String from;
    private String to;
    private String quantity;
    private String memo;
}
