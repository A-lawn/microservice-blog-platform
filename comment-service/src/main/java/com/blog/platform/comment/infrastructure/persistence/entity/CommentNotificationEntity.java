package com.blog.platform.comment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_notifications")
public class CommentNotificationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "comment_id", length = 36, nullable = false)
    private String commentId;
    
    @Column(name = "recipient_id", length = 36, nullable = false)
    private String recipientId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", insertable = false, updatable = false)
    private CommentEntity comment;
    
    // Enum
    public enum NotificationType {
        REPLY, MENTION, LIKE
    }
    
    // Default constructor
    public CommentNotificationEntity() {}
    
    // Constructor with required fields
    public CommentNotificationEntity(String commentId, String recipientId, NotificationType type) {
        this.commentId = commentId;
        this.recipientId = recipientId;
        this.type = type;
    }
    
    // Constructor with comment entity
    public CommentNotificationEntity(CommentEntity comment, String recipientId, NotificationType type) {
        this.comment = comment;
        this.commentId = comment.getId();
        this.recipientId = recipientId;
        this.type = type;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCommentId() {
        return commentId;
    }
    
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
    
    public String getRecipientId() {
        return recipientId;
    }
    
    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
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
    
    public CommentEntity getComment() {
        return comment;
    }
    
    public void setComment(CommentEntity comment) {
        this.comment = comment;
        if (comment != null) {
            this.commentId = comment.getId();
        }
    }
    
    // Business methods
    public void markAsRead() {
        this.isRead = true;
    }
    
    public boolean isUnread() {
        return !isRead;
    }
}