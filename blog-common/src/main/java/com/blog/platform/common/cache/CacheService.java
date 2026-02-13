package com.blog.platform.common.cache;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private final AtomicBoolean redisAvailable = new AtomicBoolean(false);
    
    private static final String CACHE_PREFIX = "blog:cache:";
    
    private static final Duration DEFAULT_EXPIRE_TIME = Duration.ofHours(1);
    
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();
    
    private static class CacheEntry {
        final Object value;
        final long expireTime;
        
        CacheEntry(Object value, Duration ttl) {
            this.value = value;
            this.expireTime = System.currentTimeMillis() + ttl.toMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
                logger.warn("Redis不可用，将使用本地缓存作为降级方案");
            } else {
                logger.info("Redis连接成功");
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
        set(key, value, DEFAULT_EXPIRE_TIME);
    }
    
    public void set(String key, Object value, Duration expireTime) {
        String cacheKey = buildCacheKey(key);
        
        if (redisAvailable.get()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, value, expireTime);
                return;
            } catch (Exception e) {
                logger.warn("Redis写入失败，降级到本地缓存: {}", e.getMessage());
            }
        }
        
        localCache.put(cacheKey, new CacheEntry(value, expireTime));
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        String cacheKey = buildCacheKey(key);
        
        if (redisAvailable.get()) {
            try {
                Object value = redisTemplate.opsForValue().get(cacheKey);
                return value != null ? (T) value : null;
            } catch (Exception e) {
                logger.warn("Redis读取失败，尝试本地缓存: {}", e.getMessage());
            }
        }
        
        CacheEntry entry = localCache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            return (T) entry.value;
        }
        
        if (entry != null) {
            localCache.remove(cacheKey);
        }
        
        return null;
    }
    
    public void delete(String key) {
        String cacheKey = buildCacheKey(key);
        
        if (redisAvailable.get()) {
            try {
                redisTemplate.delete(cacheKey);
            } catch (Exception e) {
                logger.warn("Redis删除失败: {}", e.getMessage());
            }
        }
        
        localCache.remove(cacheKey);
    }
    
    public void deleteByPattern(String pattern) {
        String cachePattern = buildCacheKey(pattern);
        
        if (redisAvailable.get()) {
            try {
                Set<String> keys = redisTemplate.keys(cachePattern);
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                logger.warn("Redis批量删除失败: {}", e.getMessage());
            }
        }
        
        localCache.keySet().removeIf(k -> k.startsWith(cachePattern.replace("*", "")));
    }
    
    public boolean exists(String key) {
        String cacheKey = buildCacheKey(key);
        
        if (redisAvailable.get()) {
            try {
                return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
            } catch (Exception e) {
                logger.warn("Redis检查存在失败: {}", e.getMessage());
            }
        }
        
        CacheEntry entry = localCache.get(cacheKey);
        return entry != null && !entry.isExpired();
    }
    
    public void expire(String key, Duration expireTime) {
        String cacheKey = buildCacheKey(key);
        
        if (redisAvailable.get()) {
            try {
                redisTemplate.expire(cacheKey, expireTime);
            } catch (Exception e) {
                logger.warn("Redis设置过期时间失败: {}", e.getMessage());
            }
        }
        
        CacheEntry entry = localCache.get(cacheKey);
        if (entry != null) {
            localCache.put(cacheKey, new CacheEntry(entry.value, expireTime));
        }
    }
    
    public long getExpire(String key) {
        String cacheKey = buildCacheKey(key);
        
        if (redisAvailable.get()) {
            try {
                return redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.warn("Redis获取过期时间失败: {}", e.getMessage());
            }
        }
        
        CacheEntry entry = localCache.get(cacheKey);
        if (entry != null) {
            return Math.max(0, (entry.expireTime - System.currentTimeMillis()) / 1000);
        }
        
        return -2;
    }
    
    public void setNullValue(String key) {
        setNullValue(key, Duration.ofMinutes(5));
    }
    
    public void setNullValue(String key, Duration expireTime) {
        String cacheKey = buildCacheKey(key);
        
        if (redisAvailable.get()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, "NULL", expireTime);
                return;
            } catch (Exception e) {
                logger.warn("Redis设置空值失败: {}", e.getMessage());
            }
        }
        
        localCache.put(cacheKey, new CacheEntry("NULL", expireTime));
    }
    
    public boolean isNullValue(String key) {
        String cacheKey = buildCacheKey(key);
        
        if (redisAvailable.get()) {
            try {
                Object value = redisTemplate.opsForValue().get(cacheKey);
                return "NULL".equals(value);
            } catch (Exception e) {
                logger.warn("Redis检查空值失败: {}", e.getMessage());
            }
        }
        
        CacheEntry entry = localCache.get(cacheKey);
        return entry != null && !entry.isExpired() && "NULL".equals(entry.value);
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
        
        CacheEntry existingLock = localCache.get(cacheKey);
        if (existingLock == null || existingLock.isExpired()) {
            localCache.put(cacheKey, new CacheEntry(lockValue, expireTime));
            return true;
        }
        
        return false;
    }
    
    private static final RedisScript<Integer> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) else return 0 end",
            Integer.class
    );

    public void releaseLock(String lockKey, String lockValue) {
        String cacheKey = buildCacheKey("lock:" + lockKey);
        
        if (redisAvailable.get()) {
            try {
                redisTemplate.execute(RELEASE_LOCK_SCRIPT, Collections.singletonList(cacheKey), lockValue);
                return;
            } catch (Exception e) {
                logger.warn("Redis释放锁失败: {}", e.getMessage());
            }
        }
        
        CacheEntry entry = localCache.get(cacheKey);
        if (entry != null && lockValue.equals(entry.value)) {
            localCache.remove(cacheKey);
        }
    }
    
    public boolean isRedisAvailable() {
        return redisAvailable.get();
    }
    
    private String buildCacheKey(String key) {
        return CACHE_PREFIX + key;
    }
}
