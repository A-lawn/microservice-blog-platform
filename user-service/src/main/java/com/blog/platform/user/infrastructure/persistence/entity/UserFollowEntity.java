package com.blog.platform.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_follows")
public class UserFollowEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "follower_id", length = 36, nullable = false)
    private String followerId;
    
    @Column(name = "following_id", length = 36, nullable = false)
    private String followingId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private UserEntity follower;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", insertable = false, updatable = false)
    private UserEntity following;
    
    public UserFollowEntity() {}
    
    public UserFollowEntity(String followerId, String followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFollowerId() {
        return followerId;
    }
    
    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }
    
    public String getFollowingId() {
        return followingId;
    }
    
    public void setFollowingId(String followingId) {
        this.followingId = followingId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public UserEntity getFollower() {
        return follower;
    }
    
    public void setFollower(UserEntity follower) {
        this.follower = follower;
    }
    
    public UserEntity getFollowing() {
        return following;
    }
    
    public void setFollowing(UserEntity following) {
        this.following = following;
    }
}
