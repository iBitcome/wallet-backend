package com.rst.cgi.service.impl;
import com.rst.cgi.service.thrift.gen.pushserver.PushService;
import com.rst.cgi.service.thrift.gen.simserver.SimService;
import com.rst.thrift.client.ThriftAddress;
import com.rst.thrift.export.ThriftAddressFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hujia on 2017/3/21.
 */
@Service
@ConditionalOnProperty(name = "zookeeper.server.enable", havingValue = "false")
public class SimpleThriftAddressFactory implements ThriftAddressFactory {
    /**
     * value from config properties========
     */
    @Value("${service.push.server.uri}")
    private String PushServerUri;
    @Value("${service.sim.server.uri")
    private String SimServerUri;
    /**
     * /* ======end of config properties=======
     */

    public Map<String, ThriftAddress> thriftAddressMap;

    @PostConstruct
    void init() {
        thriftAddressMap = new HashMap<>(7);
        thriftAddressMap.put(PushService.class.getSimpleName(),
                new ThriftAddress(ipFromUri(PushServerUri),
                        portFromUri(PushServerUri),
                        PushService.class, true));
        thriftAddressMap.put(SimService.class.getSimpleName(),
                new ThriftAddress(ipFromUri(SimServerUri),
                        portFromUri(SimServerUri),
                        SimService.class, true));
    }

    @Override
    public ThriftAddress get(Class aClass) {
        return thriftAddressMap.get(aClass.getSimpleName());
    }

    private String ipFromUri(String uri) {
        return uri.substring(0, uri.indexOf(":")).trim();
    }

    private int portFromUri(String uri) {
        return Integer.parseInt(uri.substring(uri.indexOf(":") + 1).trim());
    }
}
