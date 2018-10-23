package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.entity.EosAccount;
import lombok.Data;

import java.util.List;

@Data
public class EosAccountAssetReqDTO {

    private List<EosAccountAddress> addList;
    @Data
    public static class EosAccountAddress{
        private String address;
        private List<String> accountList;
    }
}
