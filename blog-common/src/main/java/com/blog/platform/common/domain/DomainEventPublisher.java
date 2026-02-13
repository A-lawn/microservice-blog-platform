package com.blog.platform.common.domain;

import java.util.List;

/**
 * 领域事件发布器接口
 */
public interface DomainEventPublisher {
    
    /**
     * 发布单个领域事件
     */
    void publish(DomainEvent event);
    
    /**
     * 发布多个领域事件
     */
    void publishAll(List<DomainEvent> events);
    
    /**
     * 发布聚合根中的所有领域事件
     */
    default void publishEvents(AggregateRoot<?> aggregateRoot) {
        if (aggregateRoot.hasDomainEvents()) {
            publishAll(aggregateRoot.getDomainEvents());
            aggregateRoot.clearDomainEvents();
        }
    }
}