package com.blog.platform.comment.infrastructure.persistence.repository;

import com.blog.platform.comment.infrastructure.persistence.entity.CommentEntity;
import com.blog.platform.common.domain.comment.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentJpaRepository extends JpaRepository<CommentEntity, String> {
    
    /**
     * Find comments by article ID
     */
    List<CommentEntity> findByArticleId(String articleId);
    
    /**
     * Find comments by article ID with pagination
     */
    Page<CommentEntity> findByArticleId(String articleId, Pageable pageable);
    
    /**
     * Find active comments by article ID
     */
    @Query("SELECT c FROM CommentEntity c WHERE c.articleId = :articleId AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    List<CommentEntity> findActiveCommentsByArticleId(@Param("articleId") String articleId);
    
    /**
     * Find active comments by article ID with pagination
     */
    @Query("SELECT c FROM CommentEntity c WHERE c.articleId = :articleId AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    Page<CommentEntity> findActiveCommentsByArticleId(@Param("articleId") String articleId, Pageable pageable);
    
    /**
     * Find comments by author ID
     */
    List<CommentEntity> findByAuthorId(String authorId);
    
    /**
     * Find comments by author ID with pagination
     */
    Page<CommentEntity> findByAuthorId(String authorId, Pageable pageable);
    
    /**
     * Find replies by parent ID
     */
    List<CommentEntity> findByParentId(String parentId);
    
    /**
     * Find active replies by parent ID
     */
    @Query("SELECT c FROM CommentEntity c WHERE c.parentId = :parentId AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    List<CommentEntity> findActiveRepliesByParentId(@Param("parentId") String parentId);
    
    /**
     * Find root comments (no parent) by article ID
     */
    @Query("SELECT c FROM CommentEntity c WHERE c.articleId = :articleId AND c.parentId IS NULL AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    List<CommentEntity> findRootCommentsByArticleId(@Param("articleId") String articleId);
    
    /**
     * Find root comments with pagination
     */
    @Query("SELECT c FROM CommentEntity c WHERE c.articleId = :articleId AND c.parentId IS NULL AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    Page<CommentEntity> findRootCommentsByArticleId(@Param("articleId") String articleId, Pageable pageable);
    
    /**
     * Find comment with statistics
     */
    @Query("SELECT c FROM CommentEntity c LEFT JOIN FETCH c.statistics WHERE c.id = :commentId")
    Optional<CommentEntity> findByIdWithStatistics(@Param("commentId") String commentId);
    
    /**
     * Find comment with replies
     */
    @Query("SELECT c FROM CommentEntity c LEFT JOIN FETCH c.replies WHERE c.id = :commentId")
    Optional<CommentEntity> findByIdWithReplies(@Param("commentId") String commentId);
    
    /**
     * Find comments by status
     */
    List<CommentEntity> findByStatus(CommentStatus status);
    
    /**
     * Find comments by status with pagination
     */
    Page<CommentEntity> findByStatus(CommentStatus status, Pageable pageable);
    
    /**
     * Find comments created after a specific date
     */
    List<CommentEntity> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Count comments by article ID
     */
    long countByArticleId(String articleId);
    
    /**
     * Count active comments by article ID
     */
    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.articleId = :articleId AND c.status = 'ACTIVE'")
    long countActiveCommentsByArticleId(@Param("articleId") String articleId);
    
    /**
     * Count comments by author ID
     */
    long countByAuthorId(String authorId);
    
    /**
     * Count replies by parent ID
     */
    long countByParentId(String parentId);
    
    /**
     * Count active replies by parent ID
     */
    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.parentId = :parentId AND c.status = 'ACTIVE'")
    long countActiveRepliesByParentId(@Param("parentId") String parentId);
    
    /**
     * Find recent comments by author
     */
    @Query("SELECT c FROM CommentEntity c WHERE c.authorId = :authorId AND c.status = 'ACTIVE' ORDER BY c.createdAt DESC")
    List<CommentEntity> findRecentCommentsByAuthor(@Param("authorId") String authorId, Pageable pageable);
    
    @Query("SELECT c FROM CommentEntity c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.status = 'ACTIVE'")
    Page<CommentEntity> searchByContent(@Param("keyword") String keyword, Pageable pageable);
    
    long countByStatus(CommentStatus status);
}