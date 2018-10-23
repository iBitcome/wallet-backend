package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class GetAddressAssetsRepDTO extends Readable {
    @ApiModelProperty(value = "数据列表")
    private List<GetAddressAssetInside> content;
    @ApiModelProperty(value = "代币简称")
    private String tokenName;

    public static class GetAddressAssetInside {
        @ApiModelProperty(value = "钱包地址")
        private String address;
        @ApiModelProperty(value = "资产（最小单位）")
        private String asset;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAsset() {
            return asset;
        }

        public void setAsset(String asset) {
            this.asset = asset;
        }
    }


    public List<GetAddressAssetInside> getContent() {
        return content;
    }

    public void setContent(List<GetAddressAssetInside> content) {
        this.content = content;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }
}
