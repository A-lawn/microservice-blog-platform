package com.blog.platform.comment.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 动态配置属性 - 评论服务
 * 支持配置热更新和降级策略
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "comment-service")
public class DynamicConfigProperties {
    
    @Autowired
    private ConfigFallbackManager fallbackManager;
    
    private String version = "1.0.0";
    private int maxCommentLength = 1000;
    private int maxReplyDepth = 5;
    private boolean enableModeration = true;
    private boolean enableNotification = true;
    private int defaultPageSize = 20;
    
    public void initializeFallback() {
        // 初始化时缓存当前配置
        if (fallbackManager != null) {
            fallbackManager.initialize();
            fallbackManager.cacheCurrentConfig();
        }
    }
    
    // Getters and Setters with fallback support
    public String getVersion() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getConfigValue("comment-service.version", version);
        }
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public int getMaxCommentLength() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getIntConfigValue("comment-service.maxCommentLength", maxCommentLength);
        }
        return maxCommentLength;
    }
    
    public void setMaxCommentLength(int maxCommentLength) {
        this.maxCommentLength = maxCommentLength;
    }
    
    public int getMaxReplyDepth() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getIntConfigValue("comment-service.maxReplyDepth", maxReplyDepth);
        }
        return maxReplyDepth;
    }
    
    public void setMaxReplyDepth(int maxReplyDepth) {
        this.maxReplyDepth = maxReplyDepth;
    }
    
    public boolean isEnableModeration() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getBooleanConfigValue("comment-service.enableModeration", enableModeration);
        }
        return enableModeration;
    }
    
    public void setEnableModeration(boolean enableModeration) {
        this.enableModeration = enableModeration;
    }
    
    public boolean isEnableNotification() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getBooleanConfigValue("comment-service.enableNotification", enableNotification);
        }
        return enableNotification;
    }
    
    public void setEnableNotification(boolean enableNotification) {
        this.enableNotification = enableNotification;
    }
    
    public int getDefaultPageSize() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getIntConfigValue("comment-service.defaultPageSize", defaultPageSize);
        }
        return defaultPageSize;
    }
    
    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }
}