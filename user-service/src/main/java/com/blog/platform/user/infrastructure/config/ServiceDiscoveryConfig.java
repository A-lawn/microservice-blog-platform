package com.blog.platform.user.infrastructure.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 服务发现配置
 * 配置负载均衡和服务调用
 */
@Configuration
public class ServiceDiscoveryConfig {
    
    /**
     * 配置负载均衡的RestTemplate
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}