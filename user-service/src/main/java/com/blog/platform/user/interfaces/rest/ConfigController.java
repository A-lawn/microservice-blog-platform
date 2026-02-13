package com.blog.platform.user.interfaces.rest;

import com.blog.platform.user.infrastructure.config.ConfigChangeListener;
import com.blog.platform.user.infrastructure.config.DynamicConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 配置管理控制器
 * 提供配置查看和刷新功能
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {
    
    @Autowired
    private DynamicConfigProperties dynamicConfigProperties;
    
    @Autowired
    private ContextRefresher contextRefresher;
    
    @Autowired
    private ConfigChangeListener configChangeListener;
    
    /**
     * 获取当前配置
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("version", dynamicConfigProperties.getVersion());
        config.put("maxLoginAttempts", dynamicConfigProperties.getMaxLoginAttempts());
        config.put("tokenExpirationTime", dynamicConfigProperties.getTokenExpirationTime());
        config.put("enableRegistration", dynamicConfigProperties.isEnableRegistration());
        config.put("defaultRole", dynamicConfigProperties.getDefaultRole());
        return config;
    }
    
    /**
     * 手动刷新配置
     */
    @PostMapping("/refresh")
    public Map<String, Object> refreshConfig() {
        Set<String> refreshedKeys = contextRefresher.refresh();
        Map<String, Object> result = new HashMap<>();
        result.put("refreshed", true);
        result.put("refreshedKeys", refreshedKeys);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    /**
     * 获取配置变更历史
     */
    @GetMapping("/history")
    public ConcurrentLinkedQueue<ConfigChangeListener.ConfigChangeRecord> getConfigHistory() {
        return configChangeListener.getChangeHistory();
    }
}