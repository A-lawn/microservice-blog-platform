package com.blog.platform.common.domain.user;

import com.blog.platform.common.domain.DomainEvent;

/**
 * 用户注册事件
 */
public class UserRegisteredEvent extends DomainEvent {
    
    private final String username;
    private final String email;
    
    public UserRegisteredEvent(String userId, String username, String email) {
        super(userId);
        this.username = username;
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    @Override
    public String toString() {
        return String.format("UserRegisteredEvent{userId='%s', username='%s', email='%s', occurredOn=%s}", 
                getAggregateId(), username, email, getOccurredOn());
    }
}