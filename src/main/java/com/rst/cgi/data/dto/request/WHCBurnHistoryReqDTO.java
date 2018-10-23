package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class WHCBurnHistoryReqDTO extends Readable {
    @ApiModelProperty(value = "WHC地址列表",required = true)
    private List<AddressReqDTO.Address> addressList;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;
    @ApiModelProperty(value = "页面大小")
    private Integer pageSize = 10;
}
