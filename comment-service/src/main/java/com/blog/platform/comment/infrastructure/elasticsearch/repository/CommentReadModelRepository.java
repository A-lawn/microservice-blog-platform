package com.blog.platform.comment.infrastructure.elasticsearch.repository;

import com.blog.platform.comment.infrastructure.elasticsearch.model.CommentReadModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentReadModelRepository extends ElasticsearchRepository<CommentReadModel, String> {
    
    /**
     * Find comments by article ID
     */
    Page<CommentReadModel> findByArticleId(String articleId, Pageable pageable);
    
    /**
     * Find active comments by article ID
     */
    Page<CommentReadModel> findByArticleIdAndStatus(String articleId, String status, Pageable pageable);
    
    /**
     * Find comments by author ID
     */
    Page<CommentReadModel> findByAuthorId(String authorId, Pageable pageable);
    
    /**
     * Find root comments (no parent) by article ID
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"articleId\": \"?0\"}}, {\"term\": {\"status\": \"?1\"}}, {\"bool\": {\"must_not\": [{\"exists\": {\"field\": \"parentId\"}}]}}]}}")
    Page<CommentReadModel> findRootCommentsByArticleIdAndStatus(String articleId, String status, Pageable pageable);
    
    /**
     * Find replies by parent ID
     */
    Page<CommentReadModel> findByParentIdAndStatus(String parentId, String status, Pageable pageable);
    
    /**
     * Find comments by hierarchy level
     */
    Page<CommentReadModel> findByArticleIdAndStatusAndLevel(String articleId, String status, Integer level, Pageable pageable);
    
    /**
     * Search comments by content
     */
    @Query("{\"bool\": {\"must\": [{\"match\": {\"content\": \"?0\"}}, {\"term\": {\"status\": \"ACTIVE\"}}]}}")
    Page<CommentReadModel> searchByContent(String keyword, Pageable pageable);
    
    /**
     * Find popular comments by like count
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"status\": \"ACTIVE\"}}, {\"range\": {\"likeCount\": {\"gte\": ?0}}}]}}")
    Page<CommentReadModel> findPopularCommentsByLikeCount(Integer minLikeCount, Pageable pageable);
    
    /**
     * Find recent comments by article
     */
    Page<CommentReadModel> findByArticleIdAndStatusOrderByCreatedAtDesc(String articleId, String status, Pageable pageable);
    
    /**
     * Find comments in a specific time range
     */
    Page<CommentReadModel> findByStatusAndCreatedAtBetween(String status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * Find comment thread (parent and all its descendants)
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"status\": \"ACTIVE\"}}, {\"bool\": {\"should\": [{\"term\": {\"id\": \"?0\"}}, {\"wildcard\": {\"path\": \"*?0*\"}}]}}]}}")
    List<CommentReadModel> findCommentThread(String commentId);
    
    /**
     * Find comments by path prefix (for hierarchical queries)
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"status\": \"ACTIVE\"}}, {\"prefix\": {\"path\": \"?0\"}}]}}")
    Page<CommentReadModel> findByPathPrefix(String pathPrefix, Pageable pageable);
    
    /**
     * Count comments by article
     */
    long countByArticleIdAndStatus(String articleId, String status);
    
    /**
     * Count replies by parent
     */
    long countByParentIdAndStatus(String parentId, String status);
    
    /**
     * Count comments by author
     */
    long countByAuthorIdAndStatus(String authorId, String status);
    
    /**
     * Find most replied comments
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"status\": \"ACTIVE\"}}, {\"range\": {\"replyCount\": {\"gte\": ?0}}}]}}")
    Page<CommentReadModel> findMostRepliedComments(Integer minReplyCount, Pageable pageable);
}