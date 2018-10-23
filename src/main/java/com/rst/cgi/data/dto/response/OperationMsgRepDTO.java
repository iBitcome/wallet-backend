package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class OperationMsgRepDTO extends Readable {
    private Integer id;
    @ApiModelProperty(value = "标题")
    private String title;
    @ApiModelProperty(value = "消息封面连接")
    private String cover;
    @ApiModelProperty(value = "活动链接")
    private String link;
    @ApiModelProperty(value = "消息正文")
    private String content;
    @ApiModelProperty(value = "消息分类,0: 活动， 1: 公告")
    private Integer category;
    @ApiModelProperty(value = "活动开始时间")
    private String startTime;
    @ApiModelProperty(value = "活动结束时间")
    private String endTime;
    @ApiModelProperty(value = "点击总数")
    private Integer clickCount;
    @ApiModelProperty(value = "1：活动推送，2：邀请码内页，3：Banner")
    private Integer messageType;
}
