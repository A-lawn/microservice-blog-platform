package com.blog.platform.comment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_statistics")
public class CommentStatisticsEntity {
    
    @Id
    @Column(name = "comment_id", length = 36)
    private String commentId;
    
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;
    
    @Column(name = "reply_count", nullable = false)
    private Long replyCount = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToOne
    @JoinColumn(name = "comment_id")
    @MapsId
    private CommentEntity comment;
    
    public CommentStatisticsEntity() {}
    
    public CommentStatisticsEntity(CommentEntity comment) {
        this.comment = comment;
        this.commentId = comment.getId();
    }
    
    public String getCommentId() {
        return commentId;
    }
    
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
    
    public Long getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    
    public Long getReplyCount() {
        return replyCount;
    }
    
    public void setReplyCount(Long replyCount) {
        this.replyCount = replyCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
    
    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    public void incrementReplyCount() {
        this.replyCount++;
    }
    
    public void decrementReplyCount() {
        if (this.replyCount > 0) {
            this.replyCount--;
        }
    }
}
