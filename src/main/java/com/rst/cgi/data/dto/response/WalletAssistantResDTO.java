package com.rst.cgi.data.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class WalletAssistantResDTO extends Readable {

    @ApiModelProperty("一级标题(CN)")
    private String firstTitleCN;
    @ApiModelProperty("一级标题(EN)")
    private String firstTitleEN;
    @ApiModelProperty("一级标题排序")
    @JsonIgnore
    private int typeSort;
    @ApiModelProperty("问答列表")
    private List<QAndA> qAndAList;

    @Setter
    @Getter
    public static class QAndA {
        @ApiModelProperty("id")
        private Integer id;
        @ApiModelProperty("问答(CN)")
        private String questionCN;
        @ApiModelProperty("问答(EN)")
        private String questionEN;
        @ApiModelProperty("问答(EN)")
        private String keyWord;
        @ApiModelProperty("问答排序")
        @JsonIgnore
        private int msgSort;
    }




    public static Comparator<WalletAssistantResDTO> firstTitleOrder = (o1, o2) -> {
        if (o1.typeSort > o2.typeSort) {
            return 1;
        } else if (o1.typeSort == o2.typeSort) {
            return 0;
        } else {
            return -1;
        }
    };

    public static Comparator<QAndA> qAndAOrder = (o1, o2) -> {
        if (o1.msgSort > o2.msgSort) {
            return 1;
        } else if (o1.msgSort == o2.msgSort) {
            return 0;
        } else {
            return -1;
        }
    };
}
