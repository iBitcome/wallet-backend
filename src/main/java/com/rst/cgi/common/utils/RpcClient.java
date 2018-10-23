package com.rst.cgi.common.utils;

import com.rst.cgi.common.enums.CoinType;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RpcClient {
    public static final String BCH = "BCH";
    public static final String BTC = "BTC";
    public static final String GATEWAY = "GATEWAY";
    public static final String ZCASH = "ZEC";
    @Autowired
    private  HttpService httpService;

    @Value("${btc.socket}")
    private  String BTC_SOCKET;
    @Value("${btc.user}")
    private  String BTC_USER;
    @Value("${btc.pwd}")
    private  String BTC_PWD;

    @Value("${bch.socket}")
    private  String BCH_SOCKET;
    @Value("${bch.user}")
    private  String BCH_USER;
    @Value("${bch.pwd}")
    private  String BCH_PWD;

    @Value("${whc.socket}")
    private  String WHC_SOCKET;
    @Value("${whc.user}")
    private  String WHC_USER;
    @Value("${whc.pwd}")
    private  String WHC_PWD;

    @Value("${usdt.socket}")
    private  String USDT_SOCKET;
    @Value("${usdt.user}")
    private  String USDT_USER;
    @Value("${usdt.pwd}")
    private  String USDT_PWD;

    @Value("${zcash.socket}")
    private  String ZCASH_SOCKET;
    @Value("${zcash.user}")
    private  String ZCASH_USER;
    @Value("${zcash.pwd}")
    private  String ZCASH_PWD;


    public  JSONObject query(String stamp, String method, Object params){
        StringBuilder urlF = new StringBuilder().append("http://");
        if (BTC.equalsIgnoreCase(stamp)) {
            urlF.append(BTC_USER).append(":").append(BTC_PWD).append("@").append(BTC_SOCKET);
        } else if (BCH.equalsIgnoreCase(stamp)) {
            urlF.append(BCH_USER).append(":").append(BCH_PWD).append("@").append(BCH_SOCKET);
        } else if (CoinType.WHC.getName().equalsIgnoreCase(stamp)) {
            urlF.append(WHC_USER).append(":").append(WHC_PWD).append("@").append(WHC_SOCKET);
        } else if (CoinType.usdt.getName().equalsIgnoreCase(stamp)) {
            urlF.append(USDT_USER).append(":").append(USDT_PWD).append("@").append(USDT_SOCKET);
        } else if (CoinType.ZEC.getName().equalsIgnoreCase(stamp)){
            urlF.append(ZCASH_USER).append(":").append(ZCASH_PWD).append("@").append(ZCASH_SOCKET);
        }

        JSONObject requestJsobj = new JSONObject();
        requestJsobj.put("method", method);
        requestJsobj.put("params", params);
        requestJsobj.put("contentType", "text/plain;charset=UTF-8");
        requestJsobj.put("id", "1");

        return  httpService.httpPostWithJson(urlF.toString(), requestJsobj);
    }
}
