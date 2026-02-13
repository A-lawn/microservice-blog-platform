package com.blog.platform.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_roles")
public class UserRoleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @Column(name = "role_name", length = 50, nullable = false)
    private String roleName;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;
    
    // Default constructor
    public UserRoleEntity() {}
    
    // Constructor with required fields
    public UserRoleEntity(String userId, String roleName) {
        this.userId = userId;
        this.roleName = roleName;
    }
    
    // Constructor with user entity
    public UserRoleEntity(UserEntity user, String roleName) {
        this.user = user;
        this.userId = user.getId();
        this.roleName = roleName;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public UserEntity getUser() {
        return user;
    }
    
    public void setUser(UserEntity user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }
}