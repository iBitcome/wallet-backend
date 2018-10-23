package com.rst.cgi.data.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hujia
 */
@Getter
@Setter
public class DepthLimit {
    @ApiModelProperty("父路径")
    private String parent;
    @ApiModelProperty("路径下已推导到的最大的孩子索引")
    private int maxIndex;

    public DepthLimit(String parent, int maxIndex) {
        this.parent = parent;
        this.maxIndex = maxIndex;
    }

    public DepthLimit() {}
}
