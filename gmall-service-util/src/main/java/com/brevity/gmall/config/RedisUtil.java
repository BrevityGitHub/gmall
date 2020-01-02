package com.brevity.gmall.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    /*
     * 使用Jedis操作redis
     * 1.获取JedisPool连接池
     * 2.设置连接池的参数(初始化连接池)
     * 3.从连接池中获取一个Jedis
     */

    // 声明一个JedisPool对象
    private JedisPool jedisPool;

    // 初始化连接池，从配置文件中读取参数
    public void initJedisPool(String host, int port, int timeOut) {
        // 设置连接池的参数
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 设置连接池的最小剩余数量
        jedisPoolConfig.setMinIdle(10);
        // 设置等待时间
        jedisPoolConfig.setMaxWaitMillis(10 * 1000);
        // 设置连接的最大数，与硬件有关
        jedisPoolConfig.setMaxTotal(200);
        // 如果出现获取jedis失败或者达到最大值的时候，使其进行等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 表示在获取jedis的时候做一个自动检测，检测是否可用
        jedisPoolConfig.setTestOnBorrow(true);

        jedisPool = new JedisPool(jedisPoolConfig, host, port, timeOut);
    }

    // 获取jedis对象
    public Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
