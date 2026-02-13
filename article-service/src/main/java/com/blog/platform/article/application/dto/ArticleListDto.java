package com.blog.platform.article.application.dto;

import com.blog.platform.common.domain.article.ArticleStatus;

import java.time.LocalDateTime;

/**
 * 文章列表DTO - 用于列表展示，不包含完整内容
 */
public class ArticleListDto {
    
    private String id;
    private String authorId;
    private String title;
    private String summary;
    private ArticleStatus status;
    private LocalDateTime publishTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ArticleStatisticsDto statistics;
    
    // Default constructor
    public ArticleListDto() {}
    
    // Constructor
    public ArticleListDto(String id, String authorId, String title, String summary,
                         ArticleStatus status, LocalDateTime publishTime, LocalDateTime createdAt,
                         LocalDateTime updatedAt, ArticleStatisticsDto statistics) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.summary = summary;
        this.status = status;
        this.publishTime = publishTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.statistics = statistics;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public ArticleStatus getStatus() {
        return status;
    }
    
    public void setStatus(ArticleStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getPublishTime() {
        return publishTime;
    }
    
    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
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
    
    public ArticleStatisticsDto getStatistics() {
        return statistics;
    }
    
    public void setStatistics(ArticleStatisticsDto statistics) {
        this.statistics = statistics;
    }
}