package com.blog.platform.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSessionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @Column(name = "token_hash", nullable = false)
    private String tokenHash;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;
    
    // Default constructor
    public UserSessionEntity() {}
    
    // Constructor with required fields
    public UserSessionEntity(String userId, String tokenHash, LocalDateTime expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
    
    // Constructor with user entity
    public UserSessionEntity(UserEntity user, String tokenHash, LocalDateTime expiresAt) {
        this.user = user;
        this.userId = user.getId();
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
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
    
    public String getTokenHash() {
        return tokenHash;
    }
    
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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
    
    // Business methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}