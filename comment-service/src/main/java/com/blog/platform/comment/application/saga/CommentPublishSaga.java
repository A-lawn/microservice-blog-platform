package com.blog.platform.comment.application.saga;

import com.blog.platform.comment.domain.exception.CommentDomainException;
import com.blog.platform.comment.domain.repository.CommentRepository;
import com.blog.platform.common.domain.comment.Comment;
import com.blog.platform.common.domain.comment.CommentId;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 评论发布Saga事务编排器（仅当 feature.seata.enabled=true 时生效）
 * 协调评论发布涉及的多个步骤：创建评论、更新文章统计、发送通知
 */
@Component
@ConditionalOnProperty(name = "feature.seata.enabled", havingValue = "true")
public class CommentPublishSaga {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentPublishSaga.class);
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * 开始评论发布Saga事务
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 60000)
    public Map<String, Object> publishComment(String commentId, String articleId, String authorId) {
        logger.info("Starting comment publish saga for comment: {}, article: {}, author: {}", 
                   commentId, articleId, authorId);
        
        Map<String, Object> context = new HashMap<>();
        context.put("commentId", commentId);
        context.put("articleId", articleId);
        context.put("authorId", authorId);
        context.put("startTime", LocalDateTime.now());
        
        try {
            // Step 1: Validate comment exists and is active
            validateComment(context);
            
            // Step 2: Update article statistics
            updateArticleStatistics(context);
            
            // Step 3: Send notification to article author
            sendCommentNotification(context);
            
            logger.info("Comment publish saga completed successfully for comment: {}", commentId);
            context.put("status", "SUCCESS");
            
        } catch (Exception e) {
            logger.error("Comment publish saga failed for comment: {}", commentId, e);
            context.put("status", "FAILED");
            context.put("error", e.getMessage());
            
            // Trigger compensation
            compensateCommentPublish(context);
            throw new CommentDomainException("SAGA_FAILED", "评论发布事务失败: " + e.getMessage());
        }
        
        return context;
    }
    
    /**
     * 步骤1: 验证评论存在且状态正确
     */
    public void validateComment(Map<String, Object> context) {
        String commentId = (String) context.get("commentId");
        
        logger.info("Validating comment: {}", commentId);
        
        try {
            Comment comment = commentRepository.findById(CommentId.of(commentId))
                    .orElseThrow(() -> new CommentDomainException("COMMENT_NOT_FOUND", "评论不存在: " + commentId));
            
            // Store comment details for later use
            context.put("comment", comment);
            context.put("commentValidated", true);
            
            logger.info("Comment validation successful for comment: {}", commentId);
            
        } catch (Exception e) {
            logger.error("Failed to validate comment: {}", commentId, e);
            context.put("commentValidated", false);
            throw e;
        }
    }
    
    /**
     * 步骤2: 更新文章统计信息
     */
    public void updateArticleStatistics(Map<String, Object> context) {
        String articleId = (String) context.get("articleId");
        String commentId = (String) context.get("commentId");
        
        logger.info("Updating article statistics for article: {}", articleId);
        
        try {
            // Call article service to increment comment count
            String articleServiceUrl = "http://article-service/api/articles/" + articleId + "/statistics/increment-comment";
            Map<String, Object> request = new HashMap<>();
            request.put("commentId", commentId);
            request.put("operation", "COMMENT_CREATED");
            
            restTemplate.postForObject(articleServiceUrl, request, Void.class);
            
            context.put("articleStatisticsUpdated", true);
            logger.info("Article statistics updated successfully for article: {}", articleId);
            
        } catch (Exception e) {
            logger.error("Failed to update article statistics for article: {}", articleId, e);
            context.put("articleStatisticsUpdated", false);
            throw new CommentDomainException("ARTICLE_STATS_UPDATE_FAILED", "更新文章统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 步骤3: 发送评论通知
     */
    public void sendCommentNotification(Map<String, Object> context) {
        String articleId = (String) context.get("articleId");
        String commentId = (String) context.get("commentId");
        String authorId = (String) context.get("authorId");
        
        logger.info("Sending comment notification for comment: {}", commentId);
        
        try {
            // First, get article author information
            String articleServiceUrl = "http://article-service/api/articles/" + articleId + "/author";
            Map<String, Object> articleInfo = restTemplate.getForObject(articleServiceUrl, Map.class);
            
            if (articleInfo != null) {
                String articleAuthorId = (String) articleInfo.get("authorId");
                
                // Don't send notification if commenting on own article
                if (!authorId.equals(articleAuthorId)) {
                    // Call notification service to send comment notification
                    String notificationServiceUrl = "http://notification-service/api/notifications/comment-created";
                    Map<String, Object> request = new HashMap<>();
                    request.put("commentId", commentId);
                    request.put("articleId", articleId);
                    request.put("commentAuthorId", authorId);
                    request.put("articleAuthorId", articleAuthorId);
                    request.put("type", "COMMENT_CREATED");
                    
                    // This is a best-effort operation, don't fail the saga if notification fails
                    try {
                        restTemplate.postForObject(notificationServiceUrl, request, Void.class);
                        context.put("notificationSent", true);
                        logger.info("Comment notification sent successfully for comment: {}", commentId);
                    } catch (Exception e) {
                        logger.warn("Failed to send comment notification for comment: {}, continuing saga", commentId, e);
                        context.put("notificationSent", false);
                        context.put("notificationError", e.getMessage());
                    }
                } else {
                    logger.info("Skipping notification for self-comment on article: {}", articleId);
                    context.put("notificationSent", false);
                    context.put("notificationSkipped", true);
                }
            }
            
        } catch (Exception e) {
            logger.error("Notification step failed for comment: {}", commentId, e);
            context.put("notificationSent", false);
            // Don't throw exception for notification failures
        }
    }
    
    // Compensation methods
    
    /**
     * 补偿方法: 回滚评论验证（通常不需要实际操作）
     */
    public void rollbackCommentValidation(Map<String, Object> context) {
        String commentId = (String) context.get("commentId");
        
        if (Boolean.TRUE.equals(context.get("commentValidated"))) {
            logger.info("Comment validation rollback for comment: {} (no action needed)", commentId);
            // Validation rollback typically doesn't require action
        }
    }
    
    /**
     * 补偿方法: 回滚文章统计
     */
    public void rollbackArticleStatistics(Map<String, Object> context) {
        String articleId = (String) context.get("articleId");
        String commentId = (String) context.get("commentId");
        
        if (Boolean.TRUE.equals(context.get("articleStatisticsUpdated"))) {
            logger.info("Rolling back article statistics for article: {}", articleId);
            
            try {
                // Call article service to decrement comment count
                String articleServiceUrl = "http://article-service/api/articles/" + articleId + "/statistics/decrement-comment";
                Map<String, Object> request = new HashMap<>();
                request.put("commentId", commentId);
                request.put("operation", "ROLLBACK_COMMENT");
                
                restTemplate.postForObject(articleServiceUrl, request, Void.class);
                logger.info("Article statistics rollback completed for article: {}", articleId);
                
            } catch (Exception e) {
                logger.error("Failed to rollback article statistics for article: {}", articleId, e);
            }
        }
    }
    
    /**
     * 补偿方法: 回滚通知（通常不需要实际操作）
     */
    public void rollbackNotification(Map<String, Object> context) {
        String commentId = (String) context.get("commentId");
        
        if (Boolean.TRUE.equals(context.get("notificationSent"))) {
            logger.info("Notification rollback for comment: {} (no action needed)", commentId);
            // Notifications are typically not rolled back, just logged
        }
    }
    
    /**
     * 整体补偿方法
     */
    private void compensateCommentPublish(Map<String, Object> context) {
        logger.info("Starting compensation for comment publish saga");
        
        try {
            rollbackNotification(context);
            rollbackArticleStatistics(context);
            rollbackCommentValidation(context);
            
            logger.info("Comment publish saga compensation completed");
        } catch (Exception e) {
            logger.error("Failed to complete saga compensation", e);
        }
    }
}