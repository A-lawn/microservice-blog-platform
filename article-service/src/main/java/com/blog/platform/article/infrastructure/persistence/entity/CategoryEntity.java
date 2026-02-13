package com.blog.platform.article.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
public class CategoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", length = 100, unique = true, nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private CategoryEntity parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CategoryEntity> children = new HashSet<>();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ArticleCategoryEntity> articles = new HashSet<>();
    
    // Default constructor
    public CategoryEntity() {}
    
    // Constructor with required fields
    public CategoryEntity(String name, String description, Integer sortOrder) {
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
    }
    
    // Getters and setters
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
    
    public CategoryEntity getParent() {
        return parent;
    }
    
    public void setParent(CategoryEntity parent) {
        this.parent = parent;
        if (parent != null) {
            this.parentId = parent.getId();
        }
    }
    
    public Set<CategoryEntity> getChildren() {
        return children;
    }
    
    public void setChildren(Set<CategoryEntity> children) {
        this.children = children;
    }
    
    public Set<ArticleCategoryEntity> getArticles() {
        return articles;
    }
    
    public void setArticles(Set<ArticleCategoryEntity> articles) {
        this.articles = articles;
    }
}