package com.blog.platform.article.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tags")
public class TagEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;
    
    @Column(name = "slug", length = 60, unique = true)
    private String slug;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "article_count", nullable = false)
    private Integer articleCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public TagEntity() {}
    
    public TagEntity(String name) {
        this.name = name;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getArticleCount() {
        return articleCount;
    }
    
    public void setArticleCount(Integer articleCount) {
        this.articleCount = articleCount;
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
    
    public void incrementArticleCount() {
        this.articleCount = (this.articleCount == null ? 0 : this.articleCount) + 1;
    }
    
    public void decrementArticleCount() {
        if (this.articleCount != null && this.articleCount > 0) {
            this.articleCount--;
        }
    }
}
