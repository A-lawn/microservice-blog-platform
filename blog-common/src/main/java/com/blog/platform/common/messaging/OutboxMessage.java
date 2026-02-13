package com.blog.platform.common.messaging;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
public class OutboxMessage {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(name = "aggregate_type", length = 100, nullable = false)
    private String aggregateType;
    
    @Column(name = "aggregate_id", length = 100, nullable = false)
    private String aggregateId;
    
    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;
    
    @Column(name = "target_topic", length = 100, nullable = false)
    private String targetTopic;
    
    @Column(name = "message_key", length = 100)
    private String messageKey;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MessageStatus status = MessageStatus.PENDING;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "max_retry", nullable = false)
    private Integer maxRetry = 5;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    public enum MessageStatus {
        PENDING,
        PROCESSING,
        SENT,
        FAILED,
        DEAD_LETTER
    }
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (nextRetryAt == null) {
            nextRetryAt = createdAt;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void markAsProcessing() {
        this.status = MessageStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsSent() {
        this.status = MessageStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String error) {
        this.retryCount++;
        this.lastError = error;
        this.updatedAt = LocalDateTime.now();
        
        if (retryCount >= maxRetry) {
            this.status = MessageStatus.DEAD_LETTER;
        } else {
            this.status = MessageStatus.FAILED;
            this.nextRetryAt = LocalDateTime.now().plusMinutes(calculateBackoff());
        }
    }
    
    private long calculateBackoff() {
        return (long) Math.pow(2, retryCount) * 1;
    }
    
    public boolean shouldRetry() {
        return status == MessageStatus.PENDING || 
               (status == MessageStatus.FAILED && retryCount < maxRetry && 
                nextRetryAt != null && nextRetryAt.isBefore(LocalDateTime.now()));
    }
    
    public OutboxMessage() {}
    
    public OutboxMessage(String aggregateType, String aggregateId, String eventType, 
                         String payload, String targetTopic, String messageKey) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.targetTopic = targetTopic;
        this.messageKey = messageKey;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getTargetTopic() { return targetTopic; }
    public void setTargetTopic(String targetTopic) { this.targetTopic = targetTopic; }
    public String getMessageKey() { return messageKey; }
    public void setMessageKey(String messageKey) { this.messageKey = messageKey; }
    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public Integer getMaxRetry() { return maxRetry; }
    public void setMaxRetry(Integer maxRetry) { this.maxRetry = maxRetry; }
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
