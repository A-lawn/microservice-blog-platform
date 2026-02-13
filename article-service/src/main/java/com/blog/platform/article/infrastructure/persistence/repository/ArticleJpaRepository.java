package com.blog.platform.article.infrastructure.persistence.repository;

import com.blog.platform.article.infrastructure.persistence.entity.ArticleEntity;
import com.blog.platform.common.domain.article.ArticleStatus;
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
public interface ArticleJpaRepository extends JpaRepository<ArticleEntity, String> {
    
    /**
     * Find articles by author ID
     */
    List<ArticleEntity> findByAuthorId(String authorId);
    
    /**
     * Find articles by author ID with pagination
     */
    Page<ArticleEntity> findByAuthorId(String authorId, Pageable pageable);
    
    /**
     * Find articles by status
     */
    List<ArticleEntity> findByStatus(ArticleStatus status);
    
    /**
     * Find articles by status with pagination
     */
    Page<ArticleEntity> findByStatus(ArticleStatus status, Pageable pageable);
    
    /**
     * Find articles by author ID and status
     */
    Page<ArticleEntity> findByAuthorIdAndStatus(String authorId, ArticleStatus status, Pageable pageable);
    
    /**
     * Find published articles
     */
    @Query("SELECT a FROM ArticleEntity a WHERE a.status = 'PUBLISHED' ORDER BY a.publishTime DESC")
    Page<ArticleEntity> findPublishedArticles(Pageable pageable);
    
    /**
     * Find published articles by author
     */
    @Query("SELECT a FROM ArticleEntity a WHERE a.authorId = :authorId AND a.status = 'PUBLISHED' ORDER BY a.publishTime DESC")
    Page<ArticleEntity> findPublishedArticlesByAuthor(@Param("authorId") String authorId, Pageable pageable);
    
    /**
     * Find articles published after a specific date
     */
    @Query("SELECT a FROM ArticleEntity a WHERE a.status = 'PUBLISHED' AND a.publishTime >= :date ORDER BY a.publishTime DESC")
    List<ArticleEntity> findPublishedArticlesAfter(@Param("date") LocalDateTime date);
    
    /**
     * Find article with statistics
     */
    @Query("SELECT a FROM ArticleEntity a LEFT JOIN FETCH a.statistics WHERE a.id = :articleId")
    Optional<ArticleEntity> findByIdWithStatistics(@Param("articleId") String articleId);
    
    /**
     * Find article with tags
     */
    @Query("SELECT a FROM ArticleEntity a LEFT JOIN FETCH a.tags WHERE a.id = :articleId")
    Optional<ArticleEntity> findByIdWithTags(@Param("articleId") String articleId);
    
    /**
     * Find article with categories
     */
    @Query("SELECT a FROM ArticleEntity a LEFT JOIN FETCH a.categories WHERE a.id = :articleId")
    Optional<ArticleEntity> findByIdWithCategories(@Param("articleId") String articleId);
    
    /**
     * Search articles by title
     */
    @Query("SELECT a FROM ArticleEntity a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND a.status = 'PUBLISHED'")
    Page<ArticleEntity> searchByTitle(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Search articles by title or content
     */
    @Query("SELECT a FROM ArticleEntity a WHERE " +
           "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "a.status = 'PUBLISHED'")
    Page<ArticleEntity> searchByTitleOrContent(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Count articles by author
     */
    long countByAuthorId(String authorId);
    
    /**
     * Count articles by status
     */
    long countByStatus(ArticleStatus status);
    
    /**
     * Count published articles by author
     */
    @Query("SELECT COUNT(a) FROM ArticleEntity a WHERE a.authorId = :authorId AND a.status = 'PUBLISHED'")
    long countPublishedArticlesByAuthor(@Param("authorId") String authorId);
    
    /**
     * Find articles by tag name
     */
    @Query("SELECT a FROM ArticleEntity a JOIN a.tags t WHERE t.tagName = :tagName AND a.status = 'PUBLISHED'")
    Page<ArticleEntity> findByTagName(@Param("tagName") String tagName, Pageable pageable);
    
    /**
     * Find articles by category ID
     */
    @Query("SELECT a FROM ArticleEntity a JOIN a.categories ac WHERE ac.categoryId = :categoryId AND a.status = 'PUBLISHED'")
    Page<ArticleEntity> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
}