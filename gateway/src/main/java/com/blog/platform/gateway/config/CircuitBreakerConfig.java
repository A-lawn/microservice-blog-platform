package com.blog.platform.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 熔断器配置
 * 配置各微服务的熔断策略
 */
@Configuration
public class CircuitBreakerConfig {

    /**
     * 用户服务熔断器配置
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> userServiceCircuitBreakerCustomizer() {
        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .slidingWindowType(SlidingWindowType.COUNT_BASED)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .minimumNumberOfCalls(5)
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5))
                        .build()), "user-service-cb");
    }

    /**
     * 文章服务熔断器配置
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> articleServiceCircuitBreakerCustomizer() {
        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .slidingWindowType(SlidingWindowType.COUNT_BASED)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .minimumNumberOfCalls(5)
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(10))
                        .build()), "article-service-cb");
    }

    /**
     * 评论服务熔断器配置
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> commentServiceCircuitBreakerCustomizer() {
        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .slidingWindowType(SlidingWindowType.COUNT_BASED)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .minimumNumberOfCalls(5)
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(8))
                        .build()), "comment-service-cb");
    }

    /**
     * 默认熔断器配置
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCircuitBreakerCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .slidingWindowType(SlidingWindowType.COUNT_BASED)
                        .failureRateThreshold(60)
                        .waitDurationInOpenState(Duration.ofSeconds(20))
                        .minimumNumberOfCalls(3)
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5))
                        .build())
                .build());
    }
}