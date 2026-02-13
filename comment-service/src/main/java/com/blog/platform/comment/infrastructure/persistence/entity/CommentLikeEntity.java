package com.blog.platform.comment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_likes")
public class CommentLikeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "comment_id", length = 36, nullable = false)
    private String commentId;
    
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", insertable = false, updatable = false)
    private CommentEntity comment;
    
    // Default constructor
    public CommentLikeEntity() {}
    
    // Constructor with required fields
    public CommentLikeEntity(String commentId, String userId) {
        this.commentId = commentId;
        this.userId = userId;
    }
    
    // Constructor with comment entity
    public CommentLikeEntity(CommentEntity comment, String userId) {
        this.comment = comment;
        this.commentId = comment.getId();
        this.userId = userId;
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
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
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
}