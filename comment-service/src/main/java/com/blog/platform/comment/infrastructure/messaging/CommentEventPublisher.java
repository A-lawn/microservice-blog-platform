package com.blog.platform.comment.infrastructure.messaging;

import com.blog.platform.common.domain.comment.CommentCreatedEvent;
import com.blog.platform.common.domain.comment.CommentDeletedEvent;
import com.blog.platform.common.domain.comment.CommentModeratedEvent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class CommentEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(CommentEventPublisher.class);

    public static final String COMMENT_CREATED_TOPIC = "COMMENT_CREATED";
    public static final String COMMENT_DELETED_TOPIC = "COMMENT_DELETED";
    public static final String COMMENT_MODERATED_TOPIC = "COMMENT_MODERATED";

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void publishCommentCreatedEvent(CommentCreatedEvent event) {
        try {
            logger.info("Publishing comment created event for comment: {} on article: {}", 
                       event.getAggregateId(), event.getArticleId());
            rocketMQTemplate.syncSend(COMMENT_CREATED_TOPIC, MessageBuilder.withPayload(event).build());
            logger.info("Successfully published comment created event for comment: {} on article: {}", 
                       event.getAggregateId(), event.getArticleId());
        } catch (Exception e) {
            logger.error("Failed to publish comment created event for comment: {} on article: {}", 
                        event.getAggregateId(), event.getArticleId(), e);
            throw new RuntimeException("Failed to publish comment created event", e);
        }
    }

    public void publishCommentDeletedEvent(CommentDeletedEvent event) {
        try {
            logger.info("Publishing comment deleted event for comment: {}", event.getAggregateId());
            rocketMQTemplate.syncSend(COMMENT_DELETED_TOPIC, MessageBuilder.withPayload(event).build());
            logger.info("Successfully published comment deleted event for comment: {}", event.getAggregateId());
        } catch (Exception e) {
            logger.error("Failed to publish comment deleted event for comment: {}", event.getAggregateId(), e);
            throw new RuntimeException("Failed to publish comment deleted event", e);
        }
    }

    public void publishCommentModeratedEvent(CommentModeratedEvent event) {
        try {
            logger.info("Publishing comment moderated event for comment: {}", event.getAggregateId());
            rocketMQTemplate.asyncSend(COMMENT_MODERATED_TOPIC, MessageBuilder.withPayload(event).build(), null);
            logger.info("Successfully published comment moderated event for comment: {}", event.getAggregateId());
        } catch (Exception e) {
            logger.error("Failed to publish comment moderated event for comment: {}", event.getAggregateId(), e);
        }
    }

    public void publishDelayedEvent(String topic, Object event, int delayLevel) {
        try {
            logger.info("Publishing delayed event to topic: {} with delay level: {}", topic, delayLevel);
            rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(event).build(), 3000, delayLevel);
            logger.info("Successfully published delayed event to topic: {} with delay level: {}", topic, delayLevel);
        } catch (Exception e) {
            logger.error("Failed to publish delayed event to topic: {} with delay level: {}", topic, delayLevel, e);
            throw new RuntimeException("Failed to publish delayed event", e);
        }
    }

    public void publishOrderlyCommentEvent(String topic, Object event, String articleId) {
        try {
            logger.info("Publishing orderly comment event to topic: {} for article: {}", topic, articleId);
            rocketMQTemplate.syncSendOrderly(topic, MessageBuilder.withPayload(event).build(), articleId);
            logger.info("Successfully published orderly comment event to topic: {} for article: {}", topic, articleId);
        } catch (Exception e) {
            logger.error("Failed to publish orderly comment event to topic: {} for article: {}", topic, articleId, e);
            throw new RuntimeException("Failed to publish orderly comment event", e);
        }
    }
}
