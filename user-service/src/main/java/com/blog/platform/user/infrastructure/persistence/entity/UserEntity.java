package com.blog.platform.user.infrastructure.persistence.entity;

import com.blog.platform.common.domain.user.UserStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class UserEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;
    
    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;
    
    @Column(name = "phone", length = 20, unique = true)
    private String phone;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "nickname", length = 50)
    private String nickname;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "login_count", nullable = false)
    private Integer loginCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserStatisticsEntity statistics;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserRoleEntity> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserSessionEntity> sessions = new HashSet<>();
    
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserFollowEntity> following = new HashSet<>();
    
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserFollowEntity> followers = new HashSet<>();
    
    public UserEntity() {}
    
    public UserEntity(String id, String username, String email, String passwordHash) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public Integer getLoginCount() {
        return loginCount;
    }
    
    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
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
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public UserStatisticsEntity getStatistics() {
        return statistics;
    }
    
    public void setStatistics(UserStatisticsEntity statistics) {
        this.statistics = statistics;
    }
    
    public Set<UserRoleEntity> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<UserRoleEntity> roles) {
        this.roles = roles;
    }
    
    public Set<UserSessionEntity> getSessions() {
        return sessions;
    }
    
    public void setSessions(Set<UserSessionEntity> sessions) {
        this.sessions = sessions;
    }
    
    public Set<UserFollowEntity> getFollowing() {
        return following;
    }
    
    public void setFollowing(Set<UserFollowEntity> following) {
        this.following = following;
    }
    
    public Set<UserFollowEntity> getFollowers() {
        return followers;
    }
    
    public void setFollowers(Set<UserFollowEntity> followers) {
        this.followers = followers;
    }
    
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.loginCount = (this.loginCount == null ? 0 : this.loginCount) + 1;
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
