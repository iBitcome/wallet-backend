package com.rst.cgi.conf;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.config.JCacheConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by hujia on 2017/2/23.
 */
@Configuration
@EnableCaching
public class RedisConfig extends JCacheConfigurerSupport {
    @Value("${spring.redis.pool.test-on-borrow:true}")
    private boolean testOnBorrow;
    @Value("${spring.redis.pool.test-on-create:false}")
    private boolean testOnCreate;
    @Value("${spring.redis.pool.test-on-return:false}")
    private boolean testOnReturn;
    @Value("${spring.redis.pool.test-while-idle:true}")
    private boolean testWhileIdle;
    @Value("${spring.redis.pool.use-pool:true}")
    private boolean usePool;
    @Value("${spring.redis.password}")
    private String password;
//    @Value("${spring.redis.host}")
//    private String host;
//    @Value("${spring.redis.port}")
//    private int port;
    @Value("${spring.redis.database}")
    private int database;
    @Value("${spring.redis.sentinel.master}")
    private String master;
    @Value("${spring.redis.sentinel.nodes}")
    private String nodes;

    @Bean(name = {"factory"})
    JedisConnectionFactory jedisConnectionFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setTestOnCreate(testOnCreate);
        jedisPoolConfig.setTestOnReturn(testOnReturn);
        jedisPoolConfig.setTestWhileIdle(testWhileIdle);
        jedisPoolConfig.setMinIdle(10);
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxWaitMillis(10000);
        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        RedisNode.RedisNodeBuilder redisNodeBuilder = new RedisNode.RedisNodeBuilder();
        redisNodeBuilder.withName(master);
        redisSentinelConfiguration.setMaster(redisNodeBuilder.build());
        Set<RedisNode> set=Sets.newHashSet();
        Arrays.asList(nodes.split(",")).forEach( node -> {
            List<String> hostList=Arrays.asList(node.split(":"));
            for(int i=0;i<hostList.size()-1;i+=2){
                set.add(new RedisNode(hostList.get(i),Integer.parseInt(hostList.get(i+1))));
            }
        });
        redisSentinelConfiguration.setSentinels(set);
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(redisSentinelConfiguration,jedisPoolConfig);
        connectionFactory.setPassword(password);
        connectionFactory.setDatabase(database);
        connectionFactory.setUsePool(true);
        return connectionFactory;
    }

    /*@Value("${spring.redis.cluster.nodes}")
    private String strNodes;
    @Bean(name = {"factory"})
    JedisConnectionFactory jedisConnectionFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setTestOnCreate(testOnCreate);
        jedisPoolConfig.setTestOnReturn(testOnReturn);
        jedisPoolConfig.setTestWhileIdle(testWhileIdle);
        jedisPoolConfig.setMinIdle(10);
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxWaitMillis(10000);

        List<String> nodesList = Arrays.asList(strNodes.split(","));
        RedisClusterConfiguration configuration = new RedisClusterConfiguration(nodesList);
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(configuration, jedisPoolConfig);
        connectionFactory.setPassword(password);
        connectionFactory.setDatabase(database);
        connectionFactory.setUsePool(true);
        return connectionFactory;
    }*/


    @Bean
    public KeyGenerator wiselyKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object param : params) {
                sb.append(param.toString());
            }
            return sb.toString();
        };
    }
    @Bean
    public RedisTemplate<String, String> redisTemplate(
            JedisConnectionFactory factory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        stringRedisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        stringRedisTemplate.afterPropertiesSet();
        return stringRedisTemplate;
    }
    @Bean
    public CacheManager cacheManager(@SuppressWarnings("rawtypes") RedisTemplate redisTemplate) {
        return new RedisCacheManager(redisTemplate);
    }
}