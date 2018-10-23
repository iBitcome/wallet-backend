package com.rst.cgi.service.impl;

import com.rst.cgi.common.utils.OkHttpUtil;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.service.OtherService;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OtherServiceImpl implements OtherService {

    final String GET_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
    final String GET_ASSCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    @Override
    public Map<String, String> weixinSign(String url) {
        Map<String, String> ret = new HashMap<>();
        String nonce_str = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + getJsapiTicket() +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = Hex.encodeHexString(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ret.put("url", url);
        ret.put("jsapi_ticket", getJsapiTicket());
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);
        ret.put("appId", AppID);

        return ret;
    }


    private String getJsapiTicket() {
        JSONObject jsonObject = JSONObject.fromObject(OkHttpUtil.http(GET_TICKET_URL)
                .param("access_token", getAccessToken())
                .param("type", "jsapi")
                .get());
        if (jsonObject.getInt("errcode") != 0) {
            CustomException.response(-1, jsonObject.getString("errmsg"));
        }
        return jsonObject.getString("ticket");
    }



    //线上
    private final String AppID = "wx27a5fbdfb6f8c507";
    private final String AppSecret = "e3868e1f48fa19e0840d0eee2ed41135";
    //测试
   /* private final String AppID = "wx6b5b6ffe9356bc9a";
    private final String AppSecret = "9539d9fcaf1522657ff976b1546aca87";*/
    private String getAccessToken() {
        JSONObject object = JSONObject.fromObject(OkHttpUtil.http(GET_ASSCESS_TOKEN_URL)
                .param("grant_type","client_credential")
                .param("appid", AppID)
                .param("secret", AppSecret)
                .get());
        if (object.containsKey("errcode")) {
            CustomException.response(-1, object.getString("errmsg"));
        }
        return object.getString("access_token");
    }

}
