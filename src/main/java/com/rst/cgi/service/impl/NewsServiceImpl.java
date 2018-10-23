package com.rst.cgi.service.impl;

import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.constant.Urls;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.common.utils.HttpService;
import com.rst.cgi.data.dao.mysql.WalletAssistantDao;
import com.rst.cgi.data.dto.request.FlashNewsReqDTO;
import com.rst.cgi.data.dto.request.GetNewsReqDTO;
import com.rst.cgi.data.dto.response.FlashNewsRepDTO;
import com.rst.cgi.data.dto.response.GetNewsRepDTO;
import com.rst.cgi.data.dto.response.WalletAssistantResDTO;
import com.rst.cgi.data.entity.WalletAssistant;
import com.rst.cgi.service.NewsService;
import com.rst.cgi.service.thrift.gen.pushserver.PushService;
import com.rst.thrift.export.ThriftClient;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mtb on 2018/4/3.
 */
@Service
public class NewsServiceImpl implements NewsService {

    private final Logger logger = LoggerFactory.getLogger(NewsServiceImpl.class);

    @Autowired
    private HttpService httpService;
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;
    @Autowired
    private ThriftClient<PushService.Iface> pushserviceClient;
    @Autowired
    private WalletAssistantDao walletAssistantDao;


    @Value("${flashNewsTopic:flash_news_topic}")
    private String flashNewsTopic;
    /**
     * 初始化redis监听容器
     */
    @PostConstruct
    public void init() {
        redisMessageListenerContainer.addMessageListener(
                (msg, pattern) -> {
                    String msgStr = new String(msg.getBody());
                    JSONObject newsPushMsg = JSONObject.fromObject(msgStr);
                    try {
                        String msgContent = newsPushMsg.getString("title");
                        if (StringUtils.isBlank(msgContent)) {
                            msgContent = "";
                        }
                       String finalMsg =  msgContent.length() >= 15 ? msgContent.substring(0, 15) + "..." : msgContent;
                        pushserviceClient.get(PushService.Iface.class).pushToAll(newsPushMsg.getString("id"),
                                Constant.NEWS_FLASH,
                                finalMsg,
                                true);
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                }
                , new PatternTopic(flashNewsTopic));
    }


    @Override
    public GetNewsRepDTO getNews(GetNewsReqDTO body) {

        Map<String, Object> param = new HashMap<>();
        if (body.getId() != null) {
            param.put("id",body.getId());
        }
        param.put("order_way",body.getOrderWay());
        param.put("page",body.getPage());
        param.put("page_size",body.getPageSize());
        param.put("lang", CurrentThreadData.language());
        JSONObject obj = httpService.httpGet(Urls.BSM_NEWS_URL, param);
        if (Objects.isNull(obj) || obj.isEmpty()) {
            logger.error("BSM RETURN NEWS ERROR");
            CustomException.response(Error.ERR_MSG_SERVICE_ERROR);
        } else if (obj.getInt("code") != 0) {
            if (obj.containsKey("msg") && obj.getString("msg").contains("请求提交失败")) {
                CustomException.response(Error.ERR_MSG_REQUEST_FAIL);
            } else {
                CustomException.response(obj.getString("msg"));
            }
        }

        JSONArray data = obj.getJSONArray("data");
        GetNewsRepDTO newsRepDTO = new GetNewsRepDTO();
        newsRepDTO.setNewsCount(obj.getInt("news_count"));

        for (int i = 0; i<data.size(); i++) {
            GetNewsRepDTO.NewsInfo newsInfo = new GetNewsRepDTO.NewsInfo();
            newsInfo.setUpdateTime(data.getJSONObject(i).getString("update_time"));
            newsInfo.setTitle(data.getJSONObject(i).getString("title"));
            newsInfo.setSource(data.getJSONObject(i).getString("source"));
            newsInfo.setIsStick(data.getJSONObject(i).getInt("is_stick"));
            newsInfo.setId(data.getJSONObject(i).getInt("id"));
            newsInfo.setHits(data.getJSONObject(i).getInt("hits"));
            newsInfo.setCreateTime(data.getJSONObject(i).getString("create_time"));
            newsInfo.setCreateBy(data.getJSONObject(i).getString("create_by"));
            newsInfo.setCover(data.getJSONObject(i).getString("cover"));
            newsInfo.setContent(data.getJSONObject(i).getString("content"));
            newsRepDTO.getNewsInfoList().add(newsInfo);
        }
        return newsRepDTO;
    }

    @Override
    public FlashNewsRepDTO getFlashNews(FlashNewsReqDTO body) {
        SimpleDateFormat smpl = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
        JSONObject param = new JSONObject();

        if (Objects.nonNull(body.getId())) {
            param.put("id", body.getId());
        } else {
            if (!StringUtils.isBlank(body.getCreateBy())) {
                param.put("create_by", body.getCreateBy());
            }
            if (Objects.nonNull(body.getStartTime())) {
                param.put("start_time", smpl.format(body.getStartTime()));
            }
            if (Objects.nonNull(body.getEndTime())) {
                param.put("end_time", smpl.format(body.getEndTime()));
            }
            if (!StringUtils.isBlank(body.getSearch())) {
                param.put("search", body.getSearch());
            }
            param.put("order_way", body.getOrderWay());
            param.put("page", body.getPage());
            param.put("page_size", body.getPageSize());
        }
        JSONObject rst = httpService.httpGet(Urls.GET_FLASH_NEWS, param);
        if (rst.containsKey("code") && rst.getInt("code") < 0) {
            CustomException.response(Error.ERR_MSG_SERVICE_ERROR);
        }

        FlashNewsRepDTO flashNews = new FlashNewsRepDTO();
        flashNews.setCountNum(rst.getInt("count"));
        JSONArray data = rst.getJSONArray("data");
        for (int i = 0; i < data.size(); i++) {
            FlashNewsRepDTO.FlashNews flashNew = new FlashNewsRepDTO.FlashNews();
            JSONObject obj = data.getJSONObject(i);
            flashNew.setUrl(obj.getString("cover"));
            flashNew.setUpdateTime(obj.getString("update_time"));
            flashNew.setTitle(obj.getString("title"));
            flashNew.setId(obj.getInt("id"));
            flashNew.setCreateTime(obj.getString("create_time"));
            flashNew.setCreateBy(obj.getString("create_by"));
            flashNew.setContent(obj.getString("content"));
            flashNew.setSource(obj.getString("source"));
            flashNew.setIsStick(obj.getInt("is_stick"));
            flashNews.getFlashNewsList().add(flashNew);
        }


        return flashNews;
    }


    @Override
    public List<WalletAssistantResDTO> WalletAssistantMsg() {
        List<WalletAssistant> walletAssistantList = walletAssistantDao.findAssistantMsg();
        Map<String, List<WalletAssistant>> assistantTypeMap =  walletAssistantList.stream().
                collect(Collectors.groupingBy(WalletAssistant::getTypeCn));

        List<WalletAssistantResDTO> assistantResDTOList = new ArrayList<>();
        for (Map.Entry<String, List<WalletAssistant>> entry : assistantTypeMap.entrySet()) {
            List<WalletAssistant> assistantList = entry.getValue();

            List<WalletAssistantResDTO.QAndA> qAndAList = new ArrayList<>();
            assistantList.forEach(walletAssistant -> {
                WalletAssistantResDTO.QAndA qAndA = new WalletAssistantResDTO.QAndA();
                qAndA.setQuestionCN(walletAssistant.getQuestionCn());
                qAndA.setQuestionEN(walletAssistant.getQuestionEn());
                qAndA.setMsgSort(walletAssistant.getMsgSort());
                qAndA.setId(walletAssistant.getQuestionId());
                qAndA.setKeyWord(walletAssistant.getKeyWord());
                qAndAList.add(qAndA);
            });
            qAndAList.sort(WalletAssistantResDTO.qAndAOrder);

            WalletAssistant walletAssistant =  assistantList.get(0);
            WalletAssistantResDTO assistantResDTO = new WalletAssistantResDTO();
            assistantResDTO.setFirstTitleCN(walletAssistant.getTypeCn());
            assistantResDTO.setFirstTitleEN(walletAssistant.getTypeEn());
            assistantResDTO.setQAndAList(qAndAList);
            assistantResDTO.setTypeSort(walletAssistant.getTypeSort());
            assistantResDTOList.add(assistantResDTO);
        }
        assistantResDTOList.sort(WalletAssistantResDTO.firstTitleOrder);

        return assistantResDTOList;
    }

}
