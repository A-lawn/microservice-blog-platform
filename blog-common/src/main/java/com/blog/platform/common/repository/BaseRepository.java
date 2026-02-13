package com.blog.platform.common.repository;

import com.blog.platform.common.domain.AggregateRoot;
import com.blog.platform.common.domain.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository基类实现
 * 提供领域事件发布的通用逻辑
 */
public abstract class BaseRepository<T extends AggregateRoot<ID>, ID> implements Repository<T, ID> {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseRepository.class);
    
    protected final DomainEventPublisher eventPublisher;
    
    protected BaseRepository(DomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public T save(T aggregate) {
        logger.debug("Saving aggregate: {}", aggregate.getId());
        
        // 执行具体的保存逻辑
        T savedAggregate = doSave(aggregate);
        
        // 发布领域事件
        if (savedAggregate.hasDomainEvents()) {
            logger.debug("Publishing {} domain events for aggregate: {}", 
                    savedAggregate.getDomainEvents().size(), savedAggregate.getId());
            eventPublisher.publishEvents(savedAggregate);
        }
        
        return savedAggregate;
    }
    
    @Override
    public void delete(T aggregate) {
        logger.debug("Deleting aggregate: {}", aggregate.getId());
        
        // 执行具体的删除逻辑
        doDelete(aggregate);
        
        // 发布领域事件
        if (aggregate.hasDomainEvents()) {
            logger.debug("Publishing {} domain events for deleted aggregate: {}", 
                    aggregate.getDomainEvents().size(), aggregate.getId());
            eventPublisher.publishEvents(aggregate);
        }
    }
    
    /**
     * 子类实现具体的保存逻辑
     */
    protected abstract T doSave(T aggregate);
    
    /**
     * 子类实现具体的删除逻辑
     */
    protected abstract void doDelete(T aggregate);
}