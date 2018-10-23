package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.entity.UserContactsAddress;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * @author hxl
 * @date 2018/5/29 下午3:08
 */
@Data
public class UserContactsDTO {
    @ApiModelProperty("联系人ID，新增联系人不需要传，修改联系人时需要")
    private Integer contactsId;
    @ApiModelProperty("联系人名称")
    private String contactsName;
    @ApiModelProperty("联系人地址列表：token:代币名称，address:地址")
    private List<UserContactsAddress> addressList;

    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }
}
