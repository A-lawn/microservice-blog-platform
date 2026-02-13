package com.blog.platform.comment.application.saga;

import com.blog.platform.comment.domain.repository.CommentRepository;
import com.blog.platform.common.domain.comment.Comment;
import com.blog.platform.common.domain.comment.CommentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 评论发布Saga集成测试
 * 测试正常事务流程的完整性、异常情况下的补偿机制、验证事务的最终一致性
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentPublishSagaIntegrationTest {
    
    @Autowired
    private CommentPublishSaga commentPublishSaga;
    
    @MockBean
    private CommentRepository commentRepository;
    
    @MockBean
    private RestTemplate restTemplate;
    
    private Comment testComment;
    private String testCommentId;
    private String testArticleId;
    private String testAuthorId;
    
    @BeforeEach
    void setUp() {
        testCommentId = "test-comment-123";
        testArticleId = "test-article-456";
        testAuthorId = "test-author-789";
        
        // Create a mock comment
        testComment = mock(Comment.class);
        when(testComment.getId()).thenReturn(CommentId.of(testCommentId));
        
        // Mock repository behavior
        when(commentRepository.findById(CommentId.of(testCommentId)))
                .thenReturn(Optional.of(testComment));
    }
    
    /**
     * 测试正常事务流程的完整性
     * 验证所有步骤都能正常执行并完成
     */
    @Test
    void testSuccessfulCommentPublishSaga() {
        // Arrange
        Map<String, Object> articleInfo = new HashMap<String, Object>();
        articleInfo.put("authorId", "different-author");
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(articleInfo);
        when(restTemplate.postForObject(anyString(), any(), eq(Void.class)))
                .thenReturn(null); // Successful calls return null for Void
        
        // Act
        Map<String, Object> result = commentPublishSaga.publishComment(testCommentId, testArticleId, testAuthorId);
        
        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals(testCommentId, result.get("commentId"));
        assertEquals(testArticleId, result.get("articleId"));
        assertEquals(testAuthorId, result.get("authorId"));
        assertTrue((Boolean) result.get("commentValidated"));
        assertTrue((Boolean) result.get("articleStatisticsUpdated"));
        
        // Verify all steps were executed
        verify(commentRepository).findById(CommentId.of(testCommentId));
        
        // Verify external service calls
        verify(restTemplate).getForObject(
                contains("article-service"), 
                eq(Map.class)
        );
        verify(restTemplate).postForObject(
                contains("article-service"), 
                any(), 
                eq(Void.class)
        );
        verify(restTemplate).postForObject(
                contains("notification-service"), 
                any(), 
                eq(Void.class)
        );
    }
    
    /**
     * 测试评论不存在时的异常处理
     */
    @Test
    void testCommentNotFoundFailure() {
        // Arrange
        when(commentRepository.findById(CommentId.of(testCommentId)))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            commentPublishSaga.publishComment(testCommentId, testArticleId, testAuthorId);
        });
        
        // Verify no external calls were made
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(Void.class));
        verify(restTemplate, never()).getForObject(anyString(), eq(Map.class));
    }
    
    /**
     * 测试文章统计更新失败时的补偿机制
     */
    @Test
    void testArticleStatisticsUpdateFailureWithCompensation() {
        // Arrange
        when(restTemplate.postForObject(contains("increment-comment"), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Article service unavailable"));
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            commentPublishSaga.publishComment(testCommentId, testArticleId, testAuthorId);
        });
        
        assertTrue(exception.getMessage().contains("评论发布事务失败"));
        
        // Verify comment validation was done before failure
        verify(commentRepository).findById(CommentId.of(testCommentId));
    }
    
    /**
     * 测试通知服务失败不影响主流程
     */
    @Test
    void testNotificationFailureDoesNotFailSaga() {
        // Arrange
        Map<String, Object> articleInfo = new HashMap<String, Object>();
        articleInfo.put("authorId", "different-author");
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(articleInfo);
        when(restTemplate.postForObject(contains("increment-comment"), any(), eq(Void.class)))
                .thenReturn(null); // Article service succeeds
        when(restTemplate.postForObject(contains("notification-service"), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Notification service unavailable"));
        
        // Act
        Map<String, Object> result = commentPublishSaga.publishComment(testCommentId, testArticleId, testAuthorId);
        
        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertTrue((Boolean) result.get("commentValidated"));
        assertTrue((Boolean) result.get("articleStatisticsUpdated"));
        // Notification failure should not fail the saga
        assertFalse((Boolean) result.getOrDefault("notificationSent", false));
    }
    
    /**
     * 测试自评论不发送通知
     */
    @Test
    void testSelfCommentSkipsNotification() {
        // Arrange - Same author for article and comment
        Map<String, Object> articleInfo = new HashMap<String, Object>();
        articleInfo.put("authorId", testAuthorId); // Same as comment author
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(articleInfo);
        when(restTemplate.postForObject(contains("increment-comment"), any(), eq(Void.class)))
                .thenReturn(null);
        
        // Act
        Map<String, Object> result = commentPublishSaga.publishComment(testCommentId, testArticleId, testAuthorId);
        
        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertTrue((Boolean) result.get("commentValidated"));
        assertTrue((Boolean) result.get("articleStatisticsUpdated"));
        assertFalse((Boolean) result.getOrDefault("notificationSent", false));
        assertTrue((Boolean) result.getOrDefault("notificationSkipped", false));
        
        // Verify notification service was not called
        verify(restTemplate, never()).postForObject(
                contains("notification-service"), 
                any(), 
                eq(Void.class)
        );
    }
    
    /**
     * 测试事务的最终一致性
     * 验证在各种故障场景下，系统能够达到最终一致状态
     */
    @Test
    void testEventualConsistency() {
        // Arrange - Simulate partial failure and recovery
        when(restTemplate.postForObject(contains("increment-comment"), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Temporary failure"))
                .thenReturn(null); // Second call succeeds
        
        // Act - First attempt fails
        assertThrows(Exception.class, () -> {
            commentPublishSaga.publishComment(testCommentId, testArticleId, testAuthorId);
        });
        
        // Reset mocks for retry
        reset(restTemplate);
        Map<String, Object> articleInfo = new HashMap<String, Object>();
        articleInfo.put("authorId", "different-author");
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(articleInfo);
        when(restTemplate.postForObject(anyString(), any(), eq(Void.class)))
                .thenReturn(null);
        
        // Act - Retry succeeds
        Map<String, Object> result = commentPublishSaga.publishComment(testCommentId, testArticleId, testAuthorId);
        
        // Assert - Eventually consistent
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
    }
    
    /**
     * 测试补偿操作的正确性
     */
    @Test
    void testCompensationOperations() {
        // Arrange
        Map<String, Object> context = Map.of(
                "commentId", testCommentId,
                "articleId", testArticleId,
                "authorId", testAuthorId,
                "commentValidated", true,
                "articleStatisticsUpdated", true,
                "notificationSent", true
        );
        
        // Act - Test individual compensation methods
        commentPublishSaga.rollbackNotification(context);
        commentPublishSaga.rollbackArticleStatistics(context);
        commentPublishSaga.rollbackCommentValidation(context);
        
        // Assert - Verify compensation calls were made
        verify(restTemplate).postForObject(
                contains("decrement-comment"), 
                any(), 
                eq(Void.class)
        );
    }
    
    /**
     * 测试并发评论处理
     */
    @Test
    void testConcurrentComments() throws InterruptedException {
        // Arrange
        Map<String, Object> articleInfo = new HashMap<String, Object>();
        articleInfo.put("authorId", "different-author");
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(articleInfo);
        when(restTemplate.postForObject(anyString(), any(), eq(Void.class)))
                .thenReturn(null);
        
        String commentId1 = "comment-1";
        String commentId2 = "comment-2";
        
        Comment comment1 = mock(Comment.class);
        Comment comment2 = mock(Comment.class);
        
        when(comment1.getId()).thenReturn(CommentId.of(commentId1));
        when(comment2.getId()).thenReturn(CommentId.of(commentId2));
        
        when(commentRepository.findById(CommentId.of(commentId1)))
                .thenReturn(Optional.of(comment1));
        when(commentRepository.findById(CommentId.of(commentId2)))
                .thenReturn(Optional.of(comment2));
        
        // Act - Execute concurrent transactions
        Thread thread1 = new Thread(() -> {
            try {
                commentPublishSaga.publishComment(commentId1, testArticleId, testAuthorId);
            } catch (Exception e) {
                // Handle exception
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                commentPublishSaga.publishComment(commentId2, testArticleId, testAuthorId);
            } catch (Exception e) {
                // Handle exception
            }
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        // Assert - Both transactions should complete
        verify(commentRepository, times(2)).findById(any(CommentId.class));
        verify(restTemplate, times(2)).getForObject(anyString(), eq(Map.class));
        verify(restTemplate, times(2)).postForObject(
                contains("increment-comment"), 
                any(), 
                eq(Void.class)
        );
    }
    
    /**
     * 测试获取文章作者信息失败的处理
     */
    @Test
    void testGetArticleAuthorFailure() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Article service unavailable"));
        when(restTemplate.postForObject(contains("increment-comment"), any(), eq(Void.class)))
                .thenReturn(null);
        
        // Act
        Map<String, Object> result = commentPublishSaga.publishComment(testCommentId, testArticleId, testAuthorId);
        
        // Assert - Should still succeed, just skip notification
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertTrue((Boolean) result.get("commentValidated"));
        assertTrue((Boolean) result.get("articleStatisticsUpdated"));
        assertFalse((Boolean) result.getOrDefault("notificationSent", false));
    }
}