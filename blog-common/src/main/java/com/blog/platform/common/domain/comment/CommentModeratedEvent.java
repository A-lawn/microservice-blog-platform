package com.blog.platform.common.domain.comment;

import com.blog.platform.common.domain.DomainEvent;

/**
 * 评论审核事件
 */
public class CommentModeratedEvent extends DomainEvent {
    
    private final String articleId;
    private final String authorId;
    private final String moderatorId;
    private final CommentStatus oldStatus;
    private final CommentStatus newStatus;
    private final String reason;
    
    public CommentModeratedEvent(String commentId, String articleId, String authorId, 
                                String moderatorId, CommentStatus oldStatus, 
                                CommentStatus newStatus, String reason) {
        super(commentId);
        this.articleId = articleId;
        this.authorId = authorId;
        this.moderatorId = moderatorId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
    }
    
    public String getArticleId() {
        return articleId;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public String getModeratorId() {
        return moderatorId;
    }
    
    public CommentStatus getOldStatus() {
        return oldStatus;
    }
    
    public CommentStatus getNewStatus() {
        return newStatus;
    }
    
    public String getReason() {
        return reason;
    }
    
    /**
     * 检查是否为审核通过
     */
    public boolean isApproval() {
        return oldStatus == CommentStatus.PENDING && newStatus == CommentStatus.ACTIVE;
    }
    
    /**
     * 检查是否为隐藏操作
     */
    public boolean isHiding() {
        return newStatus == CommentStatus.HIDDEN;
    }
    
    /**
     * 检查是否为恢复操作
     */
    public boolean isRestoration() {
        return (oldStatus == CommentStatus.HIDDEN || oldStatus == CommentStatus.DELETED) 
               && newStatus == CommentStatus.ACTIVE;
    }
    
    @Override
    public String toString() {
        return String.format("CommentModeratedEvent{commentId='%s', articleId='%s', moderatorId='%s', statusChange='%s->%s', reason='%s', occurredOn=%s}", 
                getAggregateId(), articleId, moderatorId, oldStatus, newStatus, reason, getOccurredOn());
    }
}