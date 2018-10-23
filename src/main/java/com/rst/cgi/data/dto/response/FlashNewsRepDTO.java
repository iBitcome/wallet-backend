package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class FlashNewsRepDTO extends Readable {

    @ApiModelProperty(value = "数据总数条数")
    private Integer countNum;
    @ApiModelProperty(value = "快讯列表")
    private List<FlashNews> flashNewsList = new ArrayList<>();

    public static class FlashNews {
        private Integer id;
        @ApiModelProperty(value = "创建人")
        private String createBy;
        @ApiModelProperty(value = "创建时间")
        private String createTime;
        @ApiModelProperty(value = "更新时间")
        private String updateTime;
        @ApiModelProperty(value = "封面图片地址")
        private String url;
        @ApiModelProperty(value = "标题")
        private String title;
        @ApiModelProperty(value = "内容")
        private String content;
        @ApiModelProperty(value = "文章来源")
        private String source;
        @ApiModelProperty(value = "是否置顶（0：未置顶；1：已置顶）")
        private Integer isStick;

        public Integer getIsStick() {
            return isStick;
        }

        public void setIsStick(Integer isStick) {
            this.isStick = isStick;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(String createBy) {
            this.createBy = createBy;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

    public Integer getCountNum() {
        return countNum;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public List<FlashNews> getFlashNewsList() {
        return flashNewsList;
    }

    public void setFlashNewsList(List<FlashNews> flashNewsList) {
        this.flashNewsList = flashNewsList;
    }
}
