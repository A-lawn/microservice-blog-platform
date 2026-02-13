package com.blog.platform.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

/**
 * 结构化日志工具类
 * 提供统一的结构化日志记录方法
 */
public class StructuredLogger {

    private final Logger logger;

    public StructuredLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static StructuredLogger getLogger(Class<?> clazz) {
        return new StructuredLogger(clazz);
    }

    /**
     * 记录业务操作日志
     */
    public void logBusinessOperation(String operation, String userId, String resourceId, 
                                   String result, long duration, Map<String, String> additionalFields) {
        try {
            MDC.put("operation", operation);
            MDC.put("userId", userId);
            MDC.put("resourceId", resourceId);
            MDC.put("result", result);
            MDC.put("duration", String.valueOf(duration));
            
            if (additionalFields != null) {
                additionalFields.forEach(MDC::put);
            }
            
            logger.info("Business operation completed: {} by user {} on resource {} with result {} in {}ms", 
                       operation, userId, resourceId, result, duration);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录API请求日志
     */
    public void logApiRequest(String method, String path, String userId, 
                            int statusCode, long duration, Map<String, String> additionalFields) {
        try {
            MDC.put("httpMethod", method);
            MDC.put("httpPath", path);
            MDC.put("userId", userId);
            MDC.put("statusCode", String.valueOf(statusCode));
            MDC.put("duration", String.valueOf(duration));
            
            if (additionalFields != null) {
                additionalFields.forEach(MDC::put);
            }
            
            logger.info("API request: {} {} by user {} returned {} in {}ms", 
                       method, path, userId, statusCode, duration);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录错误日志
     */
    public void logError(String operation, String errorCode, String errorMessage, 
                        Throwable throwable, Map<String, String> additionalFields) {
        try {
            MDC.put("operation", operation);
            MDC.put("errorCode", errorCode);
            MDC.put("errorMessage", errorMessage);
            
            if (additionalFields != null) {
                additionalFields.forEach(MDC::put);
            }
            
            if (throwable != null) {
                logger.error("Error in operation {}: {} - {}", operation, errorCode, errorMessage, throwable);
            } else {
                logger.error("Error in operation {}: {} - {}", operation, errorCode, errorMessage);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录性能日志
     */
    public void logPerformance(String operation, long duration, String result, 
                             Map<String, String> additionalFields) {
        try {
            MDC.put("operation", operation);
            MDC.put("duration", String.valueOf(duration));
            MDC.put("result", result);
            
            if (additionalFields != null) {
                additionalFields.forEach(MDC::put);
            }
            
            if (duration > 1000) { // Log as warning if operation takes more than 1 second
                logger.warn("Slow operation detected: {} completed in {}ms with result {}", 
                           operation, duration, result);
            } else {
                logger.info("Performance: {} completed in {}ms with result {}", 
                           operation, duration, result);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录安全相关日志
     */
    public void logSecurity(String event, String userId, String ipAddress, 
                          String userAgent, String result, Map<String, String> additionalFields) {
        try {
            MDC.put("securityEvent", event);
            MDC.put("userId", userId);
            MDC.put("ipAddress", ipAddress);
            MDC.put("userAgent", userAgent);
            MDC.put("result", result);
            
            if (additionalFields != null) {
                additionalFields.forEach(MDC::put);
            }
            
            logger.info("Security event: {} for user {} from {} with result {}", 
                       event, userId, ipAddress, result);
        } finally {
            MDC.clear();
        }
    }

    // Delegate methods to underlying logger
    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }
}