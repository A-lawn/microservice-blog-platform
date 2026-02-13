package com.blog.platform.comment.infrastructure.persistence.repository;

import com.blog.platform.comment.infrastructure.persistence.entity.CommentEntity;
import com.blog.platform.common.domain.comment.CommentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CommentJpaRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private CommentJpaRepository commentRepository;
    
    private CommentEntity testComment;
    private CommentEntity replyComment;
    
    @BeforeEach
    void setUp() {
        testComment = new CommentEntity("comment-001", "article-001", "author-001", "This is a test comment");
        testComment.setStatus(CommentStatus.ACTIVE);
        entityManager.persistAndFlush(testComment);
        
        replyComment = new CommentEntity("comment-002", "article-001", "author-002", "This is a reply");
        replyComment.setParentId("comment-001");
        replyComment.setStatus(CommentStatus.ACTIVE);
        entityManager.persistAndFlush(replyComment);
    }
    
    @Test
    void findByArticleId_ShouldReturnCommentsForArticle() {
        // Given
        CommentEntity anotherComment = new CommentEntity("comment-003", "article-001", "author-003", "Another comment");
        entityManager.persistAndFlush(anotherComment);
        
        // When
        List<CommentEntity> comments = commentRepository.findByArticleId("article-001");
        
        // Then
        assertThat(comments).hasSize(3);
        assertThat(comments).allMatch(comment -> comment.getArticleId().equals("article-001"));
    }
    
    @Test
    void findActiveCommentsByArticleId_ShouldReturnOnlyActiveComments() {
        // Given
        CommentEntity hiddenComment = new CommentEntity("comment-003", "article-001", "author-003", "Hidden comment");
        hiddenComment.setStatus(CommentStatus.HIDDEN);
        entityManager.persistAndFlush(hiddenComment);
        
        // When
        List<CommentEntity> activeComments = commentRepository.findActiveCommentsByArticleId("article-001");
        
        // Then
        assertThat(activeComments).hasSize(2); // testComment + replyComment
        assertThat(activeComments).allMatch(comment -> comment.getStatus() == CommentStatus.ACTIVE);
    }
    
    @Test
    void findActiveCommentsByArticleIdWithPagination_ShouldReturnPagedResults() {
        // Given
        for (int i = 3; i <= 5; i++) {
            CommentEntity comment = new CommentEntity("comment-00" + i, "article-001", "author-00" + i, "Comment " + i);
            comment.setStatus(CommentStatus.ACTIVE);
            entityManager.persistAndFlush(comment);
        }
        
        // When
        Page<CommentEntity> result = commentRepository.findActiveCommentsByArticleId("article-001", PageRequest.of(0, 2));
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5); // testComment + replyComment + 3 new comments
    }
    
    @Test
    void findByAuthorId_ShouldReturnCommentsByAuthor() {
        // Given
        CommentEntity anotherCommentByAuthor = new CommentEntity("comment-003", "article-002", "author-001", "Another comment by same author");
        entityManager.persistAndFlush(anotherCommentByAuthor);
        
        // When
        List<CommentEntity> comments = commentRepository.findByAuthorId("author-001");
        
        // Then
        assertThat(comments).hasSize(2);
        assertThat(comments).allMatch(comment -> comment.getAuthorId().equals("author-001"));
    }
    
    @Test
    void findByParentId_ShouldReturnReplies() {
        // Given
        CommentEntity anotherReply = new CommentEntity("comment-003", "article-001", "author-003", "Another reply");
        anotherReply.setParentId("comment-001");
        entityManager.persistAndFlush(anotherReply);
        
        // When
        List<CommentEntity> replies = commentRepository.findByParentId("comment-001");
        
        // Then
        assertThat(replies).hasSize(2);
        assertThat(replies).allMatch(comment -> comment.getParentId().equals("comment-001"));
    }
    
    @Test
    void findActiveRepliesByParentId_ShouldReturnOnlyActiveReplies() {
        // Given
        CommentEntity hiddenReply = new CommentEntity("comment-003", "article-001", "author-003", "Hidden reply");
        hiddenReply.setParentId("comment-001");
        hiddenReply.setStatus(CommentStatus.HIDDEN);
        entityManager.persistAndFlush(hiddenReply);
        
        // When
        List<CommentEntity> activeReplies = commentRepository.findActiveRepliesByParentId("comment-001");
        
        // Then
        assertThat(activeReplies).hasSize(1); // only replyComment
        assertThat(activeReplies.get(0).getStatus()).isEqualTo(CommentStatus.ACTIVE);
    }
    
    @Test
    void findRootCommentsByArticleId_ShouldReturnOnlyRootComments() {
        // Given
        CommentEntity rootComment = new CommentEntity("comment-003", "article-001", "author-003", "Root comment");
        rootComment.setStatus(CommentStatus.ACTIVE);
        entityManager.persistAndFlush(rootComment);
        
        // When
        List<CommentEntity> rootComments = commentRepository.findRootCommentsByArticleId("article-001");
        
        // Then
        assertThat(rootComments).hasSize(2); // testComment + rootComment (replyComment has parent)
        assertThat(rootComments).allMatch(comment -> comment.getParentId() == null);
    }
    
    @Test
    void findRootCommentsByArticleIdWithPagination_ShouldReturnPagedRootComments() {
        // Given
        for (int i = 3; i <= 5; i++) {
            CommentEntity rootComment = new CommentEntity("comment-00" + i, "article-001", "author-00" + i, "Root comment " + i);
            rootComment.setStatus(CommentStatus.ACTIVE);
            entityManager.persistAndFlush(rootComment);
        }
        
        // When
        Page<CommentEntity> result = commentRepository.findRootCommentsByArticleId("article-001", PageRequest.of(0, 2));
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(4); // testComment + 3 new root comments
        assertThat(result.getContent()).allMatch(comment -> comment.getParentId() == null);
    }
    
    @Test
    void countByArticleId_ShouldReturnCorrectCount() {
        // Given
        CommentEntity anotherComment = new CommentEntity("comment-003", "article-001", "author-003", "Another comment");
        entityManager.persistAndFlush(anotherComment);
        
        // When
        long count = commentRepository.countByArticleId("article-001");
        
        // Then
        assertThat(count).isEqualTo(3);
    }
    
    @Test
    void countActiveCommentsByArticleId_ShouldReturnCorrectCount() {
        // Given
        CommentEntity hiddenComment = new CommentEntity("comment-003", "article-001", "author-003", "Hidden comment");
        hiddenComment.setStatus(CommentStatus.HIDDEN);
        entityManager.persistAndFlush(hiddenComment);
        
        // When
        long count = commentRepository.countActiveCommentsByArticleId("article-001");
        
        // Then
        assertThat(count).isEqualTo(2); // testComment + replyComment
    }
    
    @Test
    void countByAuthorId_ShouldReturnCorrectCount() {
        // Given
        CommentEntity anotherCommentByAuthor = new CommentEntity("comment-003", "article-002", "author-001", "Another comment");
        entityManager.persistAndFlush(anotherCommentByAuthor);
        
        // When
        long count = commentRepository.countByAuthorId("author-001");
        
        // Then
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    void countByParentId_ShouldReturnCorrectCount() {
        // Given
        CommentEntity anotherReply = new CommentEntity("comment-003", "article-001", "author-003", "Another reply");
        anotherReply.setParentId("comment-001");
        entityManager.persistAndFlush(anotherReply);
        
        // When
        long count = commentRepository.countByParentId("comment-001");
        
        // Then
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    void searchByContent_ShouldReturnMatchingComments() {
        // Given
        CommentEntity searchableComment = new CommentEntity("comment-003", "article-001", "author-003", "This comment contains searchable keyword");
        searchableComment.setStatus(CommentStatus.ACTIVE);
        entityManager.persistAndFlush(searchableComment);
        
        // When
        Page<CommentEntity> result = commentRepository.searchByContent("searchable", PageRequest.of(0, 10));
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).containsIgnoringCase("searchable");
    }
}