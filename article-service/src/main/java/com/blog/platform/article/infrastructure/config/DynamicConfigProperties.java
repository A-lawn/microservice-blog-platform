package com.blog.platform.article.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 动态配置属性 - 文章服务
 * 支持配置热更新和降级策略
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "article-service")
public class DynamicConfigProperties {
    
    @Autowired
    private ConfigFallbackManager fallbackManager;
    
    private String version = "1.0.0";
    private int maxArticleLength = 50000;
    private int defaultPageSize = 10;
    private boolean enableDraft = true;
    private boolean enableSearch = true;
    private int searchResultLimit = 100;
    
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
            return fallbackManager.getConfigValue("article-service.version", version);
        }
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public int getMaxArticleLength() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getIntConfigValue("article-service.maxArticleLength", maxArticleLength);
        }
        return maxArticleLength;
    }
    
    public void setMaxArticleLength(int maxArticleLength) {
        this.maxArticleLength = maxArticleLength;
    }
    
    public int getDefaultPageSize() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getIntConfigValue("article-service.defaultPageSize", defaultPageSize);
        }
        return defaultPageSize;
    }
    
    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }
    
    public boolean isEnableDraft() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getBooleanConfigValue("article-service.enableDraft", enableDraft);
        }
        return enableDraft;
    }
    
    public void setEnableDraft(boolean enableDraft) {
        this.enableDraft = enableDraft;
    }
    
    public boolean isEnableSearch() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getBooleanConfigValue("article-service.enableSearch", enableSearch);
        }
        return enableSearch;
    }
    
    public void setEnableSearch(boolean enableSearch) {
        this.enableSearch = enableSearch;
    }
    
    public int getSearchResultLimit() {
        if (fallbackManager != null && !fallbackManager.isConfigCenterAvailable()) {
            return fallbackManager.getIntConfigValue("article-service.searchResultLimit", searchResultLimit);
        }
        return searchResultLimit;
    }
    
    public void setSearchResultLimit(int searchResultLimit) {
        this.searchResultLimit = searchResultLimit;
    }
}