package com.blog.platform.comment.infrastructure.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 * 用于微服务间的HTTP通信
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * 配置负载均衡的RestTemplate
     * 支持通过服务名进行调用
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}