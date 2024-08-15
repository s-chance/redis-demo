package org.entropy.couponseckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 添加redis地址，单节点模式，也可以使用 config.useClusterServers()添加集群地址
        config.useSingleServer()
                .setAddress("redis://localhost:6379")
                .setPassword("123");
        return Redisson.create(config);
    }
}
