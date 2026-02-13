package com.blog.platform.common.infrastructure;

import com.blog.platform.common.domain.DomainEvent;
import com.blog.platform.common.domain.DomainEventHandler;
import com.blog.platform.common.domain.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 基于Spring的领域事件发布器实现
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringDomainEventPublisher.class);
    
    private final ApplicationContext applicationContext;
    
    public SpringDomainEventPublisher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void publish(DomainEvent event) {
        logger.debug("Publishing domain event: {}", event);
        
        try {
            // 获取所有事件处理器
            Map<String, DomainEventHandler> handlers = applicationContext.getBeansOfType(DomainEventHandler.class);
            
            // 找到能处理此事件的处理器并执行
            for (DomainEventHandler handler : handlers.values()) {
                if (handler.canHandle(event)) {
                    logger.debug("Handling event {} with handler {}", event.getEventType(), handler.getClass().getSimpleName());
                    handler.handle(event);
                }
            }
            
            logger.debug("Successfully published domain event: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Failed to publish domain event: {}", event, e);
            throw new DomainEventPublishException("Failed to publish domain event: " + event.getEventId(), e);
        }
    }
    
    @Override
    public void publishAll(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        logger.debug("Publishing {} domain events", events.size());
        
        for (DomainEvent event : events) {
            publish(event);
        }
    }
    
    /**
     * 领域事件发布异常
     */
    public static class DomainEventPublishException extends RuntimeException {
        public DomainEventPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}