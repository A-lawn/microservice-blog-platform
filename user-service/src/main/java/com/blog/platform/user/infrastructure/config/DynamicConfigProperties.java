package com.blog.platform.user.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 动态配置属性
 * 支持配置热更新和降级策略
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "user-service")
public class DynamicConfigProperties {
    
    @Autowired
    private ConfigFallbackManager fallbackManager;
    
    private String version = "1.0.0";
    private int maxLoginAttempts = 5;
    private long tokenExpirationTime = 86400000; // 24小时
    private boolean enableRegistration = true;
    private String defaultRole = "USER";
    
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
            return fallbackManager.getConfigValue("user-service.version", version);
        }
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public int getMaxLoginAttempts() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getIntConfigValue("user-service.maxLoginAttempts", maxLoginAttempts);
        }
        return maxLoginAttempts;
    }
    
    public void setMaxLoginAttempts(int maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }
    
    public long getTokenExpirationTime() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getLongConfigValue("user-service.tokenExpirationTime", tokenExpirationTime);
        }
        return tokenExpirationTime;
    }
    
    public void setTokenExpirationTime(long tokenExpirationTime) {
        this.tokenExpirationTime = tokenExpirationTime;
    }
    
    public boolean isEnableRegistration() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getBooleanConfigValue("user-service.enableRegistration", enableRegistration);
        }
        return enableRegistration;
    }
    
    public void setEnableRegistration(boolean enableRegistration) {
        this.enableRegistration = enableRegistration;
    }
    
    public String getDefaultRole() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getConfigValue("user-service.defaultRole", defaultRole);
        }
        return defaultRole;
    }
    
    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }
}