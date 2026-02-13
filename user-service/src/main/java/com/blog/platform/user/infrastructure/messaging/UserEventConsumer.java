package com.blog.platform.user.infrastructure.messaging;

import com.blog.platform.common.domain.user.UserRegisteredEvent;
import com.blog.platform.common.domain.user.UserProfileUpdatedEvent;
import com.blog.platform.common.messaging.MessageConstants;
import com.blog.platform.common.messaging.MessageRetryHandler;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "rocketmq.consumer.enabled", havingValue = "true", matchIfMissing = false)
public class UserEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventConsumer.class);

    @Autowired
    private MessageRetryHandler messageRetryHandler;

    /**
     * 用户注册事件消费者
     * 处理用户注册后的业务逻辑
     */
    @Service
    @ConditionalOnProperty(name = "rocketmq.consumer.enabled", havingValue = "true", matchIfMissing = false)
    @RocketMQMessageListener(
        topic = MessageConstants.USER_REGISTERED_TOPIC,
        consumerGroup = MessageConstants.USER_SERVICE_CONSUMER_GROUP + "_REGISTERED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class UserRegisteredEventConsumer implements RocketMQListener<UserRegisteredEvent> {

        private static final Logger logger = LoggerFactory.getLogger(UserRegisteredEventConsumer.class);

        @Override
        public void onMessage(UserRegisteredEvent event) {
            try {
                logger.info("Processing user registered event for user: {}", event.getAggregateId());
                
                // 处理用户注册后的业务逻辑
                processUserRegistration(event);
                
                logger.info("Successfully processed user registered event for user: {}", event.getAggregateId());
            } catch (Exception e) {
                logger.error("Failed to process user registered event for user: {}", event.getAggregateId(), e);
                throw e; // 重新抛出异常以触发重试机制
            }
        }

        private void processUserRegistration(UserRegisteredEvent event) {
            // 实现用户注册后的处理逻辑
            // 例如：发送欢迎邮件、初始化用户统计等
            logger.info("Processing user registration for user: {} with email: {}", 
                       event.getAggregateId(), event.getEmail());
            
            // 这里可以调用其他服务或执行本地业务逻辑
            // 例如：初始化用户统计、发送通知等
        }
    }

    /**
     * 用户资料更新事件消费者
     */
    @Service
    @ConditionalOnProperty(name = "rocketmq.consumer.enabled", havingValue = "true", matchIfMissing = false)
    @RocketMQMessageListener(
        topic = MessageConstants.USER_PROFILE_UPDATED_TOPIC,
        consumerGroup = MessageConstants.USER_SERVICE_CONSUMER_GROUP + "_PROFILE_UPDATED",
        maxReconsumeTimes = MessageConstants.MAX_RETRY_TIMES
    )
    public static class UserProfileUpdatedEventConsumer implements RocketMQListener<UserProfileUpdatedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(UserProfileUpdatedEventConsumer.class);

        @Override
        public void onMessage(UserProfileUpdatedEvent event) {
            try {
                logger.info("Processing user profile updated event for user: {}", event.getAggregateId());
                
                // 处理用户资料更新后的业务逻辑
                processUserProfileUpdate(event);
                
                logger.info("Successfully processed user profile updated event for user: {}", event.getAggregateId());
            } catch (Exception e) {
                logger.error("Failed to process user profile updated event for user: {}", event.getAggregateId(), e);
                throw e; // 重新抛出异常以触发重试机制
            }
        }

        private void processUserProfileUpdate(UserProfileUpdatedEvent event) {
            // 实现用户资料更新后的处理逻辑
            // 例如：更新缓存、同步到其他系统等
            logger.info("Processing user profile update for user: {}", event.getAggregateId());
            
            // 这里可以调用其他服务或执行本地业务逻辑
        }
    }
}