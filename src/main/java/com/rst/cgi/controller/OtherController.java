package com.rst.cgi.controller;

import com.rst.cgi.common.hbc.DecryptRequest;
import com.rst.cgi.common.hbc.EncryptResponse;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.service.OtherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "其他相关接口")
@RestController
@RequestMapping(value = "/other")
public class OtherController {
    @Autowired
    private OtherService otherService;

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "微信分享参数签名",notes = "{url:当前网页的URL}")
    @RequestMapping(value = "/weixinSign", method = RequestMethod.POST)
    public CommonResult weixinSign(@RequestBody Map<String, String> body){
        CommonResult<Map<String, String>> rst = new CommonResult<>();
        rst.setData(otherService.weixinSign(body.get("url")));
        return rst;
    }
}
