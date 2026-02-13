package com.blog.platform.comment.application.dto;

import com.blog.platform.common.domain.comment.CommentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论树形结构DTO
 */
public class CommentTreeDto {
    
    private String id;
    private String articleId;
    private String authorId;
    private String authorName;
    private String content;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likeCount;
    private int level; // 评论层级，0为顶级评论
    private List<CommentTreeDto> children = new ArrayList<>(); // 子评论
    
    public CommentTreeDto() {}
    
    public CommentTreeDto(String id, String articleId, String authorId, String authorName,
                         String content, CommentStatus status, LocalDateTime createdAt,
                         LocalDateTime updatedAt, int likeCount, int level) {
        this.id = id;
        this.articleId = articleId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.level = level;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getArticleId() {
        return articleId;
    }
    
    public void setArticleId(String articleId) {
        this.articleId = articleId;
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public CommentStatus getStatus() {
        return status;
    }
    
    public void setStatus(CommentStatus status) {
        this.status = status;
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
    
    public int getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public List<CommentTreeDto> getChildren() {
        return children;
    }
    
    public void setChildren(List<CommentTreeDto> children) {
        this.children = children;
    }
    
    public void addChild(CommentTreeDto child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
    
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
    
    public int getChildrenCount() {
        return children != null ? children.size() : 0;
    }
    
    public int getTotalRepliesCount() {
        if (children == null || children.isEmpty()) {
            return 0;
        }
        
        int count = children.size();
        for (CommentTreeDto child : children) {
            count += child.getTotalRepliesCount();
        }
        return count;
    }
}