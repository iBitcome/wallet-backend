package com.rst.cgi.data.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 *
 * @author mtb
 * @date 2018/4/14
 */
@Document(collection = "version_configure")
@Data
public class VersionConfigure {
    @Id
    @Field("_id")
    private String platform;
    @ApiModelProperty(value = "ios上架是否伪造")
    private boolean iosFake;
    @ApiModelProperty(value = "android上架是否伪造")
    private boolean androidFake;
    @ApiModelProperty(value = "ios最低有效版本")
    private String iosValidMinVersion;
    @ApiModelProperty(value = "ios最高有效版本")
    private String iosMaxVersion;
    @ApiModelProperty(value = "android最低有效版本")
    private String androidValidMinVersion;
    @ApiModelProperty(value = "android最高有效版本")
    private String androidMaxVersion;
    @ApiModelProperty(value = "android最高有效版本")
    private String androidDownloadUrl;
    @ApiModelProperty(value = "android最高有效版本")
    private String iosDownloadUrl;
}
