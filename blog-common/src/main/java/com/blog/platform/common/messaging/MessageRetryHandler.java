package com.blog.platform.common.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 消息重试处理器
 * 处理消息消费失败的重试逻辑
 */
@Component
public class MessageRetryHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageRetryHandler.class);

    /**
     * 处理消息重试
     * @param message 消息内容
     * @param topic 主题
     * @param retryTimes 重试次数
     * @return 是否需要继续重试
     */
    public boolean handleRetry(Object message, String topic, int retryTimes) {
        logger.warn("Message consumption failed, retry times: {}, topic: {}, message: {}", 
                   retryTimes, topic, message);

        if (retryTimes >= MessageConstants.MAX_RETRY_TIMES) {
            logger.error("Message consumption failed after {} retries, sending to DLQ. Topic: {}, Message: {}", 
                        MessageConstants.MAX_RETRY_TIMES, topic, message);
            sendToDeadLetterQueue(message, topic);
            return false;
        }

        // 根据重试次数设置不同的延时级别
        int delayLevel = getDelayLevel(retryTimes);
        logger.info("Scheduling message retry with delay level: {}, topic: {}", delayLevel, topic);
        
        return true;
    }

    /**
     * 根据重试次数获取延时级别
     */
    private int getDelayLevel(int retryTimes) {
        switch (retryTimes) {
            case 1:
                return MessageConstants.RETRY_DELAY_LEVEL_1;
            case 2:
                return MessageConstants.RETRY_DELAY_LEVEL_2;
            case 3:
                return MessageConstants.RETRY_DELAY_LEVEL_3;
            default:
                return MessageConstants.RETRY_DELAY_LEVEL_4;
        }
    }

    /**
     * 发送消息到死信队列
     */
    private void sendToDeadLetterQueue(Object message, String topic) {
        try {
            String dlqTopic = topic + MessageConstants.DLQ_SUFFIX;
            logger.info("Sending message to dead letter queue: {}", dlqTopic);
            
            // 这里可以实现具体的死信队列发送逻辑
            // 例如：记录到数据库、发送告警等
            recordDeadLetterMessage(message, topic, dlqTopic);
            
        } catch (Exception e) {
            logger.error("Failed to send message to dead letter queue", e);
        }
    }

    /**
     * 记录死信消息
     */
    private void recordDeadLetterMessage(Object message, String originalTopic, String dlqTopic) {
        // 实现死信消息的记录逻辑
        // 可以记录到数据库或发送告警
        logger.error("Dead letter message recorded - Original Topic: {}, DLQ Topic: {}, Message: {}", 
                    originalTopic, dlqTopic, message);
    }

    /**
     * 检查消息是否应该被丢弃
     */
    public boolean shouldDiscardMessage(Object message, String topic) {
        // 实现消息丢弃逻辑
        // 例如：消息过期、格式错误等
        return false;
    }

    /**
     * 获取重试主题名称
     */
    public String getRetryTopic(String originalTopic) {
        return originalTopic + MessageConstants.RETRY_SUFFIX;
    }

    /**
     * 获取死信队列主题名称
     */
    public String getDeadLetterTopic(String originalTopic) {
        return originalTopic + MessageConstants.DLQ_SUFFIX;
    }
}