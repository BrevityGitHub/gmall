package com.brevity.gmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 做配置文件使用，相当于spring框架中的核心xml文件
@Configuration
public class RedisConfig {
    // 获取参数

    /* :disabled 表示如果从配置文件中没有找到对应的host，则值为disabled，
     * 如果不配置，默认为null
     */
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;
    @Value("${spring.redis.timeOut:10000}")
    private int timeOut;

    // 将host、port、timeOut传给RedisUtil类中的initJedisPool方法使用
    // 创建一个bean对象来获取RedisUtil对象
    @Bean // 表示将RedisUtil放入spring容器中
    public RedisUtil getRedisUtil() {
        // 没有获取到host的情况
        if ("disabled".equals(host)) {
            return null;
        }

        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initJedisPool(host, port, timeOut);
        return redisUtil;
    }
}
