package com.blog.platform.common.domain.user;

import com.blog.platform.common.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * 用户ID值对象
 */
public class UserId extends ValueObject {
    
    private final String value;
    
    public UserId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        this.value = value.trim();
        validate();
    }
    
    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }
    
    public static UserId of(String value) {
        return new UserId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    protected void validate() {
        // 可以添加更多的验证逻辑，比如格式验证
        if (value.length() > 50) {
            throw new IllegalArgumentException("用户ID长度不能超过50个字符");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserId userId = (UserId) obj;
        return Objects.equals(value, userId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}