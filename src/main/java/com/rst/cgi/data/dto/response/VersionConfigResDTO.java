package com.rst.cgi.data.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author hujia
 * @date 2017/9/12
 */
@ApiModel(value="版本配置信息")
@Data
public class VersionConfigResDTO {
    @ApiModelProperty(value = "是否需要强制升级")
    private Boolean needForceUpdate;
    @ApiModelProperty(value = "是否最新版本")
    private Boolean latestVersion;
    @ApiModelProperty(value = "iOS版本是否需要假数据")
    private Boolean iosFake;
    @ApiModelProperty(value = "android是否需要假数据")
    private Boolean androidFake;
    @ApiModelProperty(value = "iOS最新版本下载地址")
    private String iosDownloadUrl;
    @ApiModelProperty(value = "android最新版本下载地址")
    private String androidDownloadUrl;
    @ApiModelProperty(value = "ios最新版本号")
    private String iosLatestVersion;
    @ApiModelProperty(value = "android最新版本号")
    private String androidLatestVersion;
}
