package com.blog.platform.common.domain.user;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    
    /**
     * 活跃状态
     */
    ACTIVE("活跃"),
    
    /**
     * 非活跃状态
     */
    INACTIVE("非活跃"),
    
    /**
     * 被禁用状态
     */
    BANNED("被禁用");
    
    private final String description;
    
    UserStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    public boolean canLogin() {
        return this == ACTIVE;
    }
    
    public boolean canPost() {
        return this == ACTIVE;
    }
}