package com.blog.platform.article.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 配置降级管理器 - 文章服务
 * 当配置中心不可用时，使用本地缓存配置
 */
@Component
public class ConfigFallbackManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigFallbackManager.class);
    
    private final Environment environment;
    private final Properties properties;
    private final ScheduledExecutorService scheduler;
    
    // 本地配置缓存
    private final ConcurrentHashMap<String, Object> localConfigCache = new ConcurrentHashMap<>();
    
    // 配置文件路径
    @Value("${config.fallback.cache-dir:./config-cache}")
    private String cacheDir;
    
    @Value("${config.fallback.enabled:true}")
    private boolean fallbackEnabled;
    
    @Value("${config.fallback.cache-interval:30}")
    private int cacheIntervalSeconds;
    
    // 配置中心连接状态
    private volatile boolean configCenterAvailable = true;
    private volatile LocalDateTime lastSuccessfulSync;
    
    public ConfigFallbackManager(Environment environment) {
        this.environment = environment;
        this.properties = new Properties();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }
    
    public void initialize() {
        if (!fallbackEnabled) {
            logger.info("Configuration fallback is disabled");
            return;
        }
        
        // 创建缓存目录
        createCacheDirectory();
        
        // 加载本地缓存配置
        loadLocalCache();
        
        // 启动定期缓存任务
        startPeriodicCaching();
        
        // 启动配置中心健康检查
        startHealthCheck();
        
        logger.info("Configuration fallback manager initialized for article-service");
    }
    
    /**
     * 缓存当前配置到本地
     */
    public void cacheCurrentConfig() {
        if (!fallbackEnabled) {
            return;
        }
        
        try {
            Map<String, Object> currentConfig = extractCurrentConfig();
            
            // 保存到内存缓存
            localConfigCache.putAll(currentConfig);
            
            // 保存到文件
            saveConfigToFile(currentConfig);
            
            lastSuccessfulSync = LocalDateTime.now();
            configCenterAvailable = true;
            
            logger.debug("Configuration cached successfully. Cache size: {}", currentConfig.size());
            
        } catch (Exception e) {
            logger.error("Failed to cache configuration", e);
            configCenterAvailable = false;
        }
    }
    
    /**
     * 获取配置值，优先从配置中心获取，失败时使用本地缓存
     */
    public String getConfigValue(String key, String defaultValue) {
        try {
            // 尝试从配置中心获取
            String value = environment.getProperty(key);
            if (value != null) {
                // 更新本地缓存
                localConfigCache.put(key, value);
                return value;
            }
        } catch (Exception e) {
            logger.warn("Failed to get config from center for key: {}, using fallback", key, e);
            configCenterAvailable = false;
        }
        
        // 使用本地缓存
        Object cachedValue = localConfigCache.get(key);
        if (cachedValue != null) {
            logger.info("Using cached config for key: {}", key);
            return cachedValue.toString();
        }
        
        logger.warn("No cached config found for key: {}, using default: {}", key, defaultValue);
        return defaultValue;
    }
    
    /**
     * 获取整数配置值
     */
    public int getIntConfigValue(String key, int defaultValue) {
        try {
            String value = getConfigValue(key, String.valueOf(defaultValue));
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer config for key: {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔配置值
     */
    public boolean getBooleanConfigValue(String key, boolean defaultValue) {
        String value = getConfigValue(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
    
    /**
     * 检查配置中心是否可用
     */
    public boolean isConfigCenterAvailable() {
        return configCenterAvailable;
    }
    
    /**
     * 获取最后成功同步时间
     */
    public LocalDateTime getLastSuccessfulSync() {
        return lastSuccessfulSync;
    }
    
    /**
     * 获取缓存配置数量
     */
    public int getCachedConfigCount() {
        return localConfigCache.size();
    }
    
    private void createCacheDirectory() {
        try {
            Path cachePath = Paths.get(cacheDir);
            if (!Files.exists(cachePath)) {
                Files.createDirectories(cachePath);
                logger.info("Created cache directory: {}", cacheDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create cache directory: {}", cacheDir, e);
        }
    }
    
    private void loadLocalCache() {
        try {
            File cacheFile = new File(cacheDir, "article-service-config.properties");
            if (cacheFile.exists()) {
                Properties cachedConfig = new Properties();
                try (FileInputStream fis = new FileInputStream(cacheFile)) {
                    cachedConfig.load(fis);
                    for (String key : cachedConfig.stringPropertyNames()) {
                        localConfigCache.put(key, cachedConfig.getProperty(key));
                    }
                    logger.info("Loaded {} cached configurations from file", cachedConfig.size());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load local cache", e);
        }
    }
    
    private void saveConfigToFile(Map<String, Object> config) {
        try {
            File cacheFile = new File(cacheDir, "article-service-config.properties");
            Properties props = new Properties();
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue().toString());
            }
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                props.store(fos, "Article Service Configuration Cache - " + LocalDateTime.now());
            }
            logger.debug("Configuration saved to file: {}", cacheFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save configuration to file", e);
        }
    }
    
    private Map<String, Object> extractCurrentConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 提取文章服务相关配置
        String[] configKeys = {
            "article-service.version",
            "article-service.maxArticlesPerUser",
            "article-service.enableDrafts",
            "article-service.defaultStatus",
            "spring.datasource.article.jdbc-url",
            "spring.datasource.article.username",
            "spring.data.redis.host",
            "spring.data.redis.port",
            "spring.data.elasticsearch.uris",
            "logging.level.com.blog.platform"
        };
        
        for (String key : configKeys) {
            String value = environment.getProperty(key);
            if (value != null) {
                config.put(key, value);
            }
        }
        
        return config;
    }
    
    private void startPeriodicCaching() {
        scheduler.scheduleWithFixedDelay(
            this::cacheCurrentConfig,
            cacheIntervalSeconds,
            cacheIntervalSeconds,
            TimeUnit.SECONDS
        );
        
        logger.info("Started periodic configuration caching every {} seconds", cacheIntervalSeconds);
    }
    
    private void startHealthCheck() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                // 尝试获取一个配置来检查连接状态
                environment.getProperty("spring.application.name");
                if (!configCenterAvailable) {
                    logger.info("Configuration center connection restored");
                    configCenterAvailable = true;
                    // 立即缓存最新配置
                    cacheCurrentConfig();
                }
            } catch (Exception e) {
                if (configCenterAvailable) {
                    logger.warn("Configuration center connection lost, switching to fallback mode");
                    configCenterAvailable = false;
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
    
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}