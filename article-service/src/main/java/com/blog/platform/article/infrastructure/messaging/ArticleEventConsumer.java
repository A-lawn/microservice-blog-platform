package com.blog.platform.article.infrastructure.messaging;

import com.blog.platform.common.domain.article.ArticleCreatedEvent;
import com.blog.platform.common.domain.article.ArticlePublishedEvent;
import com.blog.platform.common.domain.article.ArticleUpdatedEvent;
import com.blog.platform.common.messaging.MessageConstants;
import com.blog.platform.article.infrastructure.elasticsearch.service.ArticleReadModelSyncService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ArticleEventConsumer.class);

    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.ARTICLE_CREATED_TOPIC,
        consumerGroup = MessageConstants.ARTICLE_SERVICE_CONSUMER_GROUP + "_CREATED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class ArticleCreatedEventConsumer implements RocketMQListener<ArticleCreatedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(ArticleCreatedEventConsumer.class);

        @Autowired(required = false)
        private ArticleReadModelSyncService readModelSyncService;

        @Override
        public void onMessage(ArticleCreatedEvent event) {
            try {
                logger.info("Processing article created event for article: {}", event.getAggregateId());
                
                if (readModelSyncService != null) {
                    readModelSyncService.syncArticle(event.getAggregateId());
                    logger.info("Synced article to read model: {}", event.getAggregateId());
                }
                
                logger.info("Successfully processed article created event for article: {}", event.getAggregateId());
            } catch (Exception e) {
                logger.error("Failed to process article created event for article: {}", event.getAggregateId(), e);
                throw e;
            }
        }
    }

    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.ARTICLE_PUBLISHED_TOPIC,
        consumerGroup = MessageConstants.ARTICLE_SERVICE_CONSUMER_GROUP + "_PUBLISHED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class ArticlePublishedEventConsumer implements RocketMQListener<ArticlePublishedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(ArticlePublishedEventConsumer.class);

        @Autowired(required = false)
        private ArticleReadModelSyncService readModelSyncService;

        @Override
        public void onMessage(ArticlePublishedEvent event) {
            try {
                logger.info("Processing article published event for article: {}", event.getAggregateId());
                
                if (readModelSyncService != null) {
                    readModelSyncService.syncArticle(event.getAggregateId());
                    logger.info("Updated search index for published article: {}", event.getAggregateId());
                }
                
                logger.info("Successfully processed article published event for article: {}", event.getAggregateId());
            } catch (Exception e) {
                logger.error("Failed to process article published event for article: {}", event.getAggregateId(), e);
                throw e;
            }
        }
    }

    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.ARTICLE_UPDATED_TOPIC,
        consumerGroup = MessageConstants.ARTICLE_SERVICE_CONSUMER_GROUP + "_UPDATED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class ArticleUpdatedEventConsumer implements RocketMQListener<ArticleUpdatedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(ArticleUpdatedEventConsumer.class);

        @Autowired(required = false)
        private ArticleReadModelSyncService readModelSyncService;

        @Override
        public void onMessage(ArticleUpdatedEvent event) {
            try {
                logger.info("Processing article updated event for article: {}", event.getAggregateId());
                
                if (readModelSyncService != null) {
                    readModelSyncService.syncArticle(event.getAggregateId());
                    logger.info("Updated read model for article: {}", event.getAggregateId());
                }
                
                logger.info("Successfully processed article updated event for article: {}", event.getAggregateId());
            } catch (Exception e) {
                logger.error("Failed to process article updated event for article: {}", event.getAggregateId(), e);
            }
        }
    }
}
