package com.blog.platform.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 缓存预热服务
 * 在应用启动时预加载热点数据
 */
@Service
public class CacheWarmupService implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheWarmupService.class);
    
    private final CacheService cacheService;
    private final Executor warmupExecutor;
    
    public CacheWarmupService(CacheService cacheService) {
        this.cacheService = cacheService;
        this.warmupExecutor = Executors.newFixedThreadPool(3);
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("开始缓存预热...");
        
        CompletableFuture<Void> warmupFuture = CompletableFuture.allOf(
            warmupHotArticles(),
            warmupPopularUsers(),
            warmupSystemConfig()
        );
        
        warmupFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.error("缓存预热失败", throwable);
            } else {
                logger.info("缓存预热完成");
            }
        });
    }
    
    /**
     * 预热热门文章
     */
    private CompletableFuture<Void> warmupHotArticles() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("预热热门文章缓存...");
                
                // 模拟预热热门文章数据
                // 在实际实现中，这里会从数据库查询热门文章并缓存
                for (int i = 1; i <= 10; i++) {
                    String articleKey = "hot_article:" + i;
                    String articleData = "热门文章数据 " + i;
                    cacheService.set(articleKey, articleData, Duration.ofHours(2));
                }
                
                logger.info("热门文章缓存预热完成");
            } catch (Exception e) {
                logger.error("热门文章缓存预热失败", e);
            }
        }, warmupExecutor);
    }
    
    /**
     * 预热热门用户
     */
    private CompletableFuture<Void> warmupPopularUsers() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("预热热门用户缓存...");
                
                // 模拟预热热门用户数据
                // 在实际实现中，这里会从数据库查询热门用户并缓存
                for (int i = 1; i <= 5; i++) {
                    String userKey = "popular_user:" + i;
                    String userData = "热门用户数据 " + i;
                    cacheService.set(userKey, userData, Duration.ofHours(1));
                }
                
                logger.info("热门用户缓存预热完成");
            } catch (Exception e) {
                logger.error("热门用户缓存预热失败", e);
            }
        }, warmupExecutor);
    }
    
    /**
     * 预热系统配置
     */
    private CompletableFuture<Void> warmupSystemConfig() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("预热系统配置缓存...");
                
                // 预热系统配置数据
                cacheService.set("system:config:max_article_length", 10000, Duration.ofHours(24));
                cacheService.set("system:config:max_comment_length", 500, Duration.ofHours(24));
                cacheService.set("system:config:rate_limit_per_minute", 60, Duration.ofHours(24));
                
                logger.info("系统配置缓存预热完成");
            } catch (Exception e) {
                logger.error("系统配置缓存预热失败", e);
            }
        }, warmupExecutor);
    }
    
    /**
     * 手动触发缓存预热
     */
    public void manualWarmup() {
        logger.info("手动触发缓存预热...");
        try {
            run(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 清理所有缓存
     */
    public void clearAllCache() {
        logger.info("清理所有缓存...");
        cacheService.deleteByPattern("*");
        logger.info("缓存清理完成");
    }
}