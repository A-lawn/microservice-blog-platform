package com.blog.platform.article.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_categories")
public class ArticleCategoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "article_id", length = 36, nullable = false)
    private String articleId;
    
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private ArticleEntity article;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private CategoryEntity category;
    
    // Default constructor
    public ArticleCategoryEntity() {}
    
    // Constructor with required fields
    public ArticleCategoryEntity(String articleId, Long categoryId) {
        this.articleId = articleId;
        this.categoryId = categoryId;
    }
    
    // Constructor with entities
    public ArticleCategoryEntity(ArticleEntity article, CategoryEntity category) {
        this.article = article;
        this.category = category;
        this.articleId = article.getId();
        this.categoryId = category.getId();
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
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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
    
    public CategoryEntity getCategory() {
        return category;
    }
    
    public void setCategory(CategoryEntity category) {
        this.category = category;
        if (category != null) {
            this.categoryId = category.getId();
        }
    }
}