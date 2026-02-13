package com.blog.platform.comment.infrastructure.persistence.entity;

import com.blog.platform.common.domain.comment.CommentStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "comments")
@SQLDelete(sql = "UPDATE comments SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class CommentEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "article_id", length = 36, nullable = false)
    private String articleId;
    
    @Column(name = "author_id", length = 36, nullable = false)
    private String authorId;
    
    @Column(name = "parent_id", length = 36)
    private String parentId;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommentStatus status = CommentStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private CommentEntity parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CommentEntity> replies = new HashSet<>();
    
    @OneToOne(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CommentStatisticsEntity statistics;
    
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CommentLikeEntity> likes = new HashSet<>();
    
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CommentReportEntity> reports = new HashSet<>();
    
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CommentNotificationEntity> notifications = new HashSet<>();
    
    public CommentEntity() {}
    
    public CommentEntity(String id, String articleId, String authorId, String content) {
        this.id = id;
        this.articleId = articleId;
        this.authorId = authorId;
        this.content = content;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getArticleId() {
        return articleId;
    }
    
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public CommentStatus getStatus() {
        return status;
    }
    
    public void setStatus(CommentStatus status) {
        this.status = status;
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
    
    public CommentEntity getParent() {
        return parent;
    }
    
    public void setParent(CommentEntity parent) {
        this.parent = parent;
        if (parent != null) {
            this.parentId = parent.getId();
        }
    }
    
    public Set<CommentEntity> getReplies() {
        return replies;
    }
    
    public void setReplies(Set<CommentEntity> replies) {
        this.replies = replies;
    }
    
    public CommentStatisticsEntity getStatistics() {
        return statistics;
    }
    
    public void setStatistics(CommentStatisticsEntity statistics) {
        this.statistics = statistics;
    }
    
    public Set<CommentLikeEntity> getLikes() {
        return likes;
    }
    
    public void setLikes(Set<CommentLikeEntity> likes) {
        this.likes = likes;
    }
    
    public Set<CommentReportEntity> getReports() {
        return reports;
    }
    
    public void setReports(Set<CommentReportEntity> reports) {
        this.reports = reports;
    }
    
    public Set<CommentNotificationEntity> getNotifications() {
        return notifications;
    }
    
    public void setNotifications(Set<CommentNotificationEntity> notifications) {
        this.notifications = notifications;
    }
    
    public boolean isReply() {
        return parentId != null;
    }
    
    public boolean isActive() {
        return status == CommentStatus.ACTIVE;
    }
    
    public void hide() {
        this.status = CommentStatus.HIDDEN;
    }
    
    public void delete() {
        this.status = CommentStatus.DELETED;
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
