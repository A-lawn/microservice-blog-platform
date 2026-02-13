package com.blog.platform.common.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class InfrastructureHealthService {
    
    private static final Logger logger = LoggerFactory.getLogger(InfrastructureHealthService.class);
    
    private final AtomicBoolean redisAvailable = new AtomicBoolean(false);
    private final AtomicBoolean rocketMqAvailable = new AtomicBoolean(false);
    
    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @PostConstruct
    public void checkInfrastructure() {
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {}
            checkRedis();
            checkRocketMQ();
            logStatus();
        });
    }
    
    private void checkRedis() {
        if (redisConnectionFactory == null) {
            logger.warn("Redis未配置，缓存功能将被禁用");
            return;
        }
        
        try {
            redisConnectionFactory.getConnection().ping();
            redisAvailable.set(true);
            logger.info("Redis连接正常");
        } catch (Exception e) {
            redisAvailable.set(false);
            logger.warn("Redis连接失败: {}，缓存功能将被禁用", e.getMessage());
        }
    }
    
    private void checkRocketMQ() {
        try {
            Class<?> rocketMQTemplateClass = Class.forName("org.apache.rocketmq.spring.core.RocketMQTemplate");
            if (rocketMQTemplateClass != null) {
                rocketMqAvailable.set(true);
                logger.info("RocketMQ已配置");
            }
        } catch (ClassNotFoundException e) {
            logger.warn("RocketMQ未配置，消息队列功能将被禁用");
            rocketMqAvailable.set(false);
        }
    }
    
    private void logStatus() {
        logger.info("=== 基础设施状态 ===");
        logger.info("Redis: {}", redisAvailable.get() ? "可用" : "不可用");
        logger.info("RocketMQ: {}", rocketMqAvailable.get() ? "可用" : "不可用");
        logger.info("==================");
    }
    
    public boolean isRedisAvailable() {
        return redisAvailable.get();
    }
    
    public boolean isRocketMqAvailable() {
        return rocketMqAvailable.get();
    }
    
    public void refreshRedisStatus() {
        Thread.ofVirtual().start(this::checkRedis);
    }
    
    public void refreshRocketMQStatus() {
        Thread.ofVirtual().start(this::checkRocketMQ);
    }
}
