package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by mtb on 2018/3/30.
 */
@Getter
@Setter
public class GetAllHdTransactionReqDTO extends Readable{
    @ApiModelProperty(value = "公钥hash列表",required = true)
    private List<TranxAddressHD> walletAddressList;
    private List<String> eosAccountList;
    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;
    @ApiModelProperty(value = "页面大小")
    private Integer pageSize = 10;
    @ApiModelProperty(value = "交易时段（current:当月， history:历史(查询以太坊链币种需要此参数)")
    private String timeType = "current";
    @ApiModelProperty(value = "交易类型（不传时：查询所有，1:收款，-1:付款）")
    private Integer transType;
    @ApiModelProperty(value = "需要查询的代币名称", required = true)
    private String tokenType = "ETH";
    @ApiModelProperty(value = "需要查询代币的coinType(BTC:0, BCH:145, ETH:60, EOS：194）", required = true)
    private Integer coinType;

    @Data
    public static class TranxAddressHD {
        @ApiModelProperty(value = "钱包地址", required = true)
        private String pubkHash;
        @ApiModelProperty(value = "coinType(BTC:0, BCH:145, ETH:60）", required = true)
        private Integer coinType;
        @ApiModelProperty("地址类型:0-pkh,1-sh")
        private int type = 0;
    }

    public static final String EXPENSE = "expense";
    public static final String INCOME = "income";
    public static final String ALL = "all";

}
