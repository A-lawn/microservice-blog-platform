package com.blog.platform.common.domain.user;

import com.blog.platform.common.domain.DomainEvent;

/**
 * 用户资料更新事件
 */
public class UserProfileUpdatedEvent extends DomainEvent {
    
    private final String nickname;
    private final String avatar;
    private final String bio;
    
    public UserProfileUpdatedEvent(String userId, String nickname, String avatar, String bio) {
        super(userId);
        this.nickname = nickname;
        this.avatar = avatar;
        this.bio = bio;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public String getBio() {
        return bio;
    }
    
    @Override
    public String toString() {
        return String.format("UserProfileUpdatedEvent{userId='%s', nickname='%s', occurredOn=%s}", 
                getAggregateId(), nickname, getOccurredOn());
    }
}