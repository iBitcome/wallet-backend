package com.rst.cgi.service.exchange.impl;

import com.rst.cgi.common.utils.DijkstraUtil;
import com.rst.cgi.data.dto.Symbol;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hujia
 */
@Component
@Scope("prototype")
public class SymbolPriceCache {
    public interface SymbolPriceMaker {
        /**
         * API获取symbol的实时价格
         * @param symbol 交易对
         * @return 最新价格
         */
        String fetchPrice(Symbol symbol);

        /**
         * API获取支持的交易对
         * @return 交易对列表
         */
        Set<Symbol> fetchSymbols();
    }

    private Map<Symbol, String> symbolToPrice = new HashMap<>();

    private List<String> assets;
    private Set<Symbol> symbols;
    private SymbolPriceMaker priceMaker;
    private int[][] assetEdges;

    public void init(Set<Symbol> symbolList, SymbolPriceMaker priceMaker) {
        setUpSymbols(symbolList);
        this.priceMaker = priceMaker;
    }

    public List<Symbol> getSymbols() {
        return symbols.stream().collect(Collectors.toList());
    }

    public Map<Symbol, String> currentAllPrice() {
        return symbolToPrice;
    }

    public void setPrice(Symbol symbol, String price) {
        symbolToPrice.put(symbol, price);
    }

    public String getPrice(Symbol symbol) {
        return getPrice(symbol.getBaseAsset(), symbol.getQuoteAsset());
    }

    private String getPrice(String baseAsset, String quoteAsset) {
        String price = purePrice(baseAsset, quoteAsset);
        if (!StringUtils.isEmpty(price)) {
            return price;
        }
        //没有一手价格，推导出价格
        return derivePrice(baseAsset, quoteAsset);
    }

    private String purePrice(String baseAsset, String quoteAsset) {
        Symbol symbol = new Symbol(baseAsset, quoteAsset);
        boolean inverse = false;
        if (!symbols.contains(symbol)) {
            symbol = symbol.inverse();
            if (!symbols.contains(symbol)) {
                return null;
            }
            inverse = true;
        }

        if (!symbolToPrice.containsKey(symbol)) {
            symbolToPrice.put(symbol, priceMaker.fetchPrice(symbol));
        }

        String price = symbolToPrice.get(symbol);
        if (inverse) {
            price = new Double(1.0) / Double.parseDouble(price) + "";
        }

        return price;
    }

    private void setupAssetEdges() {
        int size = assets.size();
        assetEdges = new int[size][size];

        Symbol symbol = new Symbol();
        for (int i = 0; i < size; i++) {
            assetEdges[i][i] = Integer.MAX_VALUE;
            for (int j = 0; j < i; j++) {
                symbol.setBaseAsset(assets.get(i));
                symbol.setQuoteAsset(assets.get(j));
                if (symbols.contains(symbol) || symbols.contains(symbol.inverse())) {
                    assetEdges[i][j] = assetEdges[j][i] = 1;
                } else {
                    assetEdges[i][j] = Integer.MAX_VALUE;
                }
            }
        }
    }

    public String derivePrice(String baseAsset, String quoteAsset) {
        DijkstraUtil.Vertex vertex = DijkstraUtil.search(
                assets.indexOf(baseAsset), assets.indexOf(quoteAsset), assetEdges);

        if (vertex == null) {
            return null;
        }

        Double price = 1.0;
        for (int i = 0; i < vertex.getPrevList().size(); i++) {
            int index = vertex.getPrevList().get(i);
            String base = assets.get(index);
            String quote = quoteAsset;
            if (i < vertex.getPrevList().size() - 1) {
                index = vertex.getPrevList().get(i+1);
                quote = assets.get(index);
            }

            String purePrice = purePrice(base, quote);

            if (StringUtils.isEmpty(purePrice)) {
                return null;
            }

            price *= Double.parseDouble(purePrice);
        }

        return "" + price;
    }

//    @Scheduled(fixedDelay = 10 * 60 * 1000)
    private void updatePrices() {
        symbolToPrice.forEach((key, value) -> {
            String price = priceMaker.fetchPrice(key);
            if (!StringUtils.isEmpty(price)) {
                symbolToPrice.put(key, value);
            }

            try {
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    //每日凌晨2点
//    @Scheduled(cron = "0 0 2 * * ?")
    private void updateSymbols() {
        setUpSymbols(priceMaker.fetchSymbols());
    }

    private void setUpSymbols(Set<Symbol> symbolList) {
        Set<String> assetSet = new HashSet<>();
        symbols = new HashSet<>();
        symbolList.forEach(symbol -> {
            symbols.add(symbol);
            assetSet.add(symbol.getBaseAsset().toUpperCase());
            assetSet.add(symbol.getQuoteAsset().toUpperCase());
        });

        assets = new ArrayList<>();
        assets.addAll(assetSet);

        setupAssetEdges();
    }
}
