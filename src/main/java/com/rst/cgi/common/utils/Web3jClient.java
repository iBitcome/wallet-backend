package com.rst.cgi.common.utils;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.controller.interceptor.CustomException;
import net.sf.json.JSONObject;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.http.HttpService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class Web3jClient {

    @Value("${eth.node.url}")
    private  String ethNodeUrl;
    Web3jService web3jService = null;
    Web3j web3j = null;

    public  JSONObject ask(List<?> params, String method){
        if (web3jService == null) {
            web3jService = new HttpService(ethNodeUrl);
        }

        Object res = null;
        try {
            if (params == null || params.isEmpty()) {
                params = Collections.emptyList();
            }
            Request<?, Response>  request = new Request<>(
                    method,
                    params,
                    web3jService,
                    Response.class);
            Response response = request.send();
            res = response.getResult();
        } catch (IOException e) {
            e.printStackTrace();
            CustomException.response(Error.SERVER_EXCEPTION);
        }
        return JSONObject.fromObject(res);
    }




    public Web3j getWeb3j () {
        if (web3j == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            web3j = Web3j.build(new HttpService(ethNodeUrl, client, false));
        }
        return web3j;
    }
}
