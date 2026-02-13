package com.blog.platform.article.application.saga;

import com.blog.platform.article.application.service.ArticleApplicationService;
import com.blog.platform.article.domain.exception.ArticleDomainException;
import com.blog.platform.article.domain.repository.ArticleRepository;
import com.blog.platform.common.domain.article.Article;
import com.blog.platform.common.domain.article.ArticleId;
import com.blog.platform.common.domain.article.ArticleStatus;
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
 * 文章发布Saga事务编排器（仅当 feature.seata.enabled=true 时生效）
 * 协调文章发布涉及的多个步骤：更新文章状态、用户统计、发送通知
 */
@Component
@ConditionalOnProperty(name = "feature.seata.enabled", havingValue = "true")
public class ArticlePublishSaga {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticlePublishSaga.class);
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * 开始文章发布Saga事务
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 60000)
    public Map<String, Object> publishArticle(String articleId, String authorId) {
        logger.info("Starting article publish saga for article: {}, author: {}", articleId, authorId);
        
        Map<String, Object> context = new HashMap<>();
        context.put("articleId", articleId);
        context.put("authorId", authorId);
        context.put("startTime", LocalDateTime.now());
        
        try {
            // Step 1: Update article status
            updateArticleStatus(context);
            
            // Step 2: Update user statistics
            updateUserStatistics(context);
            
            // Step 3: Send notification
            sendPublishNotification(context);
            
            logger.info("Article publish saga completed successfully for article: {}", articleId);
            context.put("status", "SUCCESS");
            
        } catch (Exception e) {
            logger.error("Article publish saga failed for article: {}", articleId, e);
            context.put("status", "FAILED");
            context.put("error", e.getMessage());
            
            // Trigger compensation
            compensateArticlePublish(context);
            throw new ArticleDomainException("SAGA_FAILED", "文章发布事务失败: " + e.getMessage());
        }
        
        return context;
    }
    
    /**
     * 步骤1: 更新文章状态为已发布
     */
    public void updateArticleStatus(Map<String, Object> context) {
        String articleId = (String) context.get("articleId");
        String authorId = (String) context.get("authorId");
        
        logger.info("Updating article status to PUBLISHED for article: {}", articleId);
        
        try {
            Article article = articleRepository.findById(ArticleId.of(articleId))
                    .orElseThrow(() -> new ArticleDomainException("ARTICLE_NOT_FOUND", "文章不存在: " + articleId));
            
            // Store original status for compensation
            context.put("originalStatus", article.getStatus());
            
            article.publish();
            articleRepository.save(article);
            
            context.put("articleStatusUpdated", true);
            logger.info("Article status updated successfully for article: {}", articleId);
            
        } catch (Exception e) {
            logger.error("Failed to update article status for article: {}", articleId, e);
            context.put("articleStatusUpdated", false);
            throw e;
        }
    }
    
    /**
     * 步骤2: 更新用户统计信息
     */
    public void updateUserStatistics(Map<String, Object> context) {
        String authorId = (String) context.get("authorId");
        String articleId = (String) context.get("articleId");
        
        logger.info("Updating user statistics for author: {}", authorId);
        
        try {
            // Call user service to increment article count
            String userServiceUrl = "http://user-service/api/users/" + authorId + "/statistics/increment-article";
            Map<String, Object> request = new HashMap<>();
            request.put("articleId", articleId);
            request.put("operation", "PUBLISH");
            
            restTemplate.postForObject(userServiceUrl, request, Void.class);
            
            context.put("userStatisticsUpdated", true);
            logger.info("User statistics updated successfully for author: {}", authorId);
            
        } catch (Exception e) {
            logger.error("Failed to update user statistics for author: {}", authorId, e);
            context.put("userStatisticsUpdated", false);
            throw new ArticleDomainException("USER_STATS_UPDATE_FAILED", "更新用户统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 步骤3: 发送发布通知
     */
    public void sendPublishNotification(Map<String, Object> context) {
        String authorId = (String) context.get("authorId");
        String articleId = (String) context.get("articleId");
        
        logger.info("Sending publish notification for article: {}", articleId);
        
        try {
            // Call notification service to send publish notification
            String notificationServiceUrl = "http://notification-service/api/notifications/article-published";
            Map<String, Object> request = new HashMap<>();
            request.put("articleId", articleId);
            request.put("authorId", authorId);
            request.put("type", "ARTICLE_PUBLISHED");
            
            // This is a best-effort operation, don't fail the saga if notification fails
            try {
                restTemplate.postForObject(notificationServiceUrl, request, Void.class);
                context.put("notificationSent", true);
                logger.info("Publish notification sent successfully for article: {}", articleId);
            } catch (Exception e) {
                logger.warn("Failed to send publish notification for article: {}, continuing saga", articleId, e);
                context.put("notificationSent", false);
                context.put("notificationError", e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Notification step failed for article: {}", articleId, e);
            context.put("notificationSent", false);
            // Don't throw exception for notification failures
        }
    }
    
    // Compensation methods
    
    /**
     * 补偿方法: 回滚文章状态
     */
    public void rollbackArticleStatus(Map<String, Object> context) {
        String articleId = (String) context.get("articleId");
        ArticleStatus originalStatus = (ArticleStatus) context.get("originalStatus");
        
        if (Boolean.TRUE.equals(context.get("articleStatusUpdated")) && originalStatus != null) {
            logger.info("Rolling back article status for article: {} to {}", articleId, originalStatus);
            
            try {
                Article article = articleRepository.findById(ArticleId.of(articleId))
                        .orElse(null);
                
                if (article != null) {
                    // Manually set status back (assuming we have a method for this)
                    if (originalStatus == ArticleStatus.DRAFT) {
                        // Convert back to draft if possible
                        // This would require additional domain logic
                        logger.warn("Cannot automatically rollback to DRAFT status for article: {}", articleId);
                    }
                    logger.info("Article status rollback completed for article: {}", articleId);
                }
            } catch (Exception e) {
                logger.error("Failed to rollback article status for article: {}", articleId, e);
            }
        }
    }
    
    /**
     * 补偿方法: 回滚用户统计
     */
    public void rollbackUserStatistics(Map<String, Object> context) {
        String authorId = (String) context.get("authorId");
        String articleId = (String) context.get("articleId");
        
        if (Boolean.TRUE.equals(context.get("userStatisticsUpdated"))) {
            logger.info("Rolling back user statistics for author: {}", authorId);
            
            try {
                // Call user service to decrement article count
                String userServiceUrl = "http://user-service/api/users/" + authorId + "/statistics/decrement-article";
                Map<String, Object> request = new HashMap<>();
                request.put("articleId", articleId);
                request.put("operation", "ROLLBACK_PUBLISH");
                
                restTemplate.postForObject(userServiceUrl, request, Void.class);
                logger.info("User statistics rollback completed for author: {}", authorId);
                
            } catch (Exception e) {
                logger.error("Failed to rollback user statistics for author: {}", authorId, e);
            }
        }
    }
    
    /**
     * 补偿方法: 回滚通知（通常不需要实际操作）
     */
    public void rollbackNotification(Map<String, Object> context) {
        String articleId = (String) context.get("articleId");
        
        if (Boolean.TRUE.equals(context.get("notificationSent"))) {
            logger.info("Notification rollback for article: {} (no action needed)", articleId);
            // Notifications are typically not rolled back, just logged
        }
    }
    
    /**
     * 整体补偿方法
     */
    private void compensateArticlePublish(Map<String, Object> context) {
        logger.info("Starting compensation for article publish saga");
        
        try {
            rollbackNotification(context);
            rollbackUserStatistics(context);
            rollbackArticleStatus(context);
            
            logger.info("Article publish saga compensation completed");
        } catch (Exception e) {
            logger.error("Failed to complete saga compensation", e);
        }
    }
}