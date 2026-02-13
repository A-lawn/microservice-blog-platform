package com.blog.platform.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类
 * 所有领域事件都应该继承此基类
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public abstract class DomainEvent {
    
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String aggregateId;
    private final String eventType;
    
    protected DomainEvent(String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.eventType = this.getClass().getSimpleName();
    }
    
    public String getEventId() {
        return eventId;
    }
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DomainEvent that = (DomainEvent) obj;
        return eventId.equals(that.eventId);
    }
    
    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s{eventId='%s', aggregateId='%s', occurredOn=%s}", 
                eventType, eventId, aggregateId, occurredOn);
    }
}