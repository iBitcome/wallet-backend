package com.rst.cgi.data.dto.request;

import com.rst.cgi.common.EOS.Tx;
import lombok.Data;

@Data
public class EOStransactionReqDto {
    private String compression;

    private Tx transaction;

    private String[] signatures;

}
