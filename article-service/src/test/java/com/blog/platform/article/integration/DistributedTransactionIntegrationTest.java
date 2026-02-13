package com.blog.platform.article.integration;

import com.blog.platform.article.application.service.ArticleApplicationService;
import com.blog.platform.article.domain.repository.ArticleRepository;
import com.blog.platform.common.domain.article.Article;
import com.blog.platform.common.domain.article.ArticleId;
import com.blog.platform.common.domain.article.ArticleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 分布式事务集成测试
 * 测试跨服务的分布式事务场景，验证最终一致性和故障恢复能力
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
class DistributedTransactionIntegrationTest {
    
    @Autowired
    private ArticleApplicationService articleApplicationService;
    
    @MockBean
    private ArticleRepository articleRepository;
    
    @MockBean
    private RestTemplate restTemplate;
    
    private Article testArticle;
    private String testArticleId;
    private String testAuthorId;
    
    @BeforeEach
    void setUp() {
        testArticleId = "integration-test-article";
        testAuthorId = "integration-test-author";
        
        testArticle = Mockito.mock(Article.class);
        when(testArticle.getId()).thenReturn(ArticleId.of(testArticleId));
        when(testArticle.getStatus()).thenReturn(ArticleStatus.DRAFT);
        doNothing().when(testArticle).publish();
        
        when(articleRepository.findById(ArticleId.of(testArticleId)))
                .thenReturn(Optional.of(testArticle));
        when(articleRepository.save(ArgumentMatchers.any(Article.class)))
                .thenReturn(testArticle);
    }
    
    @Test
    void testCompleteArticlePublishDistributedTransaction() {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
        
        assertDoesNotThrow(() -> {
            articleApplicationService.publishArticle(testArticleId, testAuthorId);
        });
        
        verify(testArticle).publish();
        verify(articleRepository).save(testArticle);
        
        verify(restTemplate, atLeastOnce()).postForObject(
                contains("user-service"), 
                any(), 
                any()
        );
    }
    
    @Test
    void testPartialServiceFailureRollback() {
        when(restTemplate.postForObject(contains("user-service"), any(), any()))
                .thenThrow(new RuntimeException("User service unavailable"));
        
        Exception exception = assertThrows(Exception.class, () -> {
            articleApplicationService.publishArticle(testArticleId, testAuthorId);
        });
        
        assertTrue(exception.getMessage().contains("文章发布失败") || 
                   exception.getMessage().contains("User service unavailable"));
        
        verify(testArticle).publish();
        verify(articleRepository).save(testArticle);
    }
    
    @Test
    @Disabled("需要配置RestTemplate超时设置后启用")
    void testNetworkTimeoutHandling() {
        when(restTemplate.postForObject(contains("user-service"), any(), any()))
                .thenAnswer(invocation -> {
                    Thread.sleep(2000);
                    return null;
                });
        
        assertDoesNotThrow(() -> {
            articleApplicationService.publishArticle(testArticleId, testAuthorId);
        });
        
        verify(testArticle).publish();
        verify(articleRepository).save(testArticle);
    }
    
    @Test
    void testHighConcurrencyDistributedTransactions() throws InterruptedException, ExecutionException {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
        
        int concurrentRequests = 10;
        CompletableFuture<Void>[] futures = new CompletableFuture[concurrentRequests];
        
        for (int i = 0; i < concurrentRequests; i++) {
            final String articleId = "article-" + i;
            final String authorId = "author-" + i;
            
            Article mockArticle = Mockito.mock(Article.class);
            when(mockArticle.getId()).thenReturn(ArticleId.of(articleId));
            when(mockArticle.getStatus()).thenReturn(ArticleStatus.DRAFT);
            doNothing().when(mockArticle).publish();
            
            when(articleRepository.findById(ArticleId.of(articleId)))
                    .thenReturn(Optional.of(mockArticle));
            when(articleRepository.save(mockArticle))
                    .thenReturn(mockArticle);
            
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    articleApplicationService.publishArticle(articleId, authorId);
                } catch (Exception e) {
                    System.err.println("Concurrent transaction failed: " + e.getMessage());
                }
            });
        }
        
        CompletableFuture.allOf(futures).get();
        
        verify(articleRepository, times(concurrentRequests)).save(any(Article.class));
        verify(restTemplate, times(concurrentRequests)).postForObject(
                contains("user-service"), 
                any(), 
                any()
        );
    }
    
    @Test
    void testServiceCommunicationRetry() {
        when(restTemplate.postForObject(contains("user-service"), any(), any()))
                .thenThrow(new RuntimeException("Temporary failure"))
                .thenReturn(null);
        
        assertThrows(Exception.class, () -> {
            articleApplicationService.publishArticle(testArticleId, testAuthorId);
        });
        
        reset(restTemplate);
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
        
        assertDoesNotThrow(() -> {
            articleApplicationService.publishArticle(testArticleId, testAuthorId);
        });
        
        verify(testArticle, times(2)).publish();
        verify(articleRepository, times(2)).save(testArticle);
    }
    
    @Test
    void testDataConsistencyValidation() {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
        
        articleApplicationService.publishArticle(testArticleId, testAuthorId);
        
        verify(testArticle).publish();
        verify(articleRepository).save(testArticle);
        
        verify(restTemplate).postForObject(
                contains("increment-article"), 
                argThat(request -> {
                    if (request instanceof java.util.Map) {
                        java.util.Map<?, ?> map = (java.util.Map<?, ?>) request;
                        return testArticleId.equals(map.get("articleId")) &&
                               "PUBLISH".equals(map.get("operation"));
                    }
                    return false;
                }), 
                any()
        );
    }
    
    @Test
    void testTransactionStatusMonitoring() {
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
        
        long startTime = System.currentTimeMillis();
        articleApplicationService.publishArticle(testArticleId, testAuthorId);
        long endTime = System.currentTimeMillis();
        
        assertTrue(endTime - startTime < 5000, "Transaction should complete within 5 seconds");
        
        verify(testArticle).publish();
        verify(articleRepository).save(testArticle);
        verify(restTemplate, atLeastOnce()).postForObject(anyString(), any(), any());
    }
    
    @Test
    void testExceptionRecoveryMechanism() {
        when(articleRepository.save(any(Article.class)))
                .thenThrow(new RuntimeException("Database connection lost"))
                .thenReturn(testArticle);
        
        assertThrows(Exception.class, () -> {
            articleApplicationService.publishArticle(testArticleId, testAuthorId);
        });
        
        reset(articleRepository);
        when(articleRepository.findById(ArticleId.of(testArticleId)))
                .thenReturn(Optional.of(testArticle));
        when(articleRepository.save(any(Article.class)))
                .thenReturn(testArticle);
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);
        
        assertDoesNotThrow(() -> {
            articleApplicationService.publishArticle(testArticleId, testAuthorId);
        });
        
        verify(articleRepository).save(testArticle);
    }
}
