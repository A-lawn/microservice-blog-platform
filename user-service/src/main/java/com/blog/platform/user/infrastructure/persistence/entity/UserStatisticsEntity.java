package com.blog.platform.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_statistics")
public class UserStatisticsEntity {
    
    @Id
    @Column(name = "user_id", length = 36)
    private String userId;
    
    @Column(name = "article_count", nullable = false)
    private Long articleCount = 0L;
    
    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;
    
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;
    
    @Column(name = "follower_count", nullable = false)
    private Long followerCount = 0L;
    
    @Column(name = "following_count", nullable = false)
    private Long followingCount = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    @MapsId
    private UserEntity user;
    
    public UserStatisticsEntity() {}
    
    public UserStatisticsEntity(UserEntity user) {
        this.user = user;
        this.userId = user.getId();
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Long getArticleCount() {
        return articleCount;
    }
    
    public void setArticleCount(Long articleCount) {
        this.articleCount = articleCount;
    }
    
    public Long getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }
    
    public Long getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    
    public Long getFollowerCount() {
        return followerCount;
    }
    
    public void setFollowerCount(Long followerCount) {
        this.followerCount = followerCount;
    }
    
    public Long getFollowingCount() {
        return followingCount;
    }
    
    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
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
    
    public UserEntity getUser() {
        return user;
    }
    
    public void setUser(UserEntity user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }
    
    public void incrementArticleCount() {
        this.articleCount++;
    }
    
    public void decrementArticleCount() {
        if (this.articleCount > 0) {
            this.articleCount--;
        }
    }
    
    public void incrementCommentCount() {
        this.commentCount++;
    }
    
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
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
    
    public void incrementFollowerCount() {
        this.followerCount++;
    }
    
    public void decrementFollowerCount() {
        if (this.followerCount > 0) {
            this.followerCount--;
        }
    }
    
    public void incrementFollowingCount() {
        this.followingCount++;
    }
    
    public void decrementFollowingCount() {
        if (this.followingCount > 0) {
            this.followingCount--;
        }
    }
}
