package com.blog.platform.common.domain.article;

/**
 * 文章状态枚举
 */
public enum ArticleStatus {
    
    /**
     * 草稿状态 - 文章已创建但未发布
     */
    DRAFT("草稿"),
    
    /**
     * 已发布状态 - 文章已公开发布
     */
    PUBLISHED("已发布"),
    
    /**
     * 已归档状态 - 文章已归档，不再显示
     */
    ARCHIVED("已归档");
    
    private final String description;
    
    ArticleStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 检查是否可以发布
     */
    public boolean canPublish() {
        return this == DRAFT;
    }
    
    /**
     * 检查是否可以编辑
     */
    public boolean canEdit() {
        return this == DRAFT || this == PUBLISHED;
    }
    
    /**
     * 检查是否可以归档
     */
    public boolean canArchive() {
        return this == PUBLISHED;
    }
    
    /**
     * 检查是否可见
     */
    public boolean isVisible() {
        return this == PUBLISHED;
    }
    
    /**
     * 检查是否是草稿
     */
    public boolean isDraft() {
        return this == DRAFT;
    }
    
    /**
     * 检查是否已发布
     */
    public boolean isPublished() {
        return this == PUBLISHED;
    }
    
    /**
     * 检查是否已归档
     */
    public boolean isArchived() {
        return this == ARCHIVED;
    }
}