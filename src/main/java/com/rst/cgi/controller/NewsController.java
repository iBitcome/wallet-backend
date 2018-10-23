package com.rst.cgi.controller;

import com.rst.cgi.common.utils.BeanCopier;
import com.rst.cgi.conf.UeditorConfig;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.request.FlashNewsReqDTO;
import com.rst.cgi.data.dto.request.GetNewsReqDTO;
import com.rst.cgi.data.dto.response.FlashNewsRepDTO;
import com.rst.cgi.data.dto.response.GetNewsOldRepDTO;
import com.rst.cgi.data.dto.response.GetNewsRepDTO;
import com.rst.cgi.data.dto.response.WalletAssistantResDTO;
import com.rst.cgi.service.NewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mtb on 2018/3/27.
 */
@Api(tags = "行业新闻相关")
@RestController
@RequestMapping("/mobile/news")
public class NewsController {

    @Autowired
    private NewsService newsService;
    @Autowired
    private UeditorConfig ueditorConfig;
    
//    @ApiOperation(value = "获取行业新闻")
//    @PostMapping("/getNews")
//    public CommonResult<List<GetNewsOldRepDTO>> getNews(@RequestBody GetNewsReqDTO body){
//        CommonResult<List<GetNewsOldRepDTO>> rst = new CommonResult<>();
//
//        GetNewsRepDTO getNewsRepDTO = newsService.getNews(body);
//        List<GetNewsOldRepDTO> getNewsOldRepDTOS = new ArrayList<>();
//        getNewsRepDTO.getNewsInfoList().forEach(newsInfo -> {
//            GetNewsOldRepDTO getNewsOld = new GetNewsOldRepDTO();
//            BeanCopier.getInstance().copyBean(newsInfo, getNewsOld);
//            getNewsOld.setNewsCount(getNewsRepDTO.getNewsCount());
//            getNewsOldRepDTOS.add(getNewsOld);
//        });
//        rst.setData(getNewsOldRepDTOS);
//        return rst;
//    }

    @ApiOperation(value = "获取行业新闻")
    @PostMapping("/getNews")
    public CommonResult<GetNewsRepDTO> getNews(@RequestBody GetNewsReqDTO body){
        CommonResult<GetNewsRepDTO> rst = new CommonResult<>();
        rst.setData(newsService.getNews(body));
        return rst;
    }

    /**
     * 前端ueditor初始化调用获取图片上传的配置接口
     * @author huangxiaolin
     * @date 2018-04-13 14:09
     */
    @ApiOperation(value = "前端ueditor初始化调用获取图片上传的配置接口")
    @GetMapping("/ueditor/getConfig")
    public String ueditorConfig(@RequestParam(required = false) String callback) {
        String configJSON = JSONObject.fromObject(ueditorConfig).toString();
        if (StringUtils.isEmpty(callback)) {
            return configJSON;
        } else {
            return callback + "(" + configJSON +")";
        }
    }


    @ApiOperation(value = "获取快讯列表或详情")
    @PostMapping("/getFlashNews")
    public CommonResult<FlashNewsRepDTO> getFlashNews(@RequestBody FlashNewsReqDTO body) {
        CommonResult<FlashNewsRepDTO> rst = new CommonResult<>();
        rst.setData(newsService.getFlashNews(body));
        return rst;
    }


    @ApiOperation(value = "查询钱包助理信息列表")
    @PostMapping("/getWalletAssistantMsg")
    public CommonResult<List<WalletAssistantResDTO>> getFlashNews() {
        CommonResult<List<WalletAssistantResDTO>> rst = new CommonResult<>();
        rst.setData(newsService.WalletAssistantMsg());
        return rst;
    }
}
