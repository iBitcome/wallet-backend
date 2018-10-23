package com.rst.cgi.service.exchange.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rst.cgi.common.utils.OkHttpUtil;
import com.rst.cgi.data.dto.Symbol;
import com.rst.cgi.data.entity.TradePoint;
import com.rst.cgi.service.FlatMoneyExchangeService;
import com.rst.cgi.service.exchange.MarketProvider;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@MarketProvider(value = "FeiXiaoHaoDataProvider", market = "FeiXiaoHao")
public class FeiXiaoHaoDataProvider extends AbstractMarketDataProvider {
    @Autowired
    private FlatMoneyExchangeService flatMoneyExchangeService;

    @Override
    public String fetchPrice(Symbol symbol) {
        long current = System.currentTimeMillis();
        String price = flatMoneyExchangeService.getRate("USD", symbol.getBaseAsset());
        if (!StringUtils.isEmpty(price)) {
            int cycleCount = 0;
            do {
                long timeInterval = 60*60*1000*(1+cycleCount*6);
                List<FXHPrice> fxhPrices =
                        fetchFXHPrice(symbol.getQuoteAsset(), current - timeInterval, current);
                if (!fxhPrices.isEmpty()) {
                    Double finalPrice =
                            Double.parseDouble(price)
                                    * Double.parseDouble(fxhPrices.get(fxhPrices.size() - 1).getPriceUSD());
                    return finalPrice+"";
                }
            } while (cycleCount < 5);
        }

        return null;
    }

    @Override
    public Set<Symbol> fetchSymbols() {
        List<FXHAsset> fxhAssets = fetchFXHAssets();
        Set<Symbol> symbols = new HashSet<>();
        fxhAssets.forEach(fxhAsset -> {
            nameToFXHAsset.put(fxhAsset.briefName, fxhAsset);

            flatMoneyExchangeService.getSupportQuotes("USD").forEach(
                    asset -> symbols.add(new Symbol(asset, fxhAsset.briefName)));
        });

        return symbols;
    }

    @Override
    public List<TradePoint> fetchTrades(Symbol symbol, Long startTime, Long endTime) {
        String price = flatMoneyExchangeService.getRate("USD", symbol.getBaseAsset());
        if (!StringUtils.isEmpty(price)) {
            List<FXHPrice> fxhPrices = fetchFXHPrice(symbol.getQuoteAsset(), startTime, endTime);
            return fxhPrices.stream()
                            .map(fxhPrice -> {
                                Double finalPrice =
                                        Double.parseDouble(price) * Double.parseDouble(fxhPrice.priceUSD);
                                Double finalVolume =
                                        Double.parseDouble(price) * Double.parseDouble(fxhPrice.volumeUSD);
                                return new TradePoint(fxhPrice.timestamp,finalPrice+"", finalVolume+"", 2);
                            })
                            .collect(Collectors.toList());
        }

        return null;
    }

    private BiMap<String, FXHAsset> nameToFXHAsset = HashBiMap.create();
    private static final String FETCH_SYMBOL_INFO_URL =
            "https://api.feixiaohao.com/search/relatedallword";
    private static final String FETCH_PRICE_INFO_URL =
            "https://api.feixiaohao.com/coinhisdata";

    private List<FXHAsset> fetchFXHAssets() {
        String result = OkHttpUtil.http(FETCH_SYMBOL_INFO_URL).get();

        JsonArray jsonArray = new Gson().fromJson(result, JsonArray.class);
        List<FXHAsset> datas = new ArrayList<>(jsonArray.size());
        jsonArray.forEach(jsonElement -> {
            FXHAsset fxhAsset = FXHAsset.from(jsonElement.getAsString());
            if (fxhAsset != null) {
                datas.add(fxhAsset);
            }
        });

        return datas;
    }

    private List<FXHPrice> fetchFXHPrice(String asset, long start, long end) {
        if (nameToFXHAsset.get(asset) == null) {
            return new ArrayList<>();
        }

        String result = OkHttpUtil.http(FETCH_PRICE_INFO_URL)
                                  .path(nameToFXHAsset.get(asset).getFullName())
                                  .path("" + start)
                                  .path("" + end)
                                  .get();
        JsonObject data = new Gson().fromJson(result, JsonObject.class);

        JsonArray marketCaps = data.getAsJsonArray("market_cap_by_available_supply");
        JsonArray priceUSDs = data.getAsJsonArray("price_usd");
        JsonArray priceBTCs = data.getAsJsonArray("price_btc");
        JsonArray volumeUSDs = data.getAsJsonArray("vol_usd");

        int size = marketCaps.size();
        List<FXHPrice> fxhPrices = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            FXHPrice price = new FXHPrice();
            JsonArray marketCap = marketCaps.get(i).getAsJsonArray();
            price.setMarketCap(marketCap.get(1).getAsString());
            price.setTimestamp(marketCap.get(0).getAsLong());
            price.setPriceBTC(priceBTCs.get(i).getAsJsonArray().get(1).getAsString());
            price.setPriceUSD(priceUSDs.get(i).getAsJsonArray().get(1).getAsString());
            price.setVolumeUSD(volumeUSDs.get(i).getAsJsonArray().get(1).getAsString());
            fxhPrices.add(price);
        }

        return fxhPrices;
    }

    @Override
    protected String getExchangeName() {
        return "FeiXiaoHao";
    }

    @Data
    private static class FXHPrice {
        private long timestamp;
        private String marketCap;
        private String priceUSD;
        private String priceBTC;
        private String volumeUSD;
    }

    /**
     * eg:0#bitcoin#Bitcoin#比特币#BTC#/coin/7033f2f2c2a16094bbb3bafc47205ba8_small.png
     */
    @Data
    private static class FXHAsset {
        private Integer type;
        private String fullName;
        private String officeName;
        private String chsName;
        private String briefName;

        static FXHAsset from(String data) {
            if (StringUtils.isEmpty(data)) {
                return null;
            }

            String[] properties = data.split("#");
            if (properties.length < 5) {
                return null;
            }

            FXHAsset fxhAsset = new FXHAsset();
            int index = 0;
            fxhAsset.type = Integer.parseInt(properties[index++]);
            fxhAsset.fullName = properties[index++];
            fxhAsset.officeName = properties[index++];
            fxhAsset.chsName = properties[index++];

            if (properties.length > 5) {
                fxhAsset.briefName = properties[index++];
            } else {
                fxhAsset.briefName = fxhAsset.chsName;
            }

            return fxhAsset;
        }
    }

    public static void main(String[] args) {
        FXHAsset.from("0#maker#Maker##MKR#/coin/e87a50389327af9e5c5c8f3a8ad1b3_small.png");
    }
}
