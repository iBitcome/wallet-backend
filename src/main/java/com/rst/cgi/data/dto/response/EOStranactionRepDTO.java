package com.rst.cgi.data.dto.response;

import com.rst.cgi.common.EOS.Tx;
import lombok.Data;

@Data
public class EOStranactionRepDTO {
    private String compression;

    private Tx transaction;

    private byte[] bytes;
}
