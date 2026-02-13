package com.blog.platform.user.infrastructure.messaging;

import com.blog.platform.common.domain.user.UserRegisteredEvent;
import com.blog.platform.common.domain.user.UserProfileUpdatedEvent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(UserEventPublisher.class);

    public static final String USER_REGISTERED_TOPIC = "USER_REGISTERED";
    public static final String USER_PROFILE_UPDATED_TOPIC = "USER_PROFILE_UPDATED";

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        if (rocketMQTemplate == null) {
            logger.warn("RocketMQTemplate not available, skipping user registered event for user: {}", event.getAggregateId());
            return;
        }
        try {
            logger.info("Publishing user registered event for user: {}", event.getAggregateId());
            rocketMQTemplate.asyncSend(USER_REGISTERED_TOPIC, MessageBuilder.withPayload(event).build(), null);
            logger.info("Successfully sent user registered event for user: {}", event.getAggregateId());
        } catch (Exception e) {
            logger.error("Failed to publish user registered event for user: {}, but continuing...", event.getAggregateId(), e);
        }
    }

    public void publishUserProfileUpdatedEvent(UserProfileUpdatedEvent event) {
        if (rocketMQTemplate == null) {
            logger.warn("RocketMQTemplate not available, skipping user profile updated event for user: {}", event.getAggregateId());
            return;
        }
        try {
            logger.info("Publishing user profile updated event for user: {}", event.getAggregateId());
            rocketMQTemplate.asyncSend(USER_PROFILE_UPDATED_TOPIC, MessageBuilder.withPayload(event).build(), null);
            logger.info("Successfully sent user profile updated event for user: {}", event.getAggregateId());
        } catch (Exception e) {
            logger.error("Failed to publish user profile updated event for user: {}, but continuing...", event.getAggregateId(), e);
        }
    }

    public void publishUserEventAsync(String topic, Object event) {
        if (rocketMQTemplate == null) {
            logger.warn("RocketMQTemplate not available, skipping async event to topic: {}", topic);
            return;
        }
        try {
            logger.info("Publishing async user event to topic: {}", topic);
            rocketMQTemplate.asyncSend(topic, MessageBuilder.withPayload(event).build(), null);
            logger.info("Successfully published async user event to topic: {}", topic);
        } catch (Exception e) {
            logger.error("Failed to publish async user event to topic: {}, but continuing...", topic, e);
        }
    }
}
