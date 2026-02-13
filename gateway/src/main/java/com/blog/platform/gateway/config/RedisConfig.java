package com.blog.platform.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置
 * 用于限流功能的Redis配置
 */
@Configuration
public class RedisConfig {

    /**
     * 响应式Redis模板配置
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        RedisSerializationContext<String, String> serializationContext = 
                RedisSerializationContext.<String, String>newSerializationContext()
                        .key(StringRedisSerializer.UTF_8)
                        .value(StringRedisSerializer.UTF_8)
                        .hashKey(StringRedisSerializer.UTF_8)
                        .hashValue(StringRedisSerializer.UTF_8)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}