package com.blog.platform.common.domain.article;

import com.blog.platform.common.domain.DomainEvent;

/**
 * 文章创建事件
 */
public class ArticleCreatedEvent extends DomainEvent {
    
    private final String authorId;
    private final String title;
    
    public ArticleCreatedEvent(String articleId, String authorId, String title) {
        super(articleId);
        this.authorId = authorId;
        this.title = title;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public String getTitle() {
        return title;
    }
    
    @Override
    public String toString() {
        return String.format("ArticleCreatedEvent{articleId='%s', authorId='%s', title='%s', occurredOn=%s}", 
                getAggregateId(), authorId, title, getOccurredOn());
    }
}