package com.blog.platform.article.infrastructure.messaging;

import com.blog.platform.common.domain.article.ArticleCreatedEvent;
import com.blog.platform.common.domain.article.ArticlePublishedEvent;
import com.blog.platform.common.domain.article.ArticleUpdatedEvent;
import com.blog.platform.common.domain.article.ArticleArchivedEvent;
import com.blog.platform.common.messaging.ReliableMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ArticleEventPublisher.class);

    public static final String ARTICLE_CREATED_TOPIC = "ARTICLE_CREATED";
    public static final String ARTICLE_PUBLISHED_TOPIC = "ARTICLE_PUBLISHED";
    public static final String ARTICLE_UPDATED_TOPIC = "ARTICLE_UPDATED";
    public static final String ARTICLE_ARCHIVED_TOPIC = "ARTICLE_ARCHIVED";

    @Autowired
    private ReliableMessageService reliableMessageService;

    public void publishArticleCreatedEvent(ArticleCreatedEvent event) {
        logger.info("Publishing article created event for article: {}", event.getAggregateId());
        reliableMessageService.sendMessage(ARTICLE_CREATED_TOPIC, event, event.getAggregateId());
        logger.info("Article created event queued for article: {}", event.getAggregateId());
    }

    public void publishArticlePublishedEvent(ArticlePublishedEvent event) {
        logger.info("Publishing article published event for article: {}", event.getAggregateId());
        reliableMessageService.sendMessage(ARTICLE_PUBLISHED_TOPIC, event, event.getAggregateId());
        logger.info("Article published event queued for article: {}", event.getAggregateId());
    }

    public void publishArticleUpdatedEvent(ArticleUpdatedEvent event) {
        logger.info("Publishing article updated event for article: {}", event.getAggregateId());
        reliableMessageService.sendMessage(ARTICLE_UPDATED_TOPIC, event, event.getAggregateId());
        logger.info("Article updated event queued for article: {}", event.getAggregateId());
    }

    public void publishArticleArchivedEvent(ArticleArchivedEvent event) {
        logger.info("Publishing article archived event for article: {}", event.getAggregateId());
        reliableMessageService.sendMessage(ARTICLE_ARCHIVED_TOPIC, event, event.getAggregateId());
        logger.info("Article archived event queued for article: {}", event.getAggregateId());
    }
}
