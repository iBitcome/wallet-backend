package com.rst.cgi.data.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hujia
 */
@Getter
@Setter
public class TokenData {
    @Getter
    @Setter
    public static class Address {
        public static final int TYPE_PKH = 0;
        public static final int TYPE_SH = 1;

        @ApiModelProperty("地址的原始20字节的hash的HexString")
        private String hash;
        @ApiModelProperty("地址类型:0-pkh,1-sh")
        private int type = TYPE_PKH;
        @ApiModelProperty("hd专用")
        private String hdPath;
        @ApiModelProperty("eth专用")
        private Long ethNonce;
        @ApiModelProperty("EOS账户列表")
        private List<String> eosAccountList;
        public Address(String hash, int type,
                       String hdPath, Long ethNonce) {
            this.hash = hash;
            this.type = type;
            this.hdPath = hdPath;
            this.ethNonce = ethNonce;
        }
        public Address(String hash, int type) {
            this.hash = hash;
            this.type = type;
        }

        public Address() {}
    }

    @ApiModelProperty("资产名称")
    private String name;
    @ApiModelProperty("已使用的地址")
    private List<Address> addresses;
    @ApiModelProperty("已推导的地址的最大深度信息")
    private List<DepthLimit> depthLimits;

    public TokenData(String name, List<Address> addresses, List<DepthLimit> depthLimits) {
        this.name = name;
        this.addresses = addresses;
        this.depthLimits = depthLimits;
    }

    public TokenData() {}

    public void addAddress(String hash, int type, String hdPath, Long ethNonce) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }

        addresses.add(new Address(hash, type, hdPath, ethNonce));
    }

    public void addAddress(String hash, int type) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }

        addresses.add(new Address(hash, type,
                null, null));
    }

    public void addAddress(String hash) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }

        addresses.add(new Address(hash, Address.TYPE_PKH,
                null, null));
    }
}
