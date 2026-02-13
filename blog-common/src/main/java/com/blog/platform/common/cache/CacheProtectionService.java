package com.blog.platform.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存保护服务
 * 防止缓存穿透、缓存雪崩、缓存击穿
 */
@Service
public class CacheProtectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheProtectionService.class);
    
    private final CacheService cacheService;
    private final Random random = new Random();
    
    public CacheProtectionService(CacheService cacheService) {
        this.cacheService = cacheService;
    }
    
    /**
     * 防缓存穿透的查询方法
     * 对于不存在的数据，缓存空值防止重复查询数据库
     */
    public <T> T getWithPenetrationProtection(String key, Class<T> type, Supplier<T> dataLoader) {
        return getWithPenetrationProtection(key, type, dataLoader, Duration.ofHours(1));
    }
    
    /**
     * 防缓存穿透的查询方法（带过期时间）
     */
    public <T> T getWithPenetrationProtection(String key, Class<T> type, Supplier<T> dataLoader, Duration expireTime) {
        // 先从缓存获取
        T cachedValue = cacheService.get(key, type);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // 检查是否为空值缓存
        if (cacheService.isNullValue(key)) {
            return null;
        }
        
        // 从数据源加载
        T value = dataLoader.get();
        if (value != null) {
            // 添加随机过期时间防止缓存雪崩
            Duration randomExpireTime = addRandomExpireTime(expireTime);
            cacheService.set(key, value, randomExpireTime);
        } else {
            // 缓存空值防止缓存穿透
            cacheService.setNullValue(key, Duration.ofMinutes(5));
        }
        
        return value;
    }
    
    /**
     * 防缓存击穿的查询方法
     * 使用分布式锁防止热点数据失效时的并发查询
     */
    public <T> T getWithBreakdownProtection(String key, Class<T> type, Supplier<T> dataLoader) {
        return getWithBreakdownProtection(key, type, dataLoader, Duration.ofHours(1));
    }
    
    /**
     * 防缓存击穿的查询方法（带过期时间）
     */
    public <T> T getWithBreakdownProtection(String key, Class<T> type, Supplier<T> dataLoader, Duration expireTime) {
        // 先从缓存获取
        T cachedValue = cacheService.get(key, type);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // 使用分布式锁防止并发查询
        String lockKey = "breakdown_lock:" + key;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        
        try {
            // 尝试获取锁
            if (cacheService.tryLock(lockKey, lockValue, Duration.ofSeconds(10))) {
                try {
                    // 双重检查，防止在等待锁的过程中其他线程已经加载了数据
                    cachedValue = cacheService.get(key, type);
                    if (cachedValue != null) {
                        return cachedValue;
                    }
                    
                    // 从数据源加载
                    T value = dataLoader.get();
                    if (value != null) {
                        Duration randomExpireTime = addRandomExpireTime(expireTime);
                        cacheService.set(key, value, randomExpireTime);
                    } else {
                        cacheService.setNullValue(key);
                    }
                    
                    return value;
                } finally {
                    // 释放锁
                    cacheService.releaseLock(lockKey, lockValue);
                }
            } else {
                // 获取锁失败，等待一段时间后重试
                try {
                    Thread.sleep(50 + random.nextInt(50)); // 50-100ms随机等待
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // 重试获取缓存
                cachedValue = cacheService.get(key, type);
                if (cachedValue != null) {
                    return cachedValue;
                }
                
                // 如果还是没有，直接查询数据库（降级策略）
                logger.warn("获取分布式锁失败，直接查询数据库: {}", key);
                return dataLoader.get();
            }
        } catch (Exception e) {
            logger.error("缓存击穿保护失败: {}", key, e);
            // 异常情况下直接查询数据库
            return dataLoader.get();
        }
    }
    
    /**
     * 综合保护方法
     * 同时防止缓存穿透、击穿和雪崩
     */
    public <T> T getWithFullProtection(String key, Class<T> type, Supplier<T> dataLoader) {
        return getWithFullProtection(key, type, dataLoader, Duration.ofHours(1));
    }
    
    /**
     * 综合保护方法（带过期时间）
     */
    public <T> T getWithFullProtection(String key, Class<T> type, Supplier<T> dataLoader, Duration expireTime) {
        // 先从缓存获取
        T cachedValue = cacheService.get(key, type);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // 检查是否为空值缓存（防穿透）
        if (cacheService.isNullValue(key)) {
            return null;
        }
        
        // 使用分布式锁防止并发查询（防击穿）
        String lockKey = "full_protection_lock:" + key;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        
        try {
            if (cacheService.tryLock(lockKey, lockValue, Duration.ofSeconds(10))) {
                try {
                    // 双重检查
                    cachedValue = cacheService.get(key, type);
                    if (cachedValue != null) {
                        return cachedValue;
                    }
                    
                    if (cacheService.isNullValue(key)) {
                        return null;
                    }
                    
                    // 从数据源加载
                    T value = dataLoader.get();
                    if (value != null) {
                        // 添加随机过期时间（防雪崩）
                        Duration randomExpireTime = addRandomExpireTime(expireTime);
                        cacheService.set(key, value, randomExpireTime);
                    } else {
                        // 缓存空值（防穿透）
                        cacheService.setNullValue(key, Duration.ofMinutes(5));
                    }
                    
                    return value;
                } finally {
                    cacheService.releaseLock(lockKey, lockValue);
                }
            } else {
                // 获取锁失败的降级策略
                try {
                    Thread.sleep(50 + random.nextInt(50));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                cachedValue = cacheService.get(key, type);
                if (cachedValue != null) {
                    return cachedValue;
                }
                
                if (cacheService.isNullValue(key)) {
                    return null;
                }
                
                logger.warn("获取分布式锁失败，直接查询数据库: {}", key);
                return dataLoader.get();
            }
        } catch (Exception e) {
            logger.error("缓存综合保护失败: {}", key, e);
            return dataLoader.get();
        }
    }
    
    /**
     * 异步刷新缓存
     * 在缓存即将过期时异步刷新，避免缓存失效
     */
    public <T> T getWithAsyncRefresh(String key, Class<T> type, Supplier<T> dataLoader, Duration expireTime) {
        T cachedValue = cacheService.get(key, type);
        
        if (cachedValue != null) {
            // 检查缓存剩余时间，如果小于总时间的1/4，异步刷新
            long remainingTime = cacheService.getExpire(key);
            long totalTime = expireTime.getSeconds();
            
            if (remainingTime > 0 && remainingTime < totalTime / 4) {
                // 异步刷新缓存
                CompletableFuture.runAsync(() -> {
                    try {
                        T newValue = dataLoader.get();
                        if (newValue != null) {
                            Duration randomExpireTime = addRandomExpireTime(expireTime);
                            cacheService.set(key, newValue, randomExpireTime);
                            logger.debug("异步刷新缓存完成: {}", key);
                        }
                    } catch (Exception e) {
                        logger.error("异步刷新缓存失败: {}", key, e);
                    }
                });
            }
            
            return cachedValue;
        }
        
        // 缓存不存在，使用综合保护方法
        return getWithFullProtection(key, type, dataLoader, expireTime);
    }
    
    /**
     * 添加随机过期时间防止缓存雪崩
     */
    private Duration addRandomExpireTime(Duration baseExpireTime) {
        long baseSeconds = baseExpireTime.getSeconds();
        // 在基础时间上增加0-20%的随机时间
        long randomSeconds = (long) (baseSeconds * 0.2 * random.nextDouble());
        return Duration.ofSeconds(baseSeconds + randomSeconds);
    }
}