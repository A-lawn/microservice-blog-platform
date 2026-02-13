package com.blog.platform.common.domain.user;

import com.blog.platform.common.domain.AggregateRoot;

import java.time.LocalDateTime;

/**
 * 用户聚合根
 */
public class User extends AggregateRoot<UserId> {
    
    private final UserId id;
    private Username username;
    private Email email;
    private Password password;
    private UserProfile profile;
    private UserStatus status;
    private UserRole role;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 私有构造函数，强制使用工厂方法
    private User(UserId id, Username username, Email email, Password password, 
                UserProfile profile, UserStatus status, UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.profile = profile;
        this.status = status;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
    
    /**
     * 用户注册工厂方法
     */
    public static User register(Username username, Email email, Password password) {
        UserId userId = UserId.generate();
        UserProfile profile = UserProfile.empty();
        UserStatus status = UserStatus.ACTIVE;
        UserRole role = UserRole.defaultRole();
        LocalDateTime now = LocalDateTime.now();
        
        User user = new User(userId, username, email, password, profile, status, role, now);
        
        // 发布用户注册事件
        user.addDomainEvent(new UserRegisteredEvent(
            userId.getValue(), 
            username.getValue(), 
            email.getValue()
        ));
        
        return user;
    }
    
    /**
     * 从持久化数据重建用户聚合根
     */
    public static User reconstruct(UserId id, Username username, Email email, Password password,
                                 UserProfile profile, UserStatus status, UserRole role,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        User user = new User(id, username, email, password, profile, status, role, createdAt);
        user.updatedAt = updatedAt;
        return user;
    }
    
    /**
     * 更新用户资料
     */
    public void updateProfile(UserProfile newProfile) {
        if (newProfile == null) {
            throw new IllegalArgumentException("用户资料不能为空");
        }
        
        if (!this.status.isActive()) {
            throw new IllegalStateException("非活跃用户不能更新资料");
        }
        
        this.profile = newProfile;
        this.updatedAt = LocalDateTime.now();
        
        // 发布用户资料更新事件
        addDomainEvent(new UserProfileUpdatedEvent(
            id.getValue(),
            newProfile.getNickname(),
            newProfile.getAvatar(),
            newProfile.getBio()
        ));
    }
    
    /**
     * 修改密码
     */
    public void changePassword(Password oldPassword, Password newPassword) {
        if (oldPassword == null || newPassword == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        if (!this.status.isActive()) {
            throw new IllegalStateException("非活跃用户不能修改密码");
        }
        
        if (!this.password.equals(oldPassword)) {
            throw new IllegalArgumentException("原密码不正确");
        }
        
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 验证密码
     */
    public boolean verifyPassword(String rawPassword) {
        return this.password.matches(rawPassword);
    }
    
    /**
     * 激活用户
     */
    public void activate() {
        if (this.status == UserStatus.BANNED) {
            throw new IllegalStateException("被禁用的用户不能直接激活");
        }
        
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 停用用户
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 禁用用户
     */
    public void ban() {
        this.status = UserStatus.BANNED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更改用户角色（仅管理员可操作）
     */
    public void changeRole(UserRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("用户角色不能为空");
        }
        
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查用户是否有特定权限
     */
    public boolean hasPermission(String permission) {
        return role.hasPermission(permission);
    }
    
    /**
     * 检查用户是否可以登录
     */
    public boolean canLogin() {
        return status.canLogin();
    }
    
    /**
     * 检查用户是否可以发布内容
     */
    public boolean canPost() {
        return status.canPost();
    }
    
    // Getters
    @Override
    public UserId getId() {
        return id;
    }
    
    public Username getUsername() {
        return username;
    }
    
    public Email getEmail() {
        return email;
    }
    
    public Password getPassword() {
        return password;
    }
    
    public UserProfile getProfile() {
        return profile;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}