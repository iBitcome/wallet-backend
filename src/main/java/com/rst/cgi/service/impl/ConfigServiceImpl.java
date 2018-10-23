package com.rst.cgi.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rst.cgi.common.constant.Urls;
import com.rst.cgi.common.utils.BeanCopier;
import com.rst.cgi.common.utils.HttpService;
import com.rst.cgi.common.utils.OkHttpUtil;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mysql.ConfigDao;
import com.rst.cgi.data.dto.response.AppVersionRepDTO;
import com.rst.cgi.data.dto.response.ExConfigRepDTO;
import com.rst.cgi.data.dto.response.OperationMsgRepDTO;
import com.rst.cgi.data.entity.AppVersionRecord;
import com.rst.cgi.data.entity.ExConfig;
import com.rst.cgi.data.entity.OperationMessage;
import com.rst.cgi.service.ConfigService;
import com.rst.cgi.service.thrift.gen.pushserver.PushService;
import com.rst.thrift.export.ThriftClient;
import net.sf.json.JSONObject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ConfigServiceImpl implements ConfigService {
    private final Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
    @Autowired
    private ThriftClient<PushService.Iface> pushService;
    @Autowired
    private ConfigDao configDao;

    @Override
    public void registerPush(String equipmentNo, String deviceToken){
        try {
            pushService.get(PushService.Iface.class).iOSDeviceArrived(deviceToken, equipmentNo);
        } catch (TException e) {
            logger.error("IOS registerPush error");
            e.printStackTrace();
        }
    }

    @Override
    public List<AppVersionRepDTO> getAppPublishRecord(String channel) {
        List<AppVersionRecord> versionRecordList = configDao.getVersionRecordByClient(channel);

        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<AppVersionRepDTO> repDTOS = new ArrayList<>();
        if (!versionRecordList.isEmpty()) {
            versionRecordList.forEach(appVersionRecord -> {
                AppVersionRepDTO versionRepDTO = new AppVersionRepDTO();
                BeanCopier.getInstance().copyBean(appVersionRecord, versionRepDTO);
                versionRepDTO.setPublishedTime(spf.format(appVersionRecord.getPublishTime()));
                repDTOS.add(versionRepDTO);
            });
            return repDTOS;
        }
        return new ArrayList<>();
    }


    @Override
    public List<OperationMsgRepDTO> getOperationMsg(Map<String, Integer> body) {
        Integer id = body.get("id");

        List<OperationMessage> operationMessageList = new ArrayList<>();
        if (id != null) {
            operationMessageList.add(configDao.getOperationMsgById(id));
        } else {
            Integer messageType = body.get("messageType");
            Integer lang = body.get("lang");
            Integer num = body.get("num");
            if (num != null) {
                operationMessageList = configDao.getOperationMsgByTypeAndNum(messageType, lang, num);
            } else {
                operationMessageList = configDao.getOperationMsgByType(messageType, lang);
            }
        }

        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<OperationMsgRepDTO> repDTOS = new ArrayList<>();
        if (!operationMessageList.isEmpty()) {
            operationMessageList.forEach(operationMessage -> {
                OperationMsgRepDTO message = new OperationMsgRepDTO();
                BeanCopier.getInstance().copyBean(operationMessage, message);
                message.setStartTime(spf.format(operationMessage.getValidStartTime()));
                message.setEndTime(spf.format(operationMessage.getValidEndTime()));
                repDTOS.add(message);
            });
        }

        return repDTOS;
    }


    @Autowired
    private HttpService httpService;
    @Override
    public ExConfigRepDTO exConfig() {
        JSONObject rstJson = httpService.httpGet(Urls.GET_GATEWAY_EXCONFIG,null);
        logger.info("【GATEWAY】 exConfig response:{}",rstJson);
        if (rstJson.getInt("code")!= 200) {
            CustomException.response(-1, rstJson.getString("msg"));
        }

        ExConfigRepDTO repDTO = new ExConfigRepDTO();
        ExConfig exConfig = new ExConfig();
        exConfig = new Gson().fromJson(rstJson.getString("data"), exConfig.getClass());
        BeanCopier.getInstance().copyBean(exConfig, repDTO);
        return repDTO;
    }
}
