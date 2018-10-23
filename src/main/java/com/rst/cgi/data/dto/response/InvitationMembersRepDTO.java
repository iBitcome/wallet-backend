package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * Created by matianbao on 2017/9/21.
 */
public class InvitationMembersRepDTO extends Readable {
    @ApiModelProperty(value = "已激活的用户数量")
    private Integer activatedNum;


    public Integer getActivatedNum() {
        return activatedNum;
    }

    public void setActivatedNum(Integer activatedNum) {
        this.activatedNum = activatedNum;
    }
}
