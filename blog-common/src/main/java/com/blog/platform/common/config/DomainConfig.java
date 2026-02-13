package com.blog.platform.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 领域基础设施配置
 */
@Configuration
@ComponentScan(basePackages = "com.blog.platform.common")
public class DomainConfig {
    // Spring会自动扫描并注册DomainEventPublisher等组件
}