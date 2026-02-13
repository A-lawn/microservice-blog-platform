package com.blog.platform.article.infrastructure.persistence.entity;

import com.blog.platform.common.domain.article.ArticleStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "articles")
@SQLDelete(sql = "UPDATE articles SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class ArticleEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "author_id", length = 36, nullable = false)
    private String authorId;
    
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @Column(name = "slug", length = 250, unique = true)
    private String slug;
    
    @Column(name = "content", columnDefinition = "LONGTEXT", nullable = false)
    private String content;
    
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    
    @Column(name = "cover_image")
    private String coverImage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ArticleStatus status = ArticleStatus.DRAFT;
    
    @Column(name = "publish_time")
    private LocalDateTime publishTime;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @OneToOne(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ArticleStatisticsEntity statistics;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ArticleTagEntity> tags = new HashSet<>();
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ArticleCategoryEntity> categories = new HashSet<>();
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ArticleLikeEntity> likes = new HashSet<>();
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ArticleBookmarkEntity> bookmarks = new HashSet<>();
    
    public ArticleEntity() {}
    
    public ArticleEntity(String id, String authorId, String title, String content) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
    }
    
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
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getCoverImage() {
        return coverImage;
    }
    
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
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
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public ArticleStatisticsEntity getStatistics() {
        return statistics;
    }
    
    public void setStatistics(ArticleStatisticsEntity statistics) {
        this.statistics = statistics;
    }
    
    public Set<ArticleTagEntity> getTags() {
        return tags;
    }
    
    public void setTags(Set<ArticleTagEntity> tags) {
        this.tags = tags;
    }
    
    public Set<ArticleCategoryEntity> getCategories() {
        return categories;
    }
    
    public void setCategories(Set<ArticleCategoryEntity> categories) {
        this.categories = categories;
    }
    
    public Set<ArticleLikeEntity> getLikes() {
        return likes;
    }
    
    public void setLikes(Set<ArticleLikeEntity> likes) {
        this.likes = likes;
    }
    
    public Set<ArticleBookmarkEntity> getBookmarks() {
        return bookmarks;
    }
    
    public void setBookmarks(Set<ArticleBookmarkEntity> bookmarks) {
        this.bookmarks = bookmarks;
    }
    
    public void publish() {
        this.status = ArticleStatus.PUBLISHED;
        this.publishTime = LocalDateTime.now();
    }
    
    public void archive() {
        this.status = ArticleStatus.ARCHIVED;
    }
    
    public boolean isPublished() {
        return status == ArticleStatus.PUBLISHED;
    }
    
    public boolean isDraft() {
        return status == ArticleStatus.DRAFT;
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
