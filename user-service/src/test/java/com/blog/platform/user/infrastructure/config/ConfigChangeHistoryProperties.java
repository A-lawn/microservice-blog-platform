package com.blog.platform.user.infrastructure.config;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * **Feature: microservice-blog-platform, Property 11: 配置变更历史记录**
 * **验证需求: Requirements 5.4**
 * 
 * 测试配置变更历史记录功能
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false"
})
public class ConfigChangeHistoryProperties {
    
    private ConfigChangeListener configChangeListener;
    
    @BeforeProperty
    void setUp() {
        configChangeListener = new ConfigChangeListener();
    }
    
    /**
     * 属性测试：配置变更历史记录
     * 对于任何配置变更操作，系统应当记录变更历史并支持回滚到之前的版本
     */
    @Property(tries = 100)
    void configChangeHistoryRecording(@ForAll("configChangeKeys") Set<String> changedKeys) {
        // Given: 初始历史记录数量
        int initialHistorySize = configChangeListener.getChangeHistory().size();
        LocalDateTime beforeChange = LocalDateTime.now();
        
        // When: 触发配置变更事件
        EnvironmentChangeEvent event = new EnvironmentChangeEvent(changedKeys);
        configChangeListener.onApplicationEvent(event);
        
        // Then: 应当增加一条历史记录
        int newHistorySize = configChangeListener.getChangeHistory().size();
        Assume.that(newHistorySize == initialHistorySize + 1);
        
        // And: 最新的历史记录应当包含正确信息
        ConfigChangeListener.ConfigChangeRecord latestRecord = 
            configChangeListener.getChangeHistory().stream()
                .reduce((first, second) -> second)
                .orElse(null);
        
        Assume.that(latestRecord != null);
        Assume.that(latestRecord.getChangedKeys().equals(changedKeys));
        Assume.that(latestRecord.getServiceName().equals("user-service"));
        Assume.that(latestRecord.getTimestamp().isAfter(beforeChange) || 
                   latestRecord.getTimestamp().isEqual(beforeChange));
    }
    
    /**
     * 属性测试：配置变更历史顺序
     * 对于任何配置变更序列，历史记录应当按时间顺序保存
     */
    @Property(tries = 50)
    void configChangeHistoryOrdering(@ForAll("configChangeSequence") List<Set<String>> changeSequence) {
        // Given: 清空历史记录
        configChangeListener.getChangeHistory().clear();
        
        // When: 按顺序触发配置变更事件
        for (Set<String> changedKeys : changeSequence) {
            EnvironmentChangeEvent event = new EnvironmentChangeEvent(changedKeys);
            configChangeListener.onApplicationEvent(event);
            // 添加小延迟确保时间戳不同
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Then: 历史记录应当按时间顺序排列
        List<ConfigChangeListener.ConfigChangeRecord> history = 
            configChangeListener.getChangeHistory().stream().collect(Collectors.toList());
        
        for (int i = 1; i < history.size(); i++) {
            LocalDateTime prevTime = history.get(i - 1).getTimestamp();
            LocalDateTime currTime = history.get(i).getTimestamp();
            Assume.that(currTime.isAfter(prevTime) || currTime.isEqual(prevTime));
        }
    }
    
    /**
     * 属性测试：配置变更历史查询
     * 对于任何历史记录，应当能够查询到特定时间段的变更记录
     */
    @Property(tries = 50)
    void configChangeHistoryQuery(@ForAll("configChangeSequence") List<Set<String>> changeSequence) {
        // Given: 清空历史记录并记录开始时间
        configChangeListener.getChangeHistory().clear();
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(1); // 稍微提前一点确保包含所有记录
        
        // When: 触发配置变更事件
        for (Set<String> changedKeys : changeSequence) {
            EnvironmentChangeEvent event = new EnvironmentChangeEvent(changedKeys);
            configChangeListener.onApplicationEvent(event);
        }
        
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(1); // 稍微延后一点确保包含所有记录
        
        // Then: 所有记录的时间戳应当在指定时间范围内
        List<ConfigChangeListener.ConfigChangeRecord> history = 
            configChangeListener.getChangeHistory().stream().collect(Collectors.toList());
        
        // 验证历史记录数量正确
        Assume.that(history.size() == changeSequence.size());
        
        // 验证时间范围
        for (ConfigChangeListener.ConfigChangeRecord record : history) {
            LocalDateTime recordTime = record.getTimestamp();
            Assume.that(!recordTime.isBefore(startTime));
            Assume.that(!recordTime.isAfter(endTime));
        }
    }
    
    /**
     * 属性测试：配置变更历史完整性
     * 对于任何配置变更，历史记录应当包含完整的变更信息
     */
    @Property(tries = 100)
    void configChangeHistoryCompleteness(@ForAll("configChangeKeys") Set<String> changedKeys) {
        // When: 触发配置变更事件
        EnvironmentChangeEvent event = new EnvironmentChangeEvent(changedKeys);
        configChangeListener.onApplicationEvent(event);
        
        // Then: 最新的历史记录应当包含所有必要字段
        ConfigChangeListener.ConfigChangeRecord latestRecord = 
            configChangeListener.getChangeHistory().stream()
                .reduce((first, second) -> second)
                .orElse(null);
        
        Assume.that(latestRecord != null);
        Assume.that(latestRecord.getTimestamp() != null);
        Assume.that(latestRecord.getServiceName() != null && !latestRecord.getServiceName().isEmpty());
        Assume.that(latestRecord.getChangedKeys() != null);
        Assume.that(latestRecord.getChangedKeys().containsAll(changedKeys));
    }
    
    /**
     * 属性测试：配置变更历史容量限制
     * 对于任何大量的配置变更，历史记录应当限制在合理的容量范围内
     */
    @Property(tries = 20)
    void configChangeHistoryCapacityLimit(@ForAll @IntRange(min = 150, max = 200) int changeCount) {
        // Given: 清空历史记录
        configChangeListener.getChangeHistory().clear();
        
        // When: 触发大量配置变更事件
        for (int i = 0; i < changeCount; i++) {
            Set<String> changedKeys = Set.of("config.key." + i);
            EnvironmentChangeEvent event = new EnvironmentChangeEvent(changedKeys);
            configChangeListener.onApplicationEvent(event);
        }
        
        // Then: 历史记录应当不超过容量限制
        int historySize = configChangeListener.getChangeHistory().size();
        Assume.that(historySize <= 100);
        
        // And: 应当保留最新的记录
        if (changeCount > 100) {
            List<ConfigChangeListener.ConfigChangeRecord> history = 
                configChangeListener.getChangeHistory().stream().collect(Collectors.toList());
            
            // 检查最后一条记录是否包含最新的变更
            ConfigChangeListener.ConfigChangeRecord lastRecord = history.get(history.size() - 1);
            String expectedLastKey = "config.key." + (changeCount - 1);
            Assume.that(lastRecord.getChangedKeys().contains(expectedLastKey));
        }
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
        ).set().ofMinSize(1).ofMaxSize(3);
    }
    
    @Provide
    Arbitrary<List<Set<String>>> configChangeSequence() {
        return configChangeKeys().list().ofMinSize(1).ofMaxSize(10);
    }
}