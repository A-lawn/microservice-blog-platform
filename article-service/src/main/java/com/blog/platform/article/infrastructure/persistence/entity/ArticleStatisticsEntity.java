package com.blog.platform.article.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_statistics")
public class ArticleStatisticsEntity {
    
    @Id
    @Column(name = "article_id", length = 36)
    private String articleId;
    
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;
    
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;
    
    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;
    
    @Column(name = "share_count", nullable = false)
    private Long shareCount = 0L;
    
    @Column(name = "bookmark_count", nullable = false)
    private Long bookmarkCount = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToOne
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private ArticleEntity article;
    
    public ArticleStatisticsEntity() {}
    
    public ArticleStatisticsEntity(ArticleEntity article) {
        this.article = article;
        this.articleId = article.getId();
    }
    
    public String getArticleId() {
        return articleId;
    }
    
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }
    
    public Long getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
    
    public Long getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    
    public Long getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }
    
    public Long getShareCount() {
        return shareCount;
    }
    
    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }
    
    public Long getBookmarkCount() {
        return bookmarkCount;
    }
    
    public void setBookmarkCount(Long bookmarkCount) {
        this.bookmarkCount = bookmarkCount;
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
    
    public ArticleEntity getArticle() {
        return article;
    }
    
    public void setArticle(ArticleEntity article) {
        this.article = article;
        if (article != null) {
            this.articleId = article.getId();
        }
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public void incrementViewCount(long count) {
        this.viewCount += count;
    }
    
    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
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
    
    public void incrementShareCount() {
        this.shareCount++;
    }
    
    public void incrementBookmarkCount() {
        this.bookmarkCount++;
    }
    
    public void decrementBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount--;
        }
    }
}
