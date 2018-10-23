package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mtb on 2018/3/30.
 */
@Getter
@Setter
public class GetNewsRepDTO extends Readable{
    @ApiModelProperty(value = "资讯总数")
    private Integer newsCount;
    @ApiModelProperty(value = "资讯详情列表")
    private List<NewsInfo> newsInfoList = new ArrayList<>();

    @Getter
    @Setter
    public static class NewsInfo {
        private Integer id;
        @ApiModelProperty(value = "创建人")
        private String createBy;
        @ApiModelProperty(value = "更新时间")
        private String updateTime;
        @ApiModelProperty(value = "创建时间")
        private String createTime;
        @ApiModelProperty(value = "文章标题")
        private String title;
        @ApiModelProperty(value = "文章封面")
        private String cover;
        @ApiModelProperty(value = "文章来源")
        private String source;
        @ApiModelProperty(value = "文章正文")
        private String content;
        @ApiModelProperty(value = "点击量")
        private Integer hits;
        @ApiModelProperty(value = "是否置顶(1置顶，0不置顶)")
        private Integer isStick;
    }
}
