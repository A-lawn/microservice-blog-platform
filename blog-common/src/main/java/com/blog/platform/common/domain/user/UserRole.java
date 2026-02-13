package com.blog.platform.common.domain.user;

import com.blog.platform.common.domain.ValueObject;

/**
 * 用户角色值对象
 */
public class UserRole extends ValueObject {
    
    public enum Role {
        USER("USER", "普通用户"),
        MODERATOR("MODERATOR", "版主"),
        ADMIN("ADMIN", "管理员");
        
        private final String code;
        private final String description;
        
        Role(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static Role fromCode(String code) {
            for (Role role : values()) {
                if (role.code.equals(code)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Unknown role code: " + code);
        }
    }
    
    private final Role role;
    
    private UserRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("用户角色不能为空");
        }
        this.role = role;
    }
    
    public static UserRole of(Role role) {
        return new UserRole(role);
    }
    
    public static UserRole of(String roleCode) {
        return new UserRole(Role.fromCode(roleCode));
    }
    
    public static UserRole defaultRole() {
        return new UserRole(Role.USER);
    }
    
    public Role getRole() {
        return role;
    }
    
    public String getCode() {
        return role.getCode();
    }
    
    public String getDescription() {
        return role.getDescription();
    }
    
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    public boolean isModerator() {
        return role == Role.MODERATOR;
    }
    
    public boolean isUser() {
        return role == Role.USER;
    }
    
    public boolean hasPermission(String permission) {
        return switch (role) {
            // 管理员拥有所有权限
            case ADMIN -> true;
            case MODERATOR -> permission.startsWith("moderate") ||
                    permission.startsWith("user:read") ||
                    permission.startsWith("article:") ||
                    permission.startsWith("comment:");
            case USER -> permission.startsWith("user:read") ||
                    permission.startsWith("user:write:own") ||
                    permission.startsWith("article:read") ||
                    permission.startsWith("article:write:own") ||
                    permission.startsWith("comment:read") ||
                    permission.startsWith("comment:write");
            default -> false;
        };
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return role.getCode();
    }
}