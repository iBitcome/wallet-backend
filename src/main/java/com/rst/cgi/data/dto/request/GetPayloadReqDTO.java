package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

@Setter
@Getter
public class GetPayloadReqDTO extends Readable {
    @ApiModelProperty("创建载荷数据的类型(0:燃烧，1：转币，2：网关兑换)")
    private Integer payloadType = BURN_PAYLOAD;
    @ApiModelProperty("转币信息（转币需要）")
    private PayloadSendToken payloadSendToken = new PayloadSendToken();
    @ApiModelProperty("网关币币兑换信息")
    private PayloadExToken payloadExToken = new PayloadExToken();

    @Data
    public static class PayloadSendToken {
        @ApiModelProperty("转币数量）")
        String value ;
        @ApiModelProperty("币种名称(默认值是WHC)")
        String tokenName = "WHC";

        public String getTokenName() {
            if (StringUtils.isBlank(tokenName)) {
                return "WHC";
            }
            return tokenName;
        }
    }

    @Data
    public static class PayloadExToken {
        @ApiModelProperty("持有币种名称（如：bch）")
        private String fromToken;
        @ApiModelProperty("目的币种名称（如：eth）")
        private String toToken;
        @ApiModelProperty("目的链收款地址")
        private String address;
    }

    public static final Integer BURN_PAYLOAD = 0;
    public static final Integer SEND_PAYLOAD = 1;
    public static final Integer GATE_WAY_PAYLOAD = 2;//网关兑换
}
