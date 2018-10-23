package com.rst.cgi.data.dto.request;

import com.rst.cgi.data.dto.Readable;
import com.rst.cgi.data.entity.VersionConfigure;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "设置cgi的相关参数")
public class ConfigReqDTO extends Readable {
    @ApiModelProperty(value = "客户端版本支持信息设置")
    private VersionConfigure versionConfigure;

    public VersionConfigure getVersionConfigure() {
        return versionConfigure;
    }

    public void setVersionConfigure(VersionConfigure versionConfigure) {
        this.versionConfigure = versionConfigure;
    }
}
