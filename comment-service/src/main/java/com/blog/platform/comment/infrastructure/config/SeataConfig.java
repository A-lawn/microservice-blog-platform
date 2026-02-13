package com.blog.platform.comment.infrastructure.config;

import io.seata.spring.annotation.GlobalTransactionScanner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seata分布式事务配置（标准版默认关闭，高级版可开启）
 * 配置Seata AT模式的基础事务管理
 */
@Configuration
@ConditionalOnProperty(name = "feature.seata.enabled", havingValue = "true")
public class SeataConfig {

    /**
     * 配置全局事务扫描器
     * 用于扫描和处理@GlobalTransactional注解
     */
    @Bean
    public GlobalTransactionScanner globalTransactionScanner() {
        return new GlobalTransactionScanner("comment-service", "blog-platform-tx-group");
    }
}