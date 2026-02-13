package com.blog.platform.common.domain.article;

import com.blog.platform.common.domain.DomainEvent;

/**
 * 文章更新事件
 */
public class ArticleUpdatedEvent extends DomainEvent {
    
    private final String authorId;
    private final String title;
    private final String updateType; // 更新类型：CONTENT, TITLE, STATUS等
    
    public ArticleUpdatedEvent(String articleId, String authorId, String title, String updateType) {
        super(articleId);
        this.authorId = authorId;
        this.title = title;
        this.updateType = updateType;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getUpdateType() {
        return updateType;
    }
    
    @Override
    public String toString() {
        return String.format("ArticleUpdatedEvent{articleId='%s', authorId='%s', title='%s', updateType='%s', occurredOn=%s}", 
                getAggregateId(), authorId, title, updateType, getOccurredOn());
    }
}