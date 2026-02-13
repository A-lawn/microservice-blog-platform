package com.blog.platform.article.application.saga;

import com.blog.platform.article.domain.repository.ArticleRepository;
import com.blog.platform.common.domain.article.Article;
import com.blog.platform.common.domain.article.ArticleId;
import com.blog.platform.common.domain.article.ArticleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 文章发布Saga集成测试
 * 测试正常事务流程的完整性、异常情况下的补偿机制、验证事务的最终一致性
 * 
 * 注意：此测试需要完整的微服务环境支持，包括：
 * - Nacos配置中心
 * - MySQL数据库
 * - Redis缓存
 * - Seata分布式事务协调器（如果启用）
 * 
 * 在CI/CD环境中，建议使用Testcontainers或Docker Compose启动依赖服务
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ArticlePublishSagaIntegrationTest {
    
    @Autowired
    private ArticlePublishSaga articlePublishSaga;
    
    @MockBean
    private ArticleRepository articleRepository;
    
    @MockBean
    private RestTemplate restTemplate;
    
    private Article testArticle;
    private String testArticleId;
    private String testAuthorId;
    
    @BeforeEach
    void setUp() {
        testArticleId = "test-article-123";
        testAuthorId = "test-author-456";
        
        testArticle = Mockito.mock(Article.class);
        when(testArticle.getId()).thenReturn(ArticleId.of(testArticleId));
        when(testArticle.getStatus()).thenReturn(ArticleStatus.DRAFT);
        doNothing().when(testArticle).publish();
        
        when(articleRepository.findById(ArticleId.of(testArticleId)))
                .thenReturn(Optional.of(testArticle));
        when(articleRepository.save(any(Article.class)))
                .thenReturn(testArticle);
    }
    
    @Test
    void testSuccessfulArticlePublishSaga() {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
        
        Map<String, Object> result = articlePublishSaga.publishArticle(testArticleId, testAuthorId);
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals(testArticleId, result.get("articleId"));
        assertEquals(testAuthorId, result.get("authorId"));
        assertTrue((Boolean) result.get("articleStatusUpdated"));
        assertTrue((Boolean) result.get("userStatisticsUpdated"));
        
        verify(articleRepository).findById(ArticleId.of(testArticleId));
        verify(articleRepository).save(testArticle);
        verify(testArticle).publish();
        
        verify(restTemplate).postForObject(
                contains("user-service"), 
                any(), 
                any()
        );
        verify(restTemplate, atLeastOnce()).postForObject(
                anyString(), 
                any(), 
                any()
        );
    }
    
    @Test
    void testArticleNotFoundFailure() {
        when(articleRepository.findById(ArticleId.of(testArticleId)))
                .thenReturn(Optional.empty());
        
        assertThrows(Exception.class, () -> {
            articlePublishSaga.publishArticle(testArticleId, testAuthorId);
        });
        
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }
    
    @Test
    void testUserStatisticsUpdateFailureWithCompensation() {
        when(restTemplate.postForObject(contains("user-service"), any(), any()))
                .thenThrow(new RuntimeException("User service unavailable"));
        
        Exception exception = assertThrows(Exception.class, () -> {
            articlePublishSaga.publishArticle(testArticleId, testAuthorId);
        });
        
        assertTrue(exception.getMessage().contains("文章发布事务失败") ||
                   exception.getMessage().contains("User service unavailable"));
        
        verify(testArticle).publish();
        verify(articleRepository).save(testArticle);
    }
    
    @Test
    void testNotificationFailureDoesNotFailSaga() {
        when(restTemplate.postForObject(contains("user-service"), any(), any()))
                .thenReturn(null);
        when(restTemplate.postForObject(contains("notification-service"), any(), any()))
                .thenThrow(new RuntimeException("Notification service unavailable"));
        
        Map<String, Object> result = articlePublishSaga.publishArticle(testArticleId, testAuthorId);
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertTrue((Boolean) result.get("articleStatusUpdated"));
        assertTrue((Boolean) result.get("userStatisticsUpdated"));
        assertFalse((Boolean) result.getOrDefault("notificationSent", false));
    }
    
    @Test
    void testEventualConsistency() {
        when(restTemplate.postForObject(contains("user-service"), any(), any()))
                .thenThrow(new RuntimeException("Temporary failure"))
                .thenReturn(null);
        
        assertThrows(Exception.class, () -> {
            articlePublishSaga.publishArticle(testArticleId, testAuthorId);
        });
        
        reset(restTemplate);
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
        
        Map<String, Object> result = articlePublishSaga.publishArticle(testArticleId, testAuthorId);
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
    }
    
    @Test
    void testCompensationOperations() {
        Map<String, Object> context = Map.of(
                "articleId", testArticleId,
                "authorId", testAuthorId,
                "articleStatusUpdated", true,
                "userStatisticsUpdated", true,
                "originalStatus", ArticleStatus.DRAFT
        );
        
        articlePublishSaga.rollbackUserStatistics(context);
        articlePublishSaga.rollbackArticleStatus(context);
        articlePublishSaga.rollbackNotification(context);
        
        verify(restTemplate).postForObject(
                contains("decrement-article"), 
                any(), 
                any()
        );
    }
    
    @Test
    void testConcurrentTransactions() throws InterruptedException {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
        
        String articleId1 = "article-1";
        String articleId2 = "article-2";
        
        Article article1 = Mockito.mock(Article.class);
        Article article2 = Mockito.mock(Article.class);
        
        when(article1.getId()).thenReturn(ArticleId.of(articleId1));
        when(article2.getId()).thenReturn(ArticleId.of(articleId2));
        when(article1.getStatus()).thenReturn(ArticleStatus.DRAFT);
        when(article2.getStatus()).thenReturn(ArticleStatus.DRAFT);
        
        when(articleRepository.findById(ArticleId.of(articleId1)))
                .thenReturn(Optional.of(article1));
        when(articleRepository.findById(ArticleId.of(articleId2)))
                .thenReturn(Optional.of(article2));
        
        Thread thread1 = new Thread(() -> {
            try {
                articlePublishSaga.publishArticle(articleId1, testAuthorId);
            } catch (Exception e) {
                // Handle exception
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                articlePublishSaga.publishArticle(articleId2, testAuthorId);
            } catch (Exception e) {
                // Handle exception
            }
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        verify(articleRepository, times(2)).findById(any(ArticleId.class));
        verify(articleRepository, times(2)).save(any(Article.class));
    }
}
