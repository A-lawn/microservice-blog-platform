package com.blog.platform.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class NotificationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "reference_id", length = 36)
    private String referenceId;
    
    @Column(name = "reference_type", length = 50)
    private String referenceType;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        SYSTEM, LIKE, COMMENT, FOLLOW, MENTION
    }
    
    public NotificationEntity() {}
    
    public NotificationEntity(String userId, NotificationType type, String title) {
        this.userId = userId;
        this.type = type;
        this.title = title;
    }
    
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
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
    
    public String getReferenceType() {
        return referenceType;
    }
    
    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void markAsRead() {
        this.isRead = true;
    }
}
