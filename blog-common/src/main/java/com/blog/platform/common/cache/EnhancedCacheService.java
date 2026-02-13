package com.blog.platform.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EnhancedCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedCacheService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final AtomicBoolean redisAvailable = new AtomicBoolean(false);
    
    private static final String CACHE_PREFIX = "blog:cache:";
    
    private final Cache<String, CacheEntry> localCache;
    
    private static class CacheEntry {
        final Object value;
        final long expireTimeNanos;
        
        CacheEntry(Object value, Duration ttl) {
            this.value = value;
            this.expireTimeNanos = System.nanoTime() + ttl.toNanos();
        }
        
        boolean isExpired() {
            return System.nanoTime() > expireTimeNanos;
        }
        
        long remainingNanos() {
            return Math.max(0, expireTimeNanos - System.nanoTime());
        }
    }
    
    public EnhancedCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        this.localCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfter(new Expiry<String, CacheEntry>() {
                @Override
                public long expireAfterCreate(String key, CacheEntry value, long currentTime) {
                    return value.remainingNanos();
                }
                
                @Override
                public long expireAfterUpdate(String key, CacheEntry value, long currentTime, long currentDuration) {
                    return value.remainingNanos();
                }
                
                @Override
                public long expireAfterRead(String key, CacheEntry value, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .recordStats()
            .build();
    }
    
    @PostConstruct
    public void init() {
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            boolean available = checkRedisConnection();
            redisAvailable.set(available);
            if (!available) {
                logger.warn("Redis不可用，使用Caffeine本地缓存作为降级方案");
            } else {
                logger.info("Redis可用，使用Redis + Caffeine二级缓存");
            }
        });
    }
    
    private boolean checkRedisConnection() {
        try {
            if (redisTemplate == null || redisTemplate.getConnectionFactory() == null) {
                return false;
            }
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            logger.debug("Redis连接检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    public void set(String key, Object value) {
        set(key, value, Duration.ofHours(1));
    }
    
    public void set(String key, Object value, Duration expireTime) {
        String cacheKey = buildCacheKey(key);
        CacheEntry entry = new CacheEntry(value, expireTime);
        
        localCache.put(cacheKey, entry);
        
        if (redisAvailable.get()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, value, expireTime);
            } catch (Exception e) {
                logger.warn("Redis写入失败，仅使用本地缓存: {}", e.getMessage());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        String cacheKey = buildCacheKey(key);
        
        CacheEntry localEntry = localCache.getIfPresent(cacheKey);
        if (localEntry != null && !localEntry.isExpired()) {
            logger.debug("Cache hit (local): {}", key);
            return (T) localEntry.value;
        }
        
        if (redisAvailable.get()) {
            try {
                Object value = redisTemplate.opsForValue().get(cacheKey);
                if (value != null) {
                    Long ttl = redisTemplate.getExpire(cacheKey, java.util.concurrent.TimeUnit.SECONDS);
                    if (ttl != null && ttl > 0) {
                        localCache.put(cacheKey, new CacheEntry(value, Duration.ofSeconds(ttl)));
                    }
                    logger.debug("Cache hit (redis): {}", key);
                    return (T) value;
                }
            } catch (Exception e) {
                logger.warn("Redis读取失败: {}", e.getMessage());
            }
        }
        
        logger.debug("Cache miss: {}", key);
        return null;
    }
    
    public <T> T getOrLoad(String key, Class<T> type, CacheLoader<T> loader, Duration ttl) {
        T value = get(key, type);
        if (value != null) {
            return value;
        }
        
        value = loader.load();
        if (value != null) {
            set(key, value, ttl);
        }
        
        return value;
    }
    
    @FunctionalInterface
    public interface CacheLoader<T> {
        T load();
    }
    
    public void delete(String key) {
        String cacheKey = buildCacheKey(key);
        
        localCache.invalidate(cacheKey);
        
        if (redisAvailable.get()) {
            try {
                redisTemplate.delete(cacheKey);
            } catch (Exception e) {
                logger.warn("Redis删除失败: {}", e.getMessage());
            }
        }
    }
    
    public void deleteByPattern(String pattern) {
        String cachePattern = buildCacheKey(pattern);
        
        localCache.asMap().keySet().removeIf(k -> k.startsWith(cachePattern.replace("*", "")));
        
        if (redisAvailable.get()) {
            try {
                var keys = redisTemplate.keys(cachePattern);
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                logger.warn("Redis批量删除失败: {}", e.getMessage());
            }
        }
    }
    
    public boolean exists(String key) {
        String cacheKey = buildCacheKey(key);
        
        CacheEntry entry = localCache.getIfPresent(cacheKey);
        if (entry != null && !entry.isExpired()) {
            return true;
        }
        
        if (redisAvailable.get()) {
            try {
                return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
            } catch (Exception e) {
                logger.warn("Redis检查存在失败: {}", e.getMessage());
            }
        }
        
        return false;
    }
    
    public boolean tryLock(String lockKey, String lockValue, Duration expireTime) {
        String cacheKey = buildCacheKey("lock:" + lockKey);
        
        if (redisAvailable.get()) {
            try {
                return Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(cacheKey, lockValue, expireTime)
                );
            } catch (Exception e) {
                logger.warn("Redis获取锁失败: {}", e.getMessage());
            }
        }
        
        CacheEntry existingLock = localCache.getIfPresent(cacheKey);
        if (existingLock == null || existingLock.isExpired()) {
            localCache.put(cacheKey, new CacheEntry(lockValue, expireTime));
            return true;
        }
        
        return false;
    }
    
    public void releaseLock(String lockKey, String lockValue) {
        String cacheKey = buildCacheKey("lock:" + lockKey);
        
        if (redisAvailable.get()) {
            try {
                Object value = redisTemplate.opsForValue().get(cacheKey);
                if (lockValue.equals(value)) {
                    redisTemplate.delete(cacheKey);
                }
                return;
            } catch (Exception e) {
                logger.warn("Redis释放锁失败: {}", e.getMessage());
            }
        }
        
        CacheEntry entry = localCache.getIfPresent(cacheKey);
        if (entry != null && lockValue.equals(entry.value)) {
            localCache.invalidate(cacheKey);
        }
    }
    
    public boolean isRedisAvailable() {
        return redisAvailable.get();
    }
    
    public CacheStats getStats() {
        var stats = localCache.stats();
        return new CacheStats(
            stats.hitCount(),
            stats.missCount(),
            stats.evictionCount(),
            localCache.estimatedSize(),
            redisAvailable.get()
        );
    }
    
    public record CacheStats(
        long hitCount,
        long missCount,
        long evictionCount,
        long size,
        boolean redisAvailable
    ) {
        public double hitRate() {
            long total = hitCount + missCount;
            return total > 0 ? (double) hitCount / total : 0.0;
        }
    }
    
    private String buildCacheKey(String key) {
        return CACHE_PREFIX + key;
    }
}
