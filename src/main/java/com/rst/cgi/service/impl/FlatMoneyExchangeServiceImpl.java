package com.rst.cgi.service.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rst.cgi.common.utils.OkHttpUtil;
import com.rst.cgi.service.FlatMoneyExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Service
public class FlatMoneyExchangeServiceImpl implements FlatMoneyExchangeService {
    private final Logger logger = LoggerFactory.getLogger(FlatMoneyExchangeService.class);
    private static final String USD_CNY_URL =
            "http://webforex.hermes.hexun.com/forex/quotelist?code=FOREXUSDCNY&column=Code,Price";

    private static final String RATE_EXCHANGE_URL =
            "";
    private static final String FETCH_MONEY_MAP_URL =
            "";

    public static final String PROPERTY_LIST = "list";
    public static final String PROPERTY_RESULT = "result";
    public static final String PROPERTY_ERR_CODE = "error_code";


    private Map<String, String> rateMap = new HashMap<>();
    private HashMap<String, String> moneyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        rateMap.put("USD-USD", "1");
        updateMoneyMap();
        updateUSDCNYPrice();
        updateExchangeRate();
    }

    private void updateMoneyMap() {
        String result = OkHttpUtil.http(RATE_EXCHANGE_URL).get();
        JsonObject data = new Gson().fromJson(result, JsonObject.class);
        if (data.has(PROPERTY_ERR_CODE) && data.get(PROPERTY_ERR_CODE).getAsInt() != 0) {
            logger.info("updateExchangeRate failed:{}", result);
            return;
        }

        if (data.has(PROPERTY_RESULT)) {
            JsonObject resultData = data.getAsJsonObject(PROPERTY_RESULT);
            if (resultData.has(PROPERTY_LIST)) {
                JsonArray listData = resultData.getAsJsonArray(PROPERTY_LIST);
                for (int i = 0; i < listData.size(); i++) {
                    JsonArray moneyData = listData.get(i).getAsJsonArray();
                    if (moneyData.size() < 2) {
                        continue;
                    }
                    moneyMap.put(moneyData.get(0).getAsString(), moneyData.get(1).getAsString());
                }
            }
        }
    }

    //每小时
    @Scheduled(cron = "0 0 */1 * * ?")
    private String updateUSDCNYPrice() {
        String result = OkHttpUtil.http(USD_CNY_URL).get();
        int start = result.indexOf("{");
        int end = result.lastIndexOf("}");
        JsonObject data = new Gson().fromJson(result.substring(start, end+1), JsonObject.class);
        String price = data.getAsJsonArray("Data")
                           .get(0).getAsJsonArray()
                           .get(0).getAsJsonArray()
                           .get(1).getAsString();

        Double priceValue = (Double.parseDouble(price) / 10000);
        if (priceValue > 0) {
            rateMap.put("USD-CNY", "" + priceValue);
        }

        return price;
    }

    //每小时
    @Scheduled(cron = "0 0 */1 * * ?")
    private void updateExchangeRate() {
        String result = OkHttpUtil.http(RATE_EXCHANGE_URL).get();
        JsonObject data = new Gson().fromJson(result, JsonObject.class);
        if (data.has(PROPERTY_ERR_CODE) && data.get(PROPERTY_ERR_CODE).getAsInt() != 0) {
            logger.info("updateExchangeRate failed:{}", result);
            return;
        }

        if (data.has(PROPERTY_RESULT)) {
            JsonObject resultData = data.getAsJsonObject(PROPERTY_RESULT);
            if (resultData.has(PROPERTY_LIST)) {
                JsonArray listData = resultData.getAsJsonArray(PROPERTY_LIST);
                for (int i = 0; i < listData.size(); i++) {
                    JsonArray rateData = listData.get(i).getAsJsonArray();
                    if (rateData.size() < 6) {
                        continue;
                    }

                    String moneyName = rateData.get(0).getAsString();
                    String base = rateData.get(1).getAsString();
                    String rate = rateData.get(5).getAsString();

                    Double priceValue = (Double.parseDouble(rate) / Double.parseDouble(base));

                    if (priceValue > 0) {
                        String moneyId = moneyMap.get(moneyName);
                        if (!StringUtils.isEmpty(moneyId)) {
                            rateMap.put(moneyId + "CNY", "" + priceValue);
                        }
                    }

                }
            }
        }
    }

    @Override
    public String getRate(String base, String quote) {
        String key = base + "-" + quote;
        return rateMap.get(key);
    }

    @Override
    public List<String> getSupportQuotes(String base) {
        return rateMap.keySet()
                      .stream()
                      .filter(key -> key.startsWith(base))
                      .map(key -> key.substring(base.length()+1))
                      .collect(Collectors.toList());
    }
}
