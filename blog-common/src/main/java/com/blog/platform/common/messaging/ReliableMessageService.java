package com.blog.platform.common.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ReliableMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReliableMessageService.class);
    
    @Autowired(required = false)
    private OutboxMessageRepository outboxRepository;
    
    private final ObjectMapper objectMapper;
    private final AtomicBoolean rocketMqAvailable = new AtomicBoolean(false);
    
    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;
    
    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;
    
    @Value("${feature.rocketmq.enabled:true}")
    private boolean rocketMqEnabled;
    
    @Value("${messaging.outbox.enabled:true}")
    private boolean outboxEnabled;
    
    @Value("${messaging.outbox.batch-size:10}")
    private int batchSize;
    
    public ReliableMessageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Transactional
    public void sendMessage(String topic, Object event) {
        sendMessage(topic, event, null);
    }
    
    @Transactional
    public void sendMessage(String topic, Object event, String messageKey) {
        String traceId = MDC.get("traceId");
        
        try {
            String payload = objectMapper.writeValueAsString(event);
            String aggregateId = extractAggregateId(event);
            String eventType = event.getClass().getSimpleName();
            String aggregateType = extractAggregateType(event);
            
            if (isRocketMqAvailable()) {
                try {
                    sendToRocketMQ(topic, payload, messageKey, traceId);
                    logger.info("[RocketMQ] Message sent successfully to topic: {}, traceId: {}", topic, traceId);
                    return;
                } catch (Exception e) {
                    logger.warn("[RocketMQ] Failed to send message, falling back to outbox: {}", e.getMessage());
                    rocketMqAvailable.set(false);
                }
            }
            
            if (outboxEnabled) {
                saveToOutbox(aggregateType, aggregateId, eventType, payload, topic, messageKey, traceId);
                logger.info("[Outbox] Message saved to outbox for topic: {}, traceId: {}", topic, traceId);
            }
        } catch (Exception e) {
            logger.error("Failed to send message to topic: {}, traceId: {}", topic, traceId, e);
            throw new RuntimeException("Failed to send message", e);
        }
    }
    
    private void sendToRocketMQ(String topic, String payload, String messageKey, String traceId) {
        if (rocketMQTemplate == null) {
            throw new IllegalStateException("RocketMQTemplate not available");
        }
        
        var message = MessageBuilder.withPayload(payload)
                .setHeader("traceId", traceId)
                .setHeader("timestamp", System.currentTimeMillis());
        
        if (messageKey != null) {
            message.setHeader("KEYS", messageKey);
        }
        
        rocketMQTemplate.syncSend(topic, message.build());
    }
    
    private void saveToOutbox(String aggregateType, String aggregateId, String eventType,
                              String payload, String topic, String messageKey, String traceId) {
        if (outboxRepository == null) {
            logger.warn("OutboxMessageRepository not available, message will not be persisted");
            return;
        }
        OutboxMessage outboxMessage = new OutboxMessage(
            aggregateType, aggregateId, eventType, payload, topic, messageKey
        );
        outboxRepository.save(outboxMessage);
    }
    
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    @Transactional
    public void processOutboxMessages() {
        if (!outboxEnabled || outboxRepository == null) {
            return;
        }
        
        checkRocketMQAvailability();
        
        if (!isRocketMqAvailable()) {
            return;
        }
        
        List<OutboxMessage> messages = outboxRepository.findPendingMessages(
            LocalDateTime.now(), 
            PageRequest.of(0, batchSize)
        );
        
        if (messages.isEmpty()) {
            return;
        }
        
        logger.info("[Outbox] Processing {} pending messages", messages.size());
        
        for (OutboxMessage message : messages) {
            processMessage(message);
        }
    }
    
    private void processMessage(OutboxMessage message) {
        try {
            message.markAsProcessing();
            outboxRepository.save(message);
            
            sendToRocketMQ(message.getTargetTopic(), message.getPayload(), 
                          message.getMessageKey(), null);
            
            message.markAsSent();
            outboxRepository.save(message);
            
            logger.info("[Outbox] Message {} sent successfully", message.getId());
        } catch (Exception e) {
            message.markAsFailed(e.getMessage());
            outboxRepository.save(message);
            
            logger.error("[Outbox] Failed to send message {}: {}", message.getId(), e.getMessage());
        }
    }
    
    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    @Transactional
    public void cleanupSentMessages() {
        if (!outboxEnabled || outboxRepository == null) {
            return;
        }
        LocalDateTime before = LocalDateTime.now().minusDays(7);
        int deleted = outboxRepository.deleteSentMessagesBefore(before);
        if (deleted > 0) {
            logger.info("[Outbox] Cleaned up {} sent messages older than 7 days", deleted);
        }
    }
    
    private void checkRocketMQAvailability() {
        if (rocketMQTemplate != null && rocketMqEnabled) {
            try {
                rocketMQTemplate.getProducer().getDefaultMQProducerImpl()
                    .getmQClientFactory().getMQClientAPIImpl()
                    .getTopicRouteInfoFromNameServer("TBW102", 3000);
                rocketMqAvailable.set(true);
            } catch (Exception e) {
                logger.debug("RocketMQ availability check failed: {}", e.getMessage());
                rocketMqAvailable.set(false);
            }
        }
    }
    
    public boolean isRocketMqAvailable() {
        return rocketMqEnabled && rocketMQTemplate != null && rocketMqAvailable.get();
    }
    
    public long getPendingMessageCount() {
        if (outboxRepository == null) {
            return 0;
        }
        return outboxRepository.countPendingMessages();
    }
    
    public long getDeadLetterCount() {
        if (outboxRepository == null) {
            return 0;
        }
        return outboxRepository.countDeadLetterMessages();
    }
    
    private String extractAggregateId(Object event) {
        try {
            var method = event.getClass().getMethod("getAggregateId");
            return String.valueOf(method.invoke(event));
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private String extractAggregateType(Object event) {
        String className = event.getClass().getSimpleName();
        if (className.endsWith("Event")) {
            return className.substring(0, className.length() - 5);
        }
        return className;
    }
}
