package com.blog.platform.user.infrastructure.messaging;

import com.blog.platform.common.domain.article.ArticlePublishedEvent;
import com.blog.platform.common.domain.comment.CommentCreatedEvent;
import com.blog.platform.common.messaging.MessageConstants;
import com.blog.platform.common.messaging.MessageRetryHandler;
import com.blog.platform.user.application.service.UserApplicationService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "rocketmq.consumer.enabled", havingValue = "true", matchIfMissing = false)
public class CrossServiceEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CrossServiceEventConsumer.class);

    @Autowired
    private UserApplicationService userApplicationService;

    @Autowired
    private MessageRetryHandler messageRetryHandler;

    /**
     * 处理文章发布事件 - 更新用户统计
     */
    @Service
    @ConditionalOnProperty(name = "rocketmq.consumer.enabled", havingValue = "true", matchIfMissing = false)
    @RocketMQMessageListener(
        topic = MessageConstants.ARTICLE_PUBLISHED_TOPIC,
        consumerGroup = MessageConstants.USER_SERVICE_CONSUMER_GROUP + "_ARTICLE_PUBLISHED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class ArticlePublishedEventConsumer implements RocketMQListener<ArticlePublishedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(ArticlePublishedEventConsumer.class);

        @Autowired
        private UserApplicationService userApplicationService;

        @Override
        public void onMessage(ArticlePublishedEvent event) {
            try {
                logger.info("Processing article published event for user statistics update. Article: {}, Author: {}", 
                           event.getAggregateId(), event.getAuthorId());
                
                // 更新用户文章统计
                updateUserArticleStatistics(event);
                
                logger.info("Successfully updated user statistics for article published event. Article: {}, Author: {}", 
                           event.getAggregateId(), event.getAuthorId());
            } catch (Exception e) {
                logger.error("Failed to process article published event for user statistics. Article: {}, Author: {}", 
                            event.getAggregateId(), event.getAuthorId(), e);
                throw e; // 重新抛出异常以触发重试机制
            }
        }

        private void updateUserArticleStatistics(ArticlePublishedEvent event) {
            try {
                // 增加用户文章发布统计
                userApplicationService.incrementArticleCount(
                    event.getAuthorId(), 
                    event.getAggregateId(), 
                    "PUBLISHED"
                );
                
                logger.info("User article statistics updated for published article: {} by user: {}", 
                           event.getAggregateId(), event.getAuthorId());
                
            } catch (Exception e) {
                logger.error("Failed to update user article statistics for article: {} by user: {}", 
                            event.getAggregateId(), event.getAuthorId(), e);
                throw e;
            }
        }
    }

    /**
     * 处理评论创建事件 - 更新用户统计
     */
    @Service
    @ConditionalOnProperty(name = "rocketmq.consumer.enabled", havingValue = "true", matchIfMissing = false)
    @RocketMQMessageListener(
        topic = MessageConstants.COMMENT_CREATED_TOPIC,
        consumerGroup = MessageConstants.USER_SERVICE_CONSUMER_GROUP + "_COMMENT_CREATED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class CommentCreatedEventConsumer implements RocketMQListener<CommentCreatedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(CommentCreatedEventConsumer.class);

        @Autowired
        private UserApplicationService userApplicationService;

        @Override
        public void onMessage(CommentCreatedEvent event) {
            try {
                logger.info("Processing comment created event for user statistics update. Comment: {}, Author: {}, Article: {}", 
                           event.getAggregateId(), event.getAuthorId(), event.getArticleId());
                
                // 更新用户评论统计
                updateUserCommentStatistics(event);
                
                logger.info("Successfully updated user statistics for comment created event. Comment: {}, Author: {}", 
                           event.getAggregateId(), event.getAuthorId());
            } catch (Exception e) {
                logger.error("Failed to process comment created event for user statistics. Comment: {}, Author: {}", 
                            event.getAggregateId(), event.getAuthorId(), e);
                // 评论统计更新失败不影响主流程，只记录日志
                logger.warn("Comment statistics update failed, but continuing...");
            }
        }

        private void updateUserCommentStatistics(CommentCreatedEvent event) {
            try {
                // 这里可以实现用户评论统计的更新逻辑
                // 例如：增加用户评论数、活跃度等
                logger.info("User comment statistics would be updated for comment: {} by user: {}", 
                           event.getAggregateId(), event.getAuthorId());
                
                // 实际实现中，这里会调用用户统计服务
                // userStatisticsService.incrementCommentCount(event.getAuthorId());
                
            } catch (Exception e) {
                logger.error("Failed to update user comment statistics for comment: {} by user: {}", 
                            event.getAggregateId(), event.getAuthorId(), e);
                throw e;
            }
        }
    }
}