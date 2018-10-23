package com.rst.cgi.data.dto.response;

import com.rst.cgi.data.dto.Readable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetTokensNewRepDTO extends Readable{
    @ApiModelProperty("当前页码")
    private Integer pageNo = 1;
    @ApiModelProperty("总页数")
    private Integer pageTotal = 0;
    private List<TokensRepDTO> content = new ArrayList<>();
}
