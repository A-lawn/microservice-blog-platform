package com.blog.platform.article.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_bookmarks")
public class ArticleBookmarkEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "article_id", length = 36, nullable = false)
    private String articleId;
    
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private ArticleEntity article;
    
    public ArticleBookmarkEntity() {}
    
    public ArticleBookmarkEntity(String articleId, String userId) {
        this.articleId = articleId;
        this.userId = userId;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getArticleId() {
        return articleId;
    }
    
    public void setArticleId(String articleId) {
        this.articleId = articleId;
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
    
    public ArticleEntity getArticle() {
        return article;
    }
    
    public void setArticle(ArticleEntity article) {
        this.article = article;
    }
}
