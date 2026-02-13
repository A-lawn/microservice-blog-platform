package com.blog.platform.comment.infrastructure.messaging;

import com.blog.platform.common.domain.comment.CommentCreatedEvent;
import com.blog.platform.common.domain.comment.CommentDeletedEvent;
import com.blog.platform.common.domain.comment.CommentModeratedEvent;
import com.blog.platform.common.messaging.MessageConstants;
import com.blog.platform.comment.infrastructure.elasticsearch.service.CommentReadModelSyncService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CommentEventConsumer.class);

    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.COMMENT_CREATED_TOPIC,
        consumerGroup = MessageConstants.COMMENT_SERVICE_CONSUMER_GROUP + "_CREATED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class CommentCreatedEventConsumer implements RocketMQListener<CommentCreatedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(CommentCreatedEventConsumer.class);

        @Autowired(required = false)
        private CommentReadModelSyncService readModelSyncService;

        @Override
        public void onMessage(CommentCreatedEvent event) {
            try {
                logger.info("Processing comment created event for comment: {} on article: {}", 
                           event.getAggregateId(), event.getArticleId());
                
                if (readModelSyncService != null) {
                    readModelSyncService.syncComment(event.getAggregateId());
                    logger.info("Synced comment to read model: {}", event.getAggregateId());
                }
                
                logger.info("Successfully processed comment created event for comment: {} on article: {}", 
                           event.getAggregateId(), event.getArticleId());
            } catch (Exception e) {
                logger.error("Failed to process comment created event for comment: {} on article: {}", 
                            event.getAggregateId(), event.getArticleId(), e);
                throw e;
            }
        }
    }

    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.COMMENT_DELETED_TOPIC,
        consumerGroup = MessageConstants.COMMENT_SERVICE_CONSUMER_GROUP + "_DELETED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class CommentDeletedEventConsumer implements RocketMQListener<CommentDeletedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(CommentDeletedEventConsumer.class);

        @Autowired(required = false)
        private CommentReadModelSyncService readModelSyncService;

        @Override
        public void onMessage(CommentDeletedEvent event) {
            try {
                logger.info("Processing comment deleted event for comment: {}", event.getAggregateId());
                
                if (readModelSyncService != null) {
                    readModelSyncService.removeComment(event.getAggregateId());
                    logger.info("Removed comment from read model: {}", event.getAggregateId());
                }
                
                logger.info("Successfully processed comment deleted event for comment: {}", event.getAggregateId());
            } catch (Exception e) {
                logger.error("Failed to process comment deleted event for comment: {}", event.getAggregateId(), e);
                throw e;
            }
        }
    }

    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.COMMENT_MODERATED_TOPIC,
        consumerGroup = MessageConstants.COMMENT_SERVICE_CONSUMER_GROUP + "_MODERATED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class CommentModeratedEventConsumer implements RocketMQListener<CommentModeratedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(CommentModeratedEventConsumer.class);

        @Autowired(required = false)
        private CommentReadModelSyncService readModelSyncService;

        @Override
        public void onMessage(CommentModeratedEvent event) {
            try {
                logger.info("Processing comment moderated event for comment: {} with status: {}", 
                           event.getAggregateId(), event.getNewStatus());
                
                if (readModelSyncService != null) {
                    readModelSyncService.syncComment(event.getAggregateId());
                    logger.info("Updated comment moderation status in read model: {} -> {}", 
                               event.getAggregateId(), event.getNewStatus());
                }
                
                logger.info("Successfully processed comment moderated event for comment: {} with status: {}", 
                           event.getAggregateId(), event.getNewStatus());
            } catch (Exception e) {
                logger.error("Failed to process comment moderated event for comment: {} with status: {}", 
                            event.getAggregateId(), event.getNewStatus(), e);
            }
        }
    }
}
