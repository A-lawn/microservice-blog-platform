package com.blog.platform.common.domain.comment;

import com.blog.platform.common.domain.DomainEvent;

/**
 * 评论删除事件
 */
public class CommentDeletedEvent extends DomainEvent {
    
    private final String articleId;
    private final String authorId;
    private final String parentId;
    private final String reason;
    
    public CommentDeletedEvent(String commentId, String articleId, String authorId, 
                              String parentId, String reason) {
        super(commentId);
        this.articleId = articleId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.reason = reason;
    }
    
    public String getArticleId() {
        return articleId;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public String getReason() {
        return reason;
    }
    
    /**
     * 检查是否为回复评论的删除
     */
    public boolean isReplyDeletion() {
        return parentId != null;
    }
    
    @Override
    public String toString() {
        return String.format("CommentDeletedEvent{commentId='%s', articleId='%s', authorId='%s', parentId='%s', reason='%s', occurredOn=%s}", 
                getAggregateId(), articleId, authorId, parentId, reason, getOccurredOn());
    }
}