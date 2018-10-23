package com.rst.cgi.common.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by jiaoweiwei on 2017/4/8.
 */
@Component
public class ServerMainUris {
    public static String BSM_SERVER;
    public static String ETH_PRICE;
    public static String HUO_BI_PRO;
    public static String JUHE;
    public static String EXCHANGE_BASE_URL;
    public static String EOS_NODE_URL;
    public static String CGI_PRIVATE_KEY;
    public static String CGI_NAME;
    public static String GATEWAY_URL;
    public static String MAILGUN_URL;


    @Value("${server.bsm.uri}")
    public void setDataServer(String str) {
        ServerMainUris.BSM_SERVER = str;
    }
    @Value("${eth.price.url")
    public  void setEthPrice(String ethPrice) {
        ETH_PRICE = ethPrice;
    }
    @Value("${token.price.url}")
    public  void setHuoBiPro(String huoBiPro) {
        HUO_BI_PRO = huoBiPro;
    }
    @Value("${juhe.url:xxxx}")
    public  void setJUHE(String juhe) {
        JUHE = juhe;
    }
    @Value("${third.exchange.url}")
    public  void setExchangeBaseUrl(String exchangeBaseUrl) {
        EXCHANGE_BASE_URL = exchangeBaseUrl;
    }
    @Value("${eos.node.url}")
    public void setEOSnodeUrl(String url){EOS_NODE_URL=url;}

    @Value("{cgi.private-key}")
    public void setCgiPrivateKey(String privateKey){CGI_PRIVATE_KEY=privateKey;}

    @Value("${cgi.server-name}")
    public void setCgiName(String cgiName){CGI_NAME=cgiName;}

    @Value("${gateway.url}")
    public void setGatewayUrl(String url){
        GATEWAY_URL = url;
    }

    @Value("${mailgun.url}")
    public void setMailgunUrl(String url) {
        MAILGUN_URL = url;
    }
}
