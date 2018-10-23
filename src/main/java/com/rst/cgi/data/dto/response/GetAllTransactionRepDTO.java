package com.rst.cgi.data.dto.response;


import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by mtb on 2018/3/30.
 */
@Setter
@Getter
public class GetAllTransactionRepDTO extends Readable {
    @ApiModelProperty(value = "支付类型（-1：付款，0: 自转账，1：收款 ）")
    private Integer type;
    @ApiModelProperty(value = "交易状态（3:pending,2:packed,1:confirmed）")
    private Integer status;
    @ApiModelProperty(value = "交易发起钱包地址(以太坊链专用)")
    private String fromWallet;
    @ApiModelProperty(value = "交易接受钱包地址（以太链专用）")
    private String toWallet;
    @ApiModelProperty(value = "协议地址（每种代币对应一种协议）")
    private String contract;
    @ApiModelProperty(value = "金额(单位wei)")
    private Double money;
    @ApiModelProperty(value = "交易时间戳")
    private Long time;
    @ApiModelProperty(value = "交易所在的区块高度")
    private Integer height;
    @ApiModelProperty(value = "交易是否成功")
    private Boolean success;
    @ApiModelProperty(value = "交易hash")
    private String hash;
    @ApiModelProperty(value = "矿工费（最小单位）")
    private Double txFee;
    @ApiModelProperty(value = "代币英文简称")
    private String tokenName;
    @ApiModelProperty(value = "代币价格（单位：美元）")
    private Double price;
    @ApiModelProperty(value = "代币进制（10的幂）")
    private Integer decimal;
    @ApiModelProperty(value = "付款钱包地址（自有链代币专用）")
    private List<TranxAddrInfo> inputAddress;
    @ApiModelProperty(value = "收款钱包地址（自有链代币专用）")
    private List<TranxAddrInfo> outputAddress;
    @ApiModelProperty(value = "EOS交易memo")
    private String memo;
    @ApiModelProperty(value = "确认数")
    private Long confirmNum;
    @ApiModelProperty(value = "目标确认数")
    private Long targetConfirmNum;
    @ApiModelProperty(value = "交易详情页面（三方）")
    private String webTxUrl;

    @Data
    public static class TranxAddrInfo{
        @ApiModelProperty(value = "钱包地址")
        private String address;
        @ApiModelProperty(value = "金额(最小单位)")
        private Double money;
    }

    public static final Integer EXPENSE = -1;
    public static final Integer INCOME = 1;


    public static final Integer PENDING = 3;
    public static final Integer PACKED = 2;
    public static final Integer CONFIRMED = 1;
    public static final Integer UNKNOWN = -2;
}
