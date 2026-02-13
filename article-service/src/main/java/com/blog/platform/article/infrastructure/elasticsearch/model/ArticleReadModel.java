package com.blog.platform.article.infrastructure.elasticsearch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "articles")
public class ArticleReadModel {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String summary;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String content;
    
    @Field(type = FieldType.Keyword)
    private String authorId;
    
    @Field(type = FieldType.Keyword)
    private String authorName;
    
    @Field(type = FieldType.Date)
    private LocalDateTime publishTime;
    
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;
    
    @Field(type = FieldType.Long)
    private Long viewCount = 0L;
    
    @Field(type = FieldType.Long)
    private Long likeCount = 0L;
    
    @Field(type = FieldType.Long)
    private Long commentCount = 0L;
    
    @Field(type = FieldType.Long)
    private Long shareCount = 0L;
    
    @Field(type = FieldType.Long)
    private Long bookmarkCount = 0L;
    
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    
    @Field(type = FieldType.Keyword)
    private List<String> categoryNames;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    public ArticleReadModel() {}
    
    public ArticleReadModel(String id, String title, String content, String authorId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
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
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public List<String> getCategoryNames() {
        return categoryNames;
    }
    
    public void setCategoryNames(List<String> categoryNames) {
        this.categoryNames = categoryNames;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
