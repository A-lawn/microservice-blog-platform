package com.blog.platform.common.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 * 提供领域事件管理功能
 */
public abstract class AggregateRoot<ID> {
    
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * 获取聚合根ID
     */
    public abstract ID getId();
    
    /**
     * 添加领域事件
     */
    protected void addDomainEvent(DomainEvent event) {
        if (event != null) {
            this.domainEvents.add(event);
        }
    }
    
    /**
     * 获取所有领域事件（只读）
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * 清除所有领域事件
     * 通常在事件发布后调用
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    /**
     * 检查是否有待发布的领域事件
     */
    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AggregateRoot<?> that = (AggregateRoot<?>) obj;
        return getId() != null && getId().equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}