package com.blog.platform.comment.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 配置重试管理器
 * 实现配置加载的重试和超时机制
 */
@Component
public class ConfigRetryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigRetryManager.class);
    
    @Value("${config.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${config.retry.initial-delay:1000}")
    private long initialDelayMs;
    
    @Value("${config.retry.max-delay:10000}")
    private long maxDelayMs;
    
    @Value("${config.retry.multiplier:2.0}")
    private double backoffMultiplier;
    
    @Value("${config.retry.timeout:30000}")
    private long timeoutMs;
    
    /**
     * 执行带重试的配置操作
     */
    public <T> T executeWithRetry(Callable<T> operation, String operationName) throws Exception {
        return executeWithRetry(operation, operationName, maxRetryAttempts);
    }
    
    /**
     * 执行带重试的配置操作（指定重试次数）
     */
    public <T> T executeWithRetry(Callable<T> operation, String operationName, int maxAttempts) throws Exception {
        Exception lastException = null;
        long delay = initialDelayMs;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                logger.debug("Executing {} - attempt {}/{}", operationName, attempt, maxAttempts);
                
                // 使用超时机制执行操作
                CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return operation.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                
                T result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
                
                if (attempt > 1) {
                    logger.info("Operation {} succeeded on attempt {}/{}", operationName, attempt, maxAttempts);
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                logger.warn("Operation {} failed on attempt {}/{}: {}", 
                    operationName, attempt, maxAttempts, e.getMessage());
                
                if (attempt < maxAttempts) {
                    try {
                        logger.debug("Waiting {}ms before retry", delay);
                        Thread.sleep(delay);
                        
                        // 指数退避
                        delay = Math.min((long) (delay * backoffMultiplier), maxDelayMs);
                        
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        logger.error("Operation {} failed after {} attempts", operationName, maxAttempts);
        throw new ConfigRetryException("Operation failed after " + maxAttempts + " attempts", lastException);
    }
    
    /**
     * 异步执行带重试的配置操作
     */
    public <T> CompletableFuture<T> executeWithRetryAsync(Callable<T> operation, String operationName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeWithRetry(operation, operationName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 执行带超时的操作
     */
    public <T> T executeWithTimeout(Callable<T> operation, String operationName, long timeoutMs) throws Exception {
        logger.debug("Executing {} with timeout {}ms", operationName, timeoutMs);
        
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                return operation.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            logger.error("Operation {} timed out after {}ms", operationName, timeoutMs);
            throw new ConfigTimeoutException("Operation timed out after " + timeoutMs + "ms", e);
        }
    }
    
    /**
     * 检查配置中心连接性
     */
    public boolean checkConfigCenterConnectivity() {
        try {
            return executeWithRetry(() -> {
                // 这里可以实现具体的连接检查逻辑
                // 例如调用Nacos的健康检查接口
                return pingConfigCenter();
            }, "config-center-ping", 2);
        } catch (Exception e) {
            logger.warn("Config center connectivity check failed", e);
            return false;
        }
    }
    
    private boolean pingConfigCenter() {
        // 简单的连接检查实现
        // 在实际环境中，这里应该调用Nacos的健康检查API
        try {
            // 模拟网络检查
            Thread.sleep(100);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * 配置重试异常
     */
    public static class ConfigRetryException extends Exception {
        public ConfigRetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * 配置超时异常
     */
    public static class ConfigTimeoutException extends Exception {
        public ConfigTimeoutException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * 重试统计信息
     */
    public static class RetryStats {
        private final String operationName;
        private final int totalAttempts;
        private final long totalDuration;
        private final boolean successful;
        private final LocalDateTime timestamp;
        
        public RetryStats(String operationName, int totalAttempts, long totalDuration, boolean successful) {
            this.operationName = operationName;
            this.totalAttempts = totalAttempts;
            this.totalDuration = totalDuration;
            this.successful = successful;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public String getOperationName() { return operationName; }
        public int getTotalAttempts() { return totalAttempts; }
        public long getTotalDuration() { return totalDuration; }
        public boolean isSuccessful() { return successful; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("RetryStats{operation='%s', attempts=%d, duration=%dms, successful=%s, time=%s}",
                operationName, totalAttempts, totalDuration, successful, timestamp);
        }
    }
}