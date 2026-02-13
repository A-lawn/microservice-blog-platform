package com.blog.platform.common.domain;

/**
 * 领域事件处理器接口
 */
public interface DomainEventHandler<T extends DomainEvent> {
    
    /**
     * 处理领域事件
     */
    void handle(T event);
    
    /**
     * 获取处理的事件类型
     */
    Class<T> getEventType();
    
    /**
     * 检查是否可以处理指定的事件
     */
    default boolean canHandle(DomainEvent event) {
        return getEventType().isAssignableFrom(event.getClass());
    }
}