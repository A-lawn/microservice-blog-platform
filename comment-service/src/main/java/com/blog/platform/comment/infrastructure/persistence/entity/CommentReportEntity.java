package com.blog.platform.comment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_reports")
public class CommentReportEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "comment_id", length = 36, nullable = false)
    private String commentId;
    
    @Column(name = "reporter_id", length = 36, nullable = false)
    private String reporterId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", insertable = false, updatable = false)
    private CommentEntity comment;
    
    // Enums
    public enum ReportReason {
        SPAM, INAPPROPRIATE, HARASSMENT, OTHER
    }
    
    public enum ReportStatus {
        PENDING, REVIEWED, RESOLVED
    }
    
    // Default constructor
    public CommentReportEntity() {}
    
    // Constructor with required fields
    public CommentReportEntity(String commentId, String reporterId, ReportReason reason) {
        this.commentId = commentId;
        this.reporterId = reporterId;
        this.reason = reason;
    }
    
    // Constructor with comment entity
    public CommentReportEntity(CommentEntity comment, String reporterId, ReportReason reason) {
        this.comment = comment;
        this.commentId = comment.getId();
        this.reporterId = reporterId;
        this.reason = reason;
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
    
    public String getReporterId() {
        return reporterId;
    }
    
    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }
    
    public ReportReason getReason() {
        return reason;
    }
    
    public void setReason(ReportReason reason) {
        this.reason = reason;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ReportStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReportStatus status) {
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
    public void markAsReviewed() {
        this.status = ReportStatus.REVIEWED;
    }
    
    public void markAsResolved() {
        this.status = ReportStatus.RESOLVED;
    }
    
    public boolean isPending() {
        return status == ReportStatus.PENDING;
    }
}