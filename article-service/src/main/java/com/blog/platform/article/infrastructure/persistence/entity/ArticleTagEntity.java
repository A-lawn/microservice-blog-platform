package com.blog.platform.article.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_tags")
public class ArticleTagEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "article_id", length = 36, nullable = false)
    private String articleId;
    
    @Column(name = "tag_name", length = 50, nullable = false)
    private String tagName;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private ArticleEntity article;
    
    // Default constructor
    public ArticleTagEntity() {}
    
    // Constructor with required fields
    public ArticleTagEntity(String articleId, String tagName) {
        this.articleId = articleId;
        this.tagName = tagName;
    }
    
    // Constructor with article entity
    public ArticleTagEntity(ArticleEntity article, String tagName) {
        this.article = article;
        this.articleId = article.getId();
        this.tagName = tagName;
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
    
    public String getTagName() {
        return tagName;
    }
    
    public void setTagName(String tagName) {
        this.tagName = tagName;
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