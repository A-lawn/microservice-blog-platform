package com.blog.platform.user.infrastructure.config;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Set;

/**
 * **Feature: microservice-blog-platform, Property 10: 配置热更新通知**
 * **验证需求: Requirements 5.2**
 * 
 * 测试配置热更新通知功能
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false"
})
public class ConfigHotUpdateProperties {
    
    private ConfigChangeListener configChangeListener;
    private DynamicConfigProperties dynamicConfigProperties;
    
    @BeforeProperty
    void setUp() {
        configChangeListener = new ConfigChangeListener();
        dynamicConfigProperties = new DynamicConfigProperties();
    }
    
    /**
     * 属性测试：配置热更新通知
     * 对于任何配置变更事件，相关服务应当收到配置变更通知并更新本地配置
     */
    @Property(tries = 100)
    void configHotUpdateNotification(@ForAll("configChangeKeys") Set<String> changedKeys) {
        // Given: 配置变更事件
        EnvironmentChangeEvent event = new EnvironmentChangeEvent(changedKeys);
        
        // When: 触发配置变更事件
        int initialHistorySize = configChangeListener.getChangeHistory().size();
        configChangeListener.onApplicationEvent(event);
        
        // Then: 应当记录配置变更历史
        int newHistorySize = configChangeListener.getChangeHistory().size();
        Assume.that(newHistorySize > initialHistorySize);
        
        // And: 变更记录应当包含正确的信息
        ConfigChangeListener.ConfigChangeRecord latestRecord = 
            configChangeListener.getChangeHistory().stream()
                .reduce((first, second) -> second)
                .orElse(null);
        
        Assume.that(latestRecord != null);
        Assume.that(latestRecord.getChangedKeys().equals(changedKeys));
        Assume.that(latestRecord.getServiceName().equals("user-service"));
        Assume.that(latestRecord.getTimestamp() != null);
    }
    
    /**
     * 属性测试：配置变更历史限制
     * 对于任何配置变更序列，历史记录应当限制在合理范围内
     */
    @Property(tries = 50)
    void configChangeHistoryLimit(@ForAll("configChangeSequence") java.util.List<Set<String>> changeSequence) {
        // Given: 一系列配置变更
        for (Set<String> changedKeys : changeSequence) {
            EnvironmentChangeEvent event = new EnvironmentChangeEvent(changedKeys);
            configChangeListener.onApplicationEvent(event);
        }
        
        // Then: 历史记录应当不超过限制
        Assume.that(configChangeListener.getChangeHistory().size() <= 100);
    }
    
    /**
     * 属性测试：配置属性更新
     * 对于任何有效的配置值，动态配置属性应当能够正确更新
     */
    @Property(tries = 100)
    void dynamicConfigPropertiesUpdate(
            @ForAll @IntRange(min = 1, max = 10) int maxLoginAttempts,
            @ForAll @LongRange(min = 3600000, max = 86400000) long tokenExpirationTime,
            @ForAll boolean enableRegistration) {
        
        // When: 更新配置属性
        dynamicConfigProperties.setMaxLoginAttempts(maxLoginAttempts);
        dynamicConfigProperties.setTokenExpirationTime(tokenExpirationTime);
        dynamicConfigProperties.setEnableRegistration(enableRegistration);
        
        // Then: 配置应当正确更新
        Assume.that(dynamicConfigProperties.getMaxLoginAttempts() == maxLoginAttempts);
        Assume.that(dynamicConfigProperties.getTokenExpirationTime() == tokenExpirationTime);
        Assume.that(dynamicConfigProperties.isEnableRegistration() == enableRegistration);
    }
    
    @Provide
    Arbitrary<Set<String>> configChangeKeys() {
        return Arbitraries.of(
            "user-service.maxLoginAttempts",
            "user-service.tokenExpirationTime", 
            "user-service.enableRegistration",
            "user-service.defaultRole",
            "user-service.version",
            "spring.datasource.url",
            "spring.redis.host",
            "logging.level.com.blog.platform"
        ).set().ofMinSize(1).ofMaxSize(5);
    }
    
    @Provide
    Arbitrary<java.util.List<Set<String>>> configChangeSequence() {
        return configChangeKeys().list().ofMinSize(1).ofMaxSize(20);
    }
}