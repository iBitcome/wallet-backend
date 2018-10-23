package com.rst.cgi.controller;
import com.rst.cgi.common.hbc.DecryptRequest;
import com.rst.cgi.common.hbc.EncryptResponse;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.request.ConfigReqDTO;
import com.rst.cgi.data.dto.request.RegisterPushReqDTO;
import com.rst.cgi.data.dto.response.AppVersionRepDTO;
import com.rst.cgi.data.dto.response.ExConfigRepDTO;
import com.rst.cgi.data.dto.response.OperationMsgRepDTO;
import com.rst.cgi.data.dto.response.VersionConfigResDTO;
import com.rst.cgi.service.ClientVersionService;
import com.rst.cgi.service.ConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by mtb on 2018/4/14.
 */
@Api(tags = "配置相关接口")
@RestController
@RequestMapping(value = "/mobile/config")
public class ConfigController {

    @Autowired
    private ClientVersionService clientVersionService;
    @Autowired
    private ConfigService configService;

    @EncryptResponse
    @ApiOperation(value = "版本提示信息")
    @RequestMapping(value = "/getVersionConfig", method = RequestMethod.POST)
    public CommonResult<VersionConfigResDTO> handleVersionConfig() {

        CommonResult<VersionConfigResDTO> res = new CommonResult<>();
        res.setData(clientVersionService.getVersionConfig());
        return res;
    }

    @ApiOperation(value = "设置客户端版本信息")
    @RequestMapping(value = "/setConfig", method = RequestMethod.POST)
    public CommonResult setConfig(@RequestBody ConfigReqDTO body) {
        CommonResult rst = new CommonResult<>();
        clientVersionService.updateConfigure(body.getVersionConfigure());
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "iOS APNS注册deviceToken")
    @RequestMapping(value = "/registerPush", method = RequestMethod.POST)
    public CommonResult registerPush(@RequestBody RegisterPushReqDTO body){
        CommonResult<String> rst = new CommonResult<>();
        configService.registerPush(body.getEquipmentNo(), body.getDeviceToken());
        rst.setOK();
        return rst;
    }


    @ApiOperation(value = "查询发版记录",notes = "参数说明：{channel:'app来源（ios/Android）'}")
    @RequestMapping(value = "/getAppPublishRecord", method = RequestMethod.POST)
    public CommonResult<List<AppVersionRepDTO>> getAppPublishRecord(@RequestBody Map<String, String> body){
        CommonResult<List<AppVersionRepDTO>> rst = new CommonResult<>();
        if (body != null) {
            rst.setData(configService.getAppPublishRecord(body.get("channel")));
        }

        return rst;
    }


    @ApiOperation(value = "获取运营配置信息",notes = "参数说明：{messageType:'信息类型（1-活动推送，2-趣味活动，3-banner）'，" +
            "lang:'语种选择（0-中文，1-英文）'，'num':查询数量（不传查询所有）,'id':信息唯一id}")
    @RequestMapping(value = "/getOperationMsg", method = RequestMethod.POST)
    public CommonResult<List<OperationMsgRepDTO>> getOperationMsg(@RequestBody Map<String, Integer> body){
        CommonResult<List<OperationMsgRepDTO>> rst = new CommonResult<>();
        if (body != null) {
            rst.setData(configService.getOperationMsg(body));
        }

        return rst;
    }



    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取铸币熔币业务的相关配置")
    @RequestMapping(value = "/exConfig", method = RequestMethod.POST)
    public CommonResult exConfig(){
        CommonResult<ExConfigRepDTO> rst = new CommonResult<>();
        rst.setData(configService.exConfig());
        return rst;
    }

}
