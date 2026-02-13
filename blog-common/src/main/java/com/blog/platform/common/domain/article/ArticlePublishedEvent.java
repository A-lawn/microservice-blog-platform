package com.blog.platform.common.domain.article;

import com.blog.platform.common.domain.DomainEvent;

import java.time.LocalDateTime;

/**
 * 文章发布事件
 */
public class ArticlePublishedEvent extends DomainEvent {
    
    private final String authorId;
    private final String title;
    private final LocalDateTime publishTime;
    
    public ArticlePublishedEvent(String articleId, String authorId, String title, LocalDateTime publishTime) {
        super(articleId);
        this.authorId = authorId;
        this.title = title;
        this.publishTime = publishTime;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public LocalDateTime getPublishTime() {
        return publishTime;
    }
    
    @Override
    public String toString() {
        return String.format("ArticlePublishedEvent{articleId='%s', authorId='%s', title='%s', publishTime=%s, occurredOn=%s}", 
                getAggregateId(), authorId, title, publishTime, getOccurredOn());
    }
}