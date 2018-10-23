package com.rst.cgi.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author hujia
 */
@Configuration
public class ExchangeConfig {
    @Value("${exchange.dex-top.api}")
    private String dexTopApi;

    public static final Exchange DEX_TOP =
            ExchangeConfig.make("Dex-Top", "https://testnet271828.dex.top/v1/", "_");

    @PostConstruct
    void init() {
        DEX_TOP.apiHost = dexTopApi;
    }

    @Data
    public static class Exchange {
        private String name;
        private String apiHost;
        private String symbolSeparator;
    }

    public static Exchange make(String name, String apiHost, String symbolSeparator) {
        Exchange exchange = new Exchange();
        exchange.name = name;
        exchange.apiHost = apiHost;
        exchange.symbolSeparator = symbolSeparator;
        return exchange;
    }
}
