package com.blog.platform.comment.infrastructure.messaging;

import com.blog.platform.common.domain.article.ArticlePublishedEvent;
import com.blog.platform.common.domain.article.ArticleArchivedEvent;
import com.blog.platform.common.domain.user.UserRegisteredEvent;
import com.blog.platform.common.messaging.MessageConstants;
import com.blog.platform.common.messaging.MessageRetryHandler;
import com.blog.platform.comment.application.service.CommentApplicationService;
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
    private CommentApplicationService commentApplicationService;

    @Autowired
    private MessageRetryHandler messageRetryHandler;

    /**
     * 处理文章发布事件 - 启用评论功能
     */
    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.ARTICLE_PUBLISHED_TOPIC,
        consumerGroup = MessageConstants.COMMENT_SERVICE_CONSUMER_GROUP + "_ARTICLE_PUBLISHED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class ArticlePublishedEventConsumer implements RocketMQListener<ArticlePublishedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(ArticlePublishedEventConsumer.class);

        @Override
        public void onMessage(ArticlePublishedEvent event) {
            try {
                logger.info("Processing article published event for comment service. Article: {}", 
                           event.getAggregateId());
                
                // 启用文章的评论功能
                enableArticleComments(event);
                
                logger.info("Successfully processed article published event for comment service. Article: {}", 
                           event.getAggregateId());
            } catch (Exception e) {
                logger.error("Failed to process article published event for comment service. Article: {}", 
                            event.getAggregateId(), e);
                // 文章发布事件处理失败不影响主流程，只记录日志
                logger.warn("Article published event processing failed in comment service, but continuing...");
            }
        }

        private void enableArticleComments(ArticlePublishedEvent event) {
            try {
                // 这里可以实现文章发布后的评论服务初始化逻辑
                // 例如：启用评论功能、设置评论规则等
                logger.info("Article comments would be enabled for article: {}", event.getAggregateId());
                
                // 实际实现中，这里可能会：
                // 1. 在评论服务中创建文章记录
                // 2. 设置评论权限和规则
                // 3. 初始化评论统计等
                
            } catch (Exception e) {
                logger.error("Failed to enable article comments for article: {}", event.getAggregateId(), e);
                throw e;
            }
        }
    }

    /**
     * 处理文章归档事件 - 禁用评论功能
     */
    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.ARTICLE_ARCHIVED_TOPIC,
        consumerGroup = MessageConstants.COMMENT_SERVICE_CONSUMER_GROUP + "_ARTICLE_ARCHIVED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class ArticleArchivedEventConsumer implements RocketMQListener<ArticleArchivedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(ArticleArchivedEventConsumer.class);

        @Override
        public void onMessage(ArticleArchivedEvent event) {
            try {
                logger.info("Processing article archived event for comment service. Article: {}", 
                           event.getAggregateId());
                
                // 禁用文章的评论功能
                disableArticleComments(event);
                
                logger.info("Successfully processed article archived event for comment service. Article: {}", 
                           event.getAggregateId());
            } catch (Exception e) {
                logger.error("Failed to process article archived event for comment service. Article: {}", 
                            event.getAggregateId(), e);
                // 文章归档事件处理失败不影响主流程，只记录日志
                logger.warn("Article archived event processing failed in comment service, but continuing...");
            }
        }

        private void disableArticleComments(ArticleArchivedEvent event) {
            try {
                // 这里可以实现文章归档后的评论服务处理逻辑
                // 例如：禁用新评论、保留现有评论等
                logger.info("Article comments would be disabled for archived article: {}", event.getAggregateId());
                
                // 实际实现中，这里可能会：
                // 1. 禁用文章的新评论功能
                // 2. 保留现有评论但设为只读
                // 3. 更新评论状态等
                
            } catch (Exception e) {
                logger.error("Failed to disable article comments for archived article: {}", event.getAggregateId(), e);
                throw e;
            }
        }
    }

    /**
     * 处理用户注册事件 - 初始化用户评论数据
     */
    @Service
    @RocketMQMessageListener(
        topic = MessageConstants.USER_REGISTERED_TOPIC,
        consumerGroup = MessageConstants.COMMENT_SERVICE_CONSUMER_GROUP + "_USER_REGISTERED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class UserRegisteredEventConsumer implements RocketMQListener<UserRegisteredEvent> {

        private static final Logger logger = LoggerFactory.getLogger(UserRegisteredEventConsumer.class);

        @Override
        public void onMessage(UserRegisteredEvent event) {
            try {
                logger.info("Processing user registered event for comment service initialization. User: {}", 
                           event.getAggregateId());
                
                // 初始化用户相关的评论数据
                initializeUserCommentData(event);
                
                logger.info("Successfully processed user registered event for comment service. User: {}", 
                           event.getAggregateId());
            } catch (Exception e) {
                logger.error("Failed to process user registered event for comment service. User: {}", 
                            event.getAggregateId(), e);
                // 用户注册事件处理失败不影响主流程，只记录日志
                logger.warn("User registration event processing failed in comment service, but continuing...");
            }
        }

        private void initializeUserCommentData(UserRegisteredEvent event) {
            try {
                // 这里可以实现用户注册后的评论服务初始化逻辑
                // 例如：创建用户评论统计记录、设置评论权限等
                logger.info("User comment data would be initialized for user: {}", event.getAggregateId());
                
                // 实际实现中，这里可能会：
                // 1. 创建用户评论统计记录
                // 2. 设置用户评论权限和规则
                // 3. 初始化用户评论偏好等
                
            } catch (Exception e) {
                logger.error("Failed to initialize user comment data for user: {}", event.getAggregateId(), e);
                throw e;
            }
        }
    }
}