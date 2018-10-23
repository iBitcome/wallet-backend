package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
* @Description:    
* @Author:  mtb 
* @Date:  2018/9/25 上午10:14
*/
@Getter
@Setter
public class InvitationTopRepDTO extends Readable {
    private Integer id;
    @ApiModelProperty("用户编号")
    private String loginName;
    @ApiModelProperty("成功邀请人数")
    private Integer invitationNum;
}
