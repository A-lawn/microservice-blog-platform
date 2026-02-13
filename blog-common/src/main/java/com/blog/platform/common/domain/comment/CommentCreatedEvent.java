package com.blog.platform.common.domain.comment;

import com.blog.platform.common.domain.DomainEvent;

/**
 * 评论创建事件
 */
public class CommentCreatedEvent extends DomainEvent {
    
    private final String articleId;
    private final String authorId;
    private final String parentId;
    private final String content;
    
    public CommentCreatedEvent(String commentId, String articleId, String authorId, 
                              String parentId, String content) {
        super(commentId);
        this.articleId = articleId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.content = content;
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
    
    public String getContent() {
        return content;
    }
    
    /**
     * 检查是否为回复评论
     */
    public boolean isReply() {
        return parentId != null;
    }
    
    @Override
    public String toString() {
        return String.format("CommentCreatedEvent{commentId='%s', articleId='%s', authorId='%s', parentId='%s', isReply=%s, occurredOn=%s}", 
                getAggregateId(), articleId, authorId, parentId, isReply(), getOccurredOn());
    }
}