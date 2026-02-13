package com.blog.platform.comment.application.dto;

import com.blog.platform.common.domain.comment.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论DTO
 */
public class CommentDto {
    
    private String id;
    private String articleId;
    private String authorId;
    private String authorName; // 作者名称，从用户服务获取
    private String content;
    private String parentId;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likeCount;
    private int replyCount;
    private List<CommentDto> replies; // 子评论列表
    
    public CommentDto() {}
    
    public CommentDto(String id, String articleId, String authorId, String authorName,
                     String content, String parentId, CommentStatus status,
                     LocalDateTime createdAt, LocalDateTime updatedAt,
                     int likeCount, int replyCount) {
        this.id = id;
        this.articleId = articleId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.parentId = parentId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
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
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
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
    
    public int getReplyCount() {
        return replyCount;
    }
    
    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }
    
    public List<CommentDto> getReplies() {
        return replies;
    }
    
    public void setReplies(List<CommentDto> replies) {
        this.replies = replies;
    }
    
    public boolean isReply() {
        return parentId != null;
    }
    
    public boolean hasReplies() {
        return replies != null && !replies.isEmpty();
    }
}