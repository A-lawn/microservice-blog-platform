package com.blog.platform.article.infrastructure.persistence.repository;

import com.blog.platform.article.infrastructure.persistence.entity.ArticleStatisticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleStatisticsJpaRepository extends JpaRepository<ArticleStatisticsEntity, String> {
    
    /**
     * Find statistics by article ID
     */
    Optional<ArticleStatisticsEntity> findByArticleId(String articleId);
    
    /**
     * Find articles with most views
     */
    @Query("SELECT s FROM ArticleStatisticsEntity s ORDER BY s.viewCount DESC")
    List<ArticleStatisticsEntity> findTopByViewCount();
    
    /**
     * Find articles with most likes
     */
    @Query("SELECT s FROM ArticleStatisticsEntity s ORDER BY s.likeCount DESC")
    List<ArticleStatisticsEntity> findTopByLikeCount();
    
    /**
     * Find articles with most comments
     */
    @Query("SELECT s FROM ArticleStatisticsEntity s ORDER BY s.commentCount DESC")
    List<ArticleStatisticsEntity> findTopByCommentCount();
    
    /**
     * Increment view count
     */
    @Modifying
    @Query("UPDATE ArticleStatisticsEntity s SET s.viewCount = s.viewCount + 1 WHERE s.articleId = :articleId")
    int incrementViewCount(@Param("articleId") String articleId);
    
    /**
     * Increment like count
     */
    @Modifying
    @Query("UPDATE ArticleStatisticsEntity s SET s.likeCount = s.likeCount + 1 WHERE s.articleId = :articleId")
    int incrementLikeCount(@Param("articleId") String articleId);
    
    /**
     * Decrement like count
     */
    @Modifying
    @Query("UPDATE ArticleStatisticsEntity s SET s.likeCount = CASE WHEN s.likeCount > 0 THEN s.likeCount - 1 ELSE 0 END WHERE s.articleId = :articleId")
    int decrementLikeCount(@Param("articleId") String articleId);
    
    /**
     * Increment comment count
     */
    @Modifying
    @Query("UPDATE ArticleStatisticsEntity s SET s.commentCount = s.commentCount + 1 WHERE s.articleId = :articleId")
    int incrementCommentCount(@Param("articleId") String articleId);
    
    /**
     * Decrement comment count
     */
    @Modifying
    @Query("UPDATE ArticleStatisticsEntity s SET s.commentCount = CASE WHEN s.commentCount > 0 THEN s.commentCount - 1 ELSE 0 END WHERE s.articleId = :articleId")
    int decrementCommentCount(@Param("articleId") String articleId);
    
    /**
     * Increment share count
     */
    @Modifying
    @Query("UPDATE ArticleStatisticsEntity s SET s.shareCount = s.shareCount + 1 WHERE s.articleId = :articleId")
    int incrementShareCount(@Param("articleId") String articleId);
}