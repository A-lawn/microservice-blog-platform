package com.blog.platform.common.messaging;

/**
 * 消息常量定义
 * 定义所有消息主题、标签和其他常量
 */
public class MessageConstants {

    // 用户相关主题
    public static final String USER_REGISTERED_TOPIC = "USER_REGISTERED";
    public static final String USER_PROFILE_UPDATED_TOPIC = "USER_PROFILE_UPDATED";

    // 文章相关主题
    public static final String ARTICLE_CREATED_TOPIC = "ARTICLE_CREATED";
    public static final String ARTICLE_PUBLISHED_TOPIC = "ARTICLE_PUBLISHED";
    public static final String ARTICLE_UPDATED_TOPIC = "ARTICLE_UPDATED";
    public static final String ARTICLE_ARCHIVED_TOPIC = "ARTICLE_ARCHIVED";

    // 评论相关主题
    public static final String COMMENT_CREATED_TOPIC = "COMMENT_CREATED";
    public static final String COMMENT_DELETED_TOPIC = "COMMENT_DELETED";
    public static final String COMMENT_MODERATED_TOPIC = "COMMENT_MODERATED";

    // 死信队列主题
    public static final String DLQ_SUFFIX = "_DLQ";
    public static final String RETRY_SUFFIX = "_RETRY";

    // 消息标签
    public static final String TAG_USER = "USER";
    public static final String TAG_ARTICLE = "ARTICLE";
    public static final String TAG_COMMENT = "COMMENT";
    public static final String TAG_NOTIFICATION = "NOTIFICATION";
    public static final String TAG_STATISTICS = "STATISTICS";

    // 消费者组
    public static final String USER_SERVICE_CONSUMER_GROUP = "user-service-consumer";
    public static final String ARTICLE_SERVICE_CONSUMER_GROUP = "article-service-consumer";
    public static final String COMMENT_SERVICE_CONSUMER_GROUP = "comment-service-consumer";

    // 生产者组
    public static final String USER_SERVICE_PRODUCER_GROUP = "user-service-producer";
    public static final String ARTICLE_SERVICE_PRODUCER_GROUP = "article-service-producer";
    public static final String COMMENT_SERVICE_PRODUCER_GROUP = "comment-service-producer";

    // 重试配置
    public static final int MAX_RETRY_TIMES = 3;
    public static final int RETRY_DELAY_LEVEL_1 = 1; // 1s
    public static final int RETRY_DELAY_LEVEL_2 = 2; // 5s
    public static final int RETRY_DELAY_LEVEL_3 = 3; // 10s
    public static final int RETRY_DELAY_LEVEL_4 = 4; // 30s

    // 消息超时配置
    public static final int SEND_MESSAGE_TIMEOUT = 3000; // 3秒
    public static final int CONSUME_MESSAGE_TIMEOUT = 15000; // 15秒

    private MessageConstants() {
        // 工具类，禁止实例化
    }
}