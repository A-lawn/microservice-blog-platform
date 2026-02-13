package com.blog.platform.article.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 配置变更监听器 - 文章服务
 * 记录配置变更历史并触发缓存更新
 */
@Component
public class ConfigChangeListener implements ApplicationListener<EnvironmentChangeEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigChangeListener.class);
    
    @Autowired
    private ConfigFallbackManager fallbackManager;
    
    // 配置变更历史记录
    private final ConcurrentLinkedQueue<ConfigChangeRecord> changeHistory = new ConcurrentLinkedQueue<>();
    
    // 当前配置快照
    private final ConcurrentHashMap<String, String> currentConfig = new ConcurrentHashMap<>();
    
    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        logger.info("Configuration change detected. Changed keys: {}", event.getKeys());
        
        // 记录配置变更
        ConfigChangeRecord record = new ConfigChangeRecord();
        record.setTimestamp(LocalDateTime.now());
        record.setChangedKeys(event.getKeys());
        record.setServiceName("article-service");
        
        // 保存变更记录
        changeHistory.offer(record);
        
        // 限制历史记录数量
        while (changeHistory.size() > 100) {
            changeHistory.poll();
        }
        
        // 触发配置缓存更新
        if (fallbackManager != null) {
            try {
                fallbackManager.cacheCurrentConfig();
                logger.info("Configuration cache updated after change event");
            } catch (Exception e) {
                logger.error("Failed to update configuration cache", e);
            }
        }
        
        logger.info("Configuration change recorded: {}", record);
    }
    
    /**
     * 获取配置变更历史
     */
    public ConcurrentLinkedQueue<ConfigChangeRecord> getChangeHistory() {
        return new ConcurrentLinkedQueue<>(changeHistory);
    }
    
    /**
     * 配置变更记录
     */
    public static class ConfigChangeRecord {
        private LocalDateTime timestamp;
        private java.util.Set<String> changedKeys;
        private String serviceName;
        
        // Getters and Setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public java.util.Set<String> getChangedKeys() {
            return changedKeys;
        }
        
        public void setChangedKeys(java.util.Set<String> changedKeys) {
            this.changedKeys = changedKeys;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        
        @Override
        public String toString() {
            return "ConfigChangeRecord{" +
                    "timestamp=" + timestamp +
                    ", changedKeys=" + changedKeys +
                    ", serviceName='" + serviceName + '\'' +
                    '}';
        }
    }
}