package com.blog.platform.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return exchange.getRequest().getRemoteAddress() != null 
                ? reactor.core.publisher.Mono.just(
                    userId != null ? userId : 
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress())
                : reactor.core.publisher.Mono.just("anonymous");
        };
    }
}
