package com.blog.platform.common.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性服务
 * 确保消息处理的幂等性，防止重复处理
 */
@Service
public class IdempotencyService {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyService.class);

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    // 24小时过期
    private static final long DEFAULT_EXPIRATION_HOURS = 24;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 检查消息是否已经处理过
     * @param messageId 消息ID
     * @param topic 主题
     * @param consumerGroup 消费者组
     * @return true表示已处理过，false表示未处理过
     */
    public boolean isProcessed(String messageId, String topic, String consumerGroup) {
        String key = buildIdempotencyKey(messageId, topic, consumerGroup);
        try {
            Boolean exists = redisTemplate.hasKey(key);
            boolean processed = exists != null && exists;
            
            if (processed) {
                logger.info("Message already processed: messageId={}, topic={}, consumerGroup={}", 
                           messageId, topic, consumerGroup);
            }
            
            return processed;
        } catch (Exception e) {
            logger.error("Failed to check idempotency for message: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup, e);
            // 如果Redis不可用，为了安全起见，假设消息未处理过
            return false;
        }
    }

    /**
     * 标记消息为已处理
     * @param messageId 消息ID
     * @param topic 主题
     * @param consumerGroup 消费者组
     * @param result 处理结果
     */
    public void markAsProcessed(String messageId, String topic, String consumerGroup, String result) {
        String key = buildIdempotencyKey(messageId, topic, consumerGroup);
        try {
            redisTemplate.opsForValue().set(key, result, DEFAULT_EXPIRATION_HOURS, TimeUnit.HOURS);
            logger.debug("Message marked as processed: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup);
        } catch (Exception e) {
            logger.error("Failed to mark message as processed: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup, e);
            // 标记失败不影响业务流程，只记录日志
        }
    }

    /**
     * 标记消息为已处理（带自定义过期时间）
     * @param messageId 消息ID
     * @param topic 主题
     * @param consumerGroup 消费者组
     * @param result 处理结果
     * @param expiration 过期时间
     */
    public void markAsProcessed(String messageId, String topic, String consumerGroup, 
                              String result, Duration expiration) {
        String key = buildIdempotencyKey(messageId, topic, consumerGroup);
        try {
            redisTemplate.opsForValue().set(key, result, expiration.toMillis(), TimeUnit.MILLISECONDS);
            logger.debug("Message marked as processed with custom expiration: messageId={}, topic={}, consumerGroup={}, expiration={}", 
                        messageId, topic, consumerGroup, expiration);
        } catch (Exception e) {
            logger.error("Failed to mark message as processed: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup, e);
        }
    }

    /**
     * 获取消息处理结果
     * @param messageId 消息ID
     * @param topic 主题
     * @param consumerGroup 消费者组
     * @return 处理结果，如果未处理过则返回null
     */
    public String getProcessingResult(String messageId, String topic, String consumerGroup) {
        String key = buildIdempotencyKey(messageId, topic, consumerGroup);
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Failed to get processing result for message: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup, e);
            return null;
        }
    }

    /**
     * 删除幂等性记录（用于测试或特殊情况）
     * @param messageId 消息ID
     * @param topic 主题
     * @param consumerGroup 消费者组
     */
    public void removeIdempotencyRecord(String messageId, String topic, String consumerGroup) {
        String key = buildIdempotencyKey(messageId, topic, consumerGroup);
        try {
            redisTemplate.delete(key);
            logger.debug("Idempotency record removed: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup);
        } catch (Exception e) {
            logger.error("Failed to remove idempotency record: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup, e);
        }
    }

    /**
     * 构建幂等性键
     */
    private String buildIdempotencyKey(String messageId, String topic, String consumerGroup) {
        return IDEMPOTENCY_KEY_PREFIX + topic + ":" + consumerGroup + ":" + messageId;
    }

    /**
     * 原子性检查并标记操作
     * 如果消息未处理过，则标记为处理中，返回true
     * 如果消息已处理过，返回false
     * @param messageId 消息ID
     * @param topic 主题
     * @param consumerGroup 消费者组
     * @return true表示可以处理，false表示已处理过
     */
    public boolean checkAndMarkAsProcessing(String messageId, String topic, String consumerGroup) {
        String key = buildIdempotencyKey(messageId, topic, consumerGroup);
        try {
            // 使用Redis的SETNX命令实现原子性检查和设置
            Boolean success = redisTemplate.opsForValue().setIfAbsent(
                key, "PROCESSING", DEFAULT_EXPIRATION_HOURS, TimeUnit.HOURS);
            
            boolean canProcess = success != null && success;
            
            if (canProcess) {
                logger.debug("Message marked as processing: messageId={}, topic={}, consumerGroup={}", 
                            messageId, topic, consumerGroup);
            } else {
                logger.info("Message already being processed or processed: messageId={}, topic={}, consumerGroup={}", 
                           messageId, topic, consumerGroup);
            }
            
            return canProcess;
        } catch (Exception e) {
            logger.error("Failed to check and mark message as processing: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup, e);
            // 如果Redis不可用，为了安全起见，允许处理
            return true;
        }
    }

    /**
     * 更新处理状态为完成
     * @param messageId 消息ID
     * @param topic 主题
     * @param consumerGroup 消费者组
     * @param result 处理结果
     */
    public void markAsCompleted(String messageId, String topic, String consumerGroup, String result) {
        String key = buildIdempotencyKey(messageId, topic, consumerGroup);
        try {
            redisTemplate.opsForValue().set(key, "COMPLETED:" + result, DEFAULT_EXPIRATION_HOURS, TimeUnit.HOURS);
            logger.debug("Message marked as completed: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup);
        } catch (Exception e) {
            logger.error("Failed to mark message as completed: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup, e);
        }
    }

    /**
     * 标记处理失败
     * @param messageId 消息ID
     * @param topic 主题
     * @param consumerGroup 消费者组
     * @param error 错误信息
     */
    public void markAsFailed(String messageId, String topic, String consumerGroup, String error) {
        String key = buildIdempotencyKey(messageId, topic, consumerGroup);
        try {
            // 失败记录保存较短时间，允许重试
            redisTemplate.opsForValue().set(key, "FAILED:" + error, 1, TimeUnit.HOURS);
            logger.debug("Message marked as failed: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup);
        } catch (Exception e) {
            logger.error("Failed to mark message as failed: messageId={}, topic={}, consumerGroup={}", 
                        messageId, topic, consumerGroup, e);
        }
    }
}