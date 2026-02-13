package com.blog.platform.article.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_likes")
public class ArticleLikeEntity {
    
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
    
    // Default constructor
    public ArticleLikeEntity() {}
    
    // Constructor with required fields
    public ArticleLikeEntity(String articleId, String userId) {
        this.articleId = articleId;
        this.userId = userId;
    }
    
    // Constructor with article entity
    public ArticleLikeEntity(ArticleEntity article, String userId) {
        this.article = article;
        this.articleId = article.getId();
        this.userId = userId;
    }
    
    // Getters and setters
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
        if (article != null) {
            this.articleId = article.getId();
        }
    }
}