package com.rst.cgi.service.impl;

import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.constant.Urls;
import com.rst.cgi.common.enums.CoinType;
import com.rst.cgi.common.utils.HttpService;
import com.rst.cgi.common.utils.OkHttpUtil;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mongo.TokenRepository;
import com.rst.cgi.data.dto.request.DexPlaceOrderDTO;
import com.rst.cgi.data.entity.Token;
import com.rst.cgi.service.ThirdService;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 第三方请求数据相关，参考文档：
 * http://47.98.167.79:8026/quantity/tucoin_server/wikis/%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3
 *
 * dex相关接口参考文档：https://github.com/dexDev/dexAPI
 * @author huangxiaolin
 * @date 2018-04-20 下午2:26
 */
@Service
public class ThirdServiceImpl implements ThirdService {

    private final Logger logger = LoggerFactory.getLogger(ThirdServiceImpl.class);


    private final String DEX_AUTH_HEAD = "Authorization";

    @Value("${token.name.price:#{null}}")
    private String namePriceStrs;
    @Value("${third.cache.time:30}")
    private int cacheTime;
    @Value("${third.exchange.name:feixiaohao}")
    private String exchangeName;

    @Value("${third.dex.url}")
    private String dexBaseUrl;
    @Value("${third.dex.username}")
    private String dexUsername;
    @Value("${third.dex.password}")
    private String dexPassword;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private HttpService httpService;
    @Autowired
    private TokenRepository tokenRepository;

    @Override
    public double getUSDByToken(String token) {
        double price = -1;
        if (StringUtils.isEmpty(token)) {
            return price;
        }

        Token currentToken = tokenRepository.findByName(token);
        String realTokenName = token;
        if (CoinType.WHC.getName().equalsIgnoreCase(token)) {
            token = CoinType.BCH.getName();
        } else if (CoinType.usdt.getName().equalsIgnoreCase(token)) {
            //如果代币是usdt则直接返回1美元的价格
            return 1d;
        } else if (currentToken != null && currentToken.getAliasCode() != null) {
            //副本代币价格直接获取原本代币的价格
            Token byAliasToken = tokenRepository.findByTokenCode(currentToken.getAliasCode());
            token = byAliasToken.getName();
        }
        //获取并返回配置中的测试代币价格
        if (StringUtils.isNotEmpty(namePriceStrs)) {
            String[] namePriceStrArray = namePriceStrs.trim().split(";");
            for (int i = 0; i < namePriceStrArray.length; i++) {
                String[] namePrice = namePriceStrArray[i].split(",");
                if (token.equalsIgnoreCase(namePrice[0])) {
                    return Double.parseDouble(namePrice[1]);
                }
            }
        }
        final String currentSymbol = token + "/USDT";
        //先从redis缓存获取数据
        String value = stringRedisTemplate.opsForValue().get(Constant.TOKEN_THRID_KEY);
        if (StringUtils.isNotEmpty(value)) {
            JSONArray jsonArray = JSONArray.fromObject(value);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                if (currentSymbol.equals(jsonData.getString("symbol"))) {
                    //logger.info("从缓存中获取代币价格：{}={}", currentSymbol, jsonData.getDouble("price"));
                    price = jsonData.getDouble("price");
                    if (CoinType.WHC.getName().equalsIgnoreCase(realTokenName)) {
                        price = price / 100.0;
                    }
                    return price;
                }
            }
            logger.warn("未找到代币 {} 对应的USDT价格", token);

            return price;
        }
        //最后从第三方请求数据
        Map<String, Object> param = new HashMap<>();
        param.put("trade_market", Constant.TOKEN_EXCHANGES);
        param.put("symbol_str", getSupportTokenParam());
        JSONObject jsonObject = httpService.postJSONForResult(Urls.EXCHANGE_PRICE_URL,
                null, param, true);
        if (jsonObject.getInt(Constant.CODE_KEY) == Constant.SUCCESS_CODE) {
            JSONArray jsonArray = jsonObject.getJSONArray(Constant.MESSAGE_KEY);
            int size = (jsonArray == null) ? 0 : jsonArray.size();
            List<Map<String, Object>> dataList = new ArrayList<>(size);
            double tokePrice = -1;
            JSONObject jsonData = null;
            for (int i = 0; i < size; i++) {
                try {
                    jsonData = jsonArray.getJSONObject(i);//返回的数据有时不是json格式
                } catch (JSONException ex) {
                    logger.warn("错误的代币格式：{}", jsonArray.getString(i));
                    continue;//非json格式直接进行下次循环
                }
                String priceStr = jsonData.getString("price");
                //价格可能为空，"null"
                if (StringUtils.isEmpty(priceStr) || "null".equals(priceStr)) {
                    continue;
                }
                String symbolStr = jsonData.getString("symbol");
                tokePrice = Double.valueOf(priceStr);
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("symbol", symbolStr);
                dataMap.put("price", tokePrice);
                dataMap.put("trade_market", jsonData.getString("trade_market"));
                dataList.add(dataMap);
                if ((price == -1) && currentSymbol.equals(symbolStr)) {
                    price = tokePrice;
                }
            }
            //放入redis缓存
            if (!CollectionUtils.isEmpty(dataList)) {
                String jsonStr = JSONArray.fromObject(dataList).toString();
                stringRedisTemplate.opsForValue().set(Constant.TOKEN_THRID_KEY, jsonStr);
//                test(dataList);
            }
        }
        if (price == -1) {
            logger.warn("未找到代币 {} 对应的USDT价格", token);
        }
        return price;
    }

    @Override
    public Map<String, Double> getUSDByTokens(List<String> tokenList) {
        Map<String, Double> resultMap = new HashMap<>();
        double price = -1;
        List<String> currentSymbolList = new ArrayList<>();
        if (tokenList != null) {
            tokenList.forEach(s -> {
                currentSymbolList.add(s + "/USDT");
            });

        }

        //先从redis缓存获取数据
        String value = stringRedisTemplate.opsForValue().get(Constant.TOKEN_THRID_KEY);
        if (StringUtils.isNotEmpty(value)) {
            JSONArray jsonArray = JSONArray.fromObject(value);
            if (currentSymbolList.isEmpty()) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    String token = jsonData.getString("symbol").split("/")[0];
                    resultMap.put(token, jsonData.getDouble("price"));
                }
            } else {
                currentSymbolList.forEach(s -> {
                    String token = s.split("/")[0];
                    boolean isFind = false;
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jsonData = jsonArray.getJSONObject(i);
                        if (s.equals(jsonData.getString("symbol"))) {
                            resultMap.put(token, jsonData.getDouble("price"));
                            isFind = true;
                            break;
                        }
                    }
                    if (!isFind) {
                        logger.warn("未找到代币 {} 对应的USDT价格", token);
                        resultMap.put(token, price);
                    }

                });
            }

        }
        return resultMap;
    }


    //仅测试
    private void test(List<Map<String, Object>> dataList) {
        List<Token> tokenList = tokenRepository.findAll();
        int count = 0;
        for (Token token : tokenList) {
            boolean isExist = false;
            for (Map<String, Object> map : dataList) {
                if ((token.getName()+"/USDT").equals(map.get("symbol"))) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                count++;
                logger.warn("未找到代币 {} 对应的USDT价格", token.getName());
            }
        }
        logger.warn("=============={}种代币未找到==========", count);
    }

    /**
     * 请求数据的代币参数
     * @author hxl
     * 2018/5/28 下午6:44
     */
    @Override
    public String getSupportTokenParam() {
        //查询所有的代币
       /* List<Token> tokenList = tokenRepository.findAll();
        if (CollectionUtils.isEmpty(tokenList)) {
            CustomException.response(Error.TOKEN_PAIR_NOT_EMPTY);
        }
        //拼接查询参数
        StringBuilder tokenParam = new StringBuilder();
        for (Token token : tokenList) {
            tokenParam.append("|")
                    .append(token.getName())
                    .append("/")
                    .append(Constant.USDT_TOKEN);
        }
        return tokenParam.substring(1);*/

        JSONObject result = JSONObject.fromObject(OkHttpUtil.http(Urls.GET_EXCHANGE_SYMBOLS).get());
        JSONArray symbols = result.getJSONArray(exchangeName);

        StringBuilder strSymbols = new StringBuilder();
        for (int i = 0; i < symbols.size(); i++) {
            if (symbols.getString(i).split("/")[1].equalsIgnoreCase("USDT")) {
                strSymbols.append("|")
                        .append(symbols.get(i));
            }
        }
        return strSymbols.substring(1);
    }


    @Override
    public JSONObject getDexPlaceOrder(DexPlaceOrderDTO placeOrderDto) {
        Map<String, Object> param = new HashMap<>();
        param.put("amout", placeOrderDto.getAmount());
        param.put("price", placeOrderDto.getPrice());
        param.put("action", placeOrderDto.getAction());
        param.put("nonce", placeOrderDto.getNonce());
        param.put("expireTimeSec", placeOrderDto.getExpireTimeSec());
        param.put("sig", placeOrderDto.getSig());
        param.put("pairId", placeOrderDto.getPairId());

        JSONObject jsonObject = httpService.postJSON(dexBaseUrl + "/v1/placeorder", getDexAuthHead(), param);

        return null;
    }

    @Override
    public JSONObject dexCancelOrder(DexPlaceOrderDTO placeOrderDto) {
        Map<String, Object> param = new HashMap<>();
        param.put("orderId", placeOrderDto.getOrderId());
        param.put("nonce", placeOrderDto.getNonce());
        param.put("pairId", placeOrderDto.getPairId());

        JSONObject jsonObject = httpService.postJSON(dexBaseUrl + "/v1/cancelorder", getDexAuthHead(), param);

        return null;
    }


    @Override
    public JSONObject dexCancelAllOrders(DexPlaceOrderDTO placeOrderDto) {
        return null;
    }

    @Override
    public JSONObject getDexWithdraw(String traderAddr, String token, String amount) {
        Map<String, Object> param = new HashMap<>();
        param.put("tokenId", token);
        param.put("amount", amount);
        JSONObject jsonObject = httpService.postJSON(dexBaseUrl + "/v1/withdraw/"+traderAddr, getDexAuthHead(), param);

        return null;
    }

    @Override
    public JSONObject getDexMaket() {
        JSONObject jsonObject = httpService.doGet(dexBaseUrl + Urls.DEX_MARKET_URL, null);
        return jsonObject;
    }

    @Override
    public JSONObject getDexPairs(String tokenId) {
        if (StringUtils.isEmpty(tokenId)) {
            CustomException.response(Error.TOKEN_NOT_EMPTY);
        }
        JSONObject jsonObject = httpService.doGet(dexBaseUrl + Urls.DEX_PAIRS_URL + tokenId, null);

        return jsonObject;
    }

    @Override
    public JSONObject getDexPairInfo(String pairId) {
        if (StringUtils.isEmpty(pairId)) {
            CustomException.response(Error.TOKEN_NOT_EMPTY);
        }
        JSONObject jsonObject = httpService.doGet(dexBaseUrl + Urls.DEX_PAIR_INFO_URL + pairId, null);

        return jsonObject;
    }

    @Override
    public JSONObject getDexTradeHistory(String pairId, int size) {
        if (StringUtils.isEmpty(pairId)) {
            CustomException.response(Error.TOKEN_NOT_EMPTY);
        }
        if (size < 1) {
            size = 1;
        }
        String url = dexBaseUrl + "/v1/tradehistory/"+pairId+"/"+size;
        JSONObject jsonObject = httpService.doGet(url, null);

        return jsonObject;
    }

    @Override
    public JSONObject getDexPairDepth(String pairId, int size) {
        if (StringUtils.isEmpty(pairId)) {
            CustomException.response(Error.TOKEN_NOT_EMPTY);
        }
        if (size < 1) {
            size = 1;
        }
        String url = dexBaseUrl + "/v1/depth/"+pairId+"/"+size;
        JSONObject jsonObject = httpService.doGet(url, null);

        return jsonObject;
    }

    private String dexToken = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJTdGRDbGFpbXMiOnsiYXVkIj" +
            "oiREV4IFNlcnZlcnMiLCJleHAiOjE1MjYwMjQ5MzAsImp0aSI6IjE5MjcxNGUxLTM0MWYtNGE5Ny1hOTEzLTI0NWJjN2R" +
            "hZTk3OCIsImlhdCI6MTUyNTc2NTczMCwiaXNzIjoiREV4Iiwic3ViIjoiNTcifX0.8wv9rahTMs8okqLji_DYsQCEhEsO" +
            "xF0USjJyGOTANk2IER_7YCfueT7sGi-41U-nrcKcyBUreD7U6bhqZgygFA";

    @Override
    public String getDexAuthToken(String email, String password) {
        Map<String, Object> param = new HashMap<>();
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) {
            param.put("email", dexUsername);
            param.put("password", dexPassword);
        } else {
            param.put("email", email);
            param.put("password", password);
        }
        JSONObject jsonObject = httpService.postJSON(dexBaseUrl + "/v1/authenticate", param);
        return jsonObject.getString("token");
    }

    @Override
    public String getDexBalances(String traderAddr) {
        if (StringUtils.isEmpty(traderAddr)) {
            CustomException.response(Error.TOKEN_NOT_EMPTY);
        }
        JSONObject jsonObject = httpService.doGet(dexBaseUrl + "/v1/balances/"+traderAddr, getDexAuthHead(), null);

        return "";
    }

    @Override
    public JSONObject getDexActiveOrders(String walletAddr, String pairId, int size, int page) {
        if (StringUtils.isEmpty(walletAddr) || StringUtils.isEmpty(pairId)) {
            CustomException.response(Error.TOKEN_NOT_EMPTY);
        }
        if (size < 0) {
            size = 1;
        }
        if (page < 0) {
            page = 1;
        }
        String url = dexBaseUrl + "/v1/activeorders/"+walletAddr+"/"+pairId+"/"+size+"/"+page;
        JSONObject jsonObject = httpService.doGet(url, getDexAuthHead(), null);

        return null;
    }

    @Override
    public JSONObject getDexPastOrders(String walletAddr, String pairId, int size, int page) {
        if (StringUtils.isEmpty(walletAddr) || StringUtils.isEmpty(pairId)) {
            CustomException.response(Error.TOKEN_NOT_EMPTY);
        }
        if (size < 0) {
            size = 1;
        }
        if (page < 0) {
            page = 1;
        }
        String url = dexBaseUrl + "/v1/pastorders/"+walletAddr+"/"+pairId+"/"+size+"/"+page;
        JSONObject jsonObject = httpService.doGet(url, getDexAuthHead(), null);

        return null;
    }

    @Override
    public JSONObject getDexOrderById(String orderId) {
        String url = dexBaseUrl + "/v1/orderbyid/"+orderId;
        JSONObject jsonObject = httpService.doGet(url, getDexAuthHead(), null);

        return null;
    }

    @Override
    public JSONObject getDexGrades(String walletAddr, String pairId, int size) {
        String url = dexBaseUrl + "/v1/trades/"+walletAddr+"/"+pairId+"/"+size;
        JSONObject jsonObject = httpService.doGet(url, getDexAuthHead(), null);

        return null;
    }

    /**
     * 获取dex认证头
     * @author huangxiaolin
     * @date 2018-05-08 16:58
     */
    private Map<String, String> getDexAuthHead() {
        Map<String, String> headMap = new HashMap<>(1);
        headMap.put(DEX_AUTH_HEAD, getDexAuthToken(null, null));
        return headMap;
    }
}
