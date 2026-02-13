package com.blog.platform.comment.application.service;

import com.blog.platform.comment.application.dto.CreateCommentRequest;
import com.blog.platform.comment.application.dto.CommentDto;
import com.blog.platform.comment.domain.repository.CommentRepository;
import com.blog.platform.comment.infrastructure.persistence.repository.CommentJpaRepository;
import com.blog.platform.common.domain.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentApplicationServiceTest {
    
    @Mock
    private CommentRepository commentRepository;
    
    @Mock
    private CommentJpaRepository commentJpaRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @InjectMocks
    private CommentApplicationService commentApplicationService;
    
    @Test
    void shouldValidateCreateCommentRequest() {
        // Given
        CreateCommentRequest request = new CreateCommentRequest();
        request.setArticleId("");
        request.setAuthorId("author-1");
        request.setContent("Test comment");
        
        // When & Then
        assertThrows(Exception.class, () -> {
            commentApplicationService.createComment(request);
        });
    }
    
    @Test
    void shouldValidateCommentContent() {
        // Given
        CreateCommentRequest request = new CreateCommentRequest();
        request.setArticleId("article-1");
        request.setAuthorId("author-1");
        request.setContent("");
        
        // When & Then
        assertThrows(Exception.class, () -> {
            commentApplicationService.createComment(request);
        });
    }
    
    @Test
    void shouldValidateCommentContentLength() {
        // Given
        CreateCommentRequest request = new CreateCommentRequest();
        request.setArticleId("article-1");
        request.setAuthorId("author-1");
        request.setContent("a".repeat(5001)); // Exceeds max length
        
        // When & Then
        assertThrows(Exception.class, () -> {
            commentApplicationService.createComment(request);
        });
    }
}