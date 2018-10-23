package com.rst.cgi.controller;

import com.rst.cgi.common.hbc.DecryptRequest;
import com.rst.cgi.common.hbc.EncryptResponse;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.request.ActivityAddressReqDTO;
import com.rst.cgi.data.dto.response.GetDappRepDTO;
import com.rst.cgi.service.ActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "活动相关")
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "提交活动相关信息")
    @RequestMapping(value = "/submitActivityInfo", method = RequestMethod.POST)
    public CommonResult submitActivityInfo(@RequestBody ActivityAddressReqDTO body){
        CommonResult rst = new CommonResult<>();
        activityService.submitActivityInfo(body);
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取Dapp信息")
    @RequestMapping(value = "/GetDappInfo", method = RequestMethod.POST)
    public CommonResult<List<GetDappRepDTO>> GetDappInfo(){
        CommonResult<List<GetDappRepDTO>> rst = new CommonResult<>();
        rst.setData(activityService.GetDappInfo());
        return rst;
    }
}
