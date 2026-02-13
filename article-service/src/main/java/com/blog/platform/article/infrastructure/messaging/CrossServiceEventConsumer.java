package com.blog.platform.article.infrastructure.messaging;

import com.blog.platform.common.domain.comment.CommentCreatedEvent;
import com.blog.platform.common.domain.comment.CommentDeletedEvent;
import com.blog.platform.common.domain.user.UserRegisteredEvent;
import com.blog.platform.common.messaging.MessageConstants;
import com.blog.platform.common.messaging.MessageRetryHandler;
import com.blog.platform.article.application.service.ArticleApplicationService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 跨服务事件消费者
 * 处理来自其他服务的事件，实现跨服务业务逻辑
 */
@Service
public class CrossServiceEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CrossServiceEventConsumer.class);

    @Autowired
    private ArticleApplicationService articleApplicationService;

    @Autowired
    private MessageRetryHandler messageRetryHandler;

    /**
     * 处理评论创建事件 - 更新文章统计
     */
    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.COMMENT_CREATED_TOPIC,
        consumerGroup = MessageConstants.ARTICLE_SERVICE_CONSUMER_GROUP + "_COMMENT_CREATED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class CommentCreatedEventConsumer implements RocketMQListener<CommentCreatedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(CommentCreatedEventConsumer.class);

        @Autowired
        private ArticleApplicationService articleApplicationService;

        @Override
        public void onMessage(CommentCreatedEvent event) {
            try {
                logger.info("Processing comment created event for article statistics update. Comment: {}, Article: {}", 
                           event.getAggregateId(), event.getArticleId());
                
                // 更新文章评论统计
                updateArticleCommentStatistics(event);
                
                logger.info("Successfully updated article statistics for comment created event. Comment: {}, Article: {}", 
                           event.getAggregateId(), event.getArticleId());
            } catch (Exception e) {
                logger.error("Failed to process comment created event for article statistics. Comment: {}, Article: {}", 
                            event.getAggregateId(), event.getArticleId(), e);
                throw e; // 重新抛出异常以触发重试机制
            }
        }

        private void updateArticleCommentStatistics(CommentCreatedEvent event) {
            try {
                // 增加文章评论统计
                articleApplicationService.incrementCommentCount(
                    event.getArticleId(), 
                    event.getAggregateId(), 
                    "CREATED"
                );
                
                logger.info("Article comment statistics updated for new comment: {} on article: {}", 
                           event.getAggregateId(), event.getArticleId());
                
            } catch (Exception e) {
                logger.error("Failed to update article comment statistics for comment: {} on article: {}", 
                            event.getAggregateId(), event.getArticleId(), e);
                throw e;
            }
        }
    }

    /**
     * 处理评论删除事件 - 更新文章统计
     */
    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.COMMENT_DELETED_TOPIC,
        consumerGroup = MessageConstants.ARTICLE_SERVICE_CONSUMER_GROUP + "_COMMENT_DELETED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class CommentDeletedEventConsumer implements RocketMQListener<CommentDeletedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(CommentDeletedEventConsumer.class);

        @Autowired
        private ArticleApplicationService articleApplicationService;

        @Override
        public void onMessage(CommentDeletedEvent event) {
            try {
                logger.info("Processing comment deleted event for article statistics update. Comment: {}, Article: {}", 
                           event.getAggregateId(), event.getArticleId());
                
                // 更新文章评论统计
                updateArticleCommentStatistics(event);
                
                logger.info("Successfully updated article statistics for comment deleted event. Comment: {}, Article: {}", 
                           event.getAggregateId(), event.getArticleId());
            } catch (Exception e) {
                logger.error("Failed to process comment deleted event for article statistics. Comment: {}, Article: {}", 
                            event.getAggregateId(), event.getArticleId(), e);
                throw e; // 重新抛出异常以触发重试机制
            }
        }

        private void updateArticleCommentStatistics(CommentDeletedEvent event) {
            try {
                // 减少文章评论统计
                articleApplicationService.decrementCommentCount(
                    event.getArticleId(), 
                    event.getAggregateId(), 
                    "DELETED"
                );
                
                logger.info("Article comment statistics updated for deleted comment: {} on article: {}", 
                           event.getAggregateId(), event.getArticleId());
                
            } catch (Exception e) {
                logger.error("Failed to update article comment statistics for deleted comment: {} on article: {}", 
                            event.getAggregateId(), event.getArticleId(), e);
                throw e;
            }
        }
    }

    /**
     * 处理用户注册事件 - 可以用于初始化用户相关的文章数据
     */
    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.USER_REGISTERED_TOPIC,
        consumerGroup = MessageConstants.ARTICLE_SERVICE_CONSUMER_GROUP + "_USER_REGISTERED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class UserRegisteredEventConsumer implements RocketMQListener<UserRegisteredEvent> {

        private static final Logger logger = LoggerFactory.getLogger(UserRegisteredEventConsumer.class);

        @Override
        public void onMessage(UserRegisteredEvent event) {
            try {
                logger.info("Processing user registered event for article service initialization. User: {}", 
                           event.getAggregateId());
                
                // 初始化用户相关的文章数据
                initializeUserArticleData(event);
                
                logger.info("Successfully processed user registered event for article service. User: {}", 
                           event.getAggregateId());
            } catch (Exception e) {
                logger.error("Failed to process user registered event for article service. User: {}", 
                            event.getAggregateId(), e);
                // 用户注册事件处理失败不影响主流程，只记录日志
                logger.warn("User registration event processing failed in article service, but continuing...");
            }
        }

        private void initializeUserArticleData(UserRegisteredEvent event) {
            try {
                // 这里可以实现用户注册后的文章服务初始化逻辑
                // 例如：创建用户文章统计记录、设置默认偏好等
                logger.info("User article data would be initialized for user: {}", event.getAggregateId());
                
                // 实际实现中，这里可能会：
                // 1. 创建用户文章统计记录
                // 2. 设置用户文章偏好
                // 3. 初始化推荐算法数据等
                
            } catch (Exception e) {
                logger.error("Failed to initialize user article data for user: {}", event.getAggregateId(), e);
                throw e;
            }
        }
    }
}