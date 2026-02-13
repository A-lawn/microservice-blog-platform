package com.blog.platform.user.infrastructure.persistence.repository;

import com.blog.platform.user.infrastructure.persistence.entity.UserStatisticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatisticsJpaRepository extends JpaRepository<UserStatisticsEntity, String> {
    
    /**
     * Find statistics by user ID
     */
    Optional<UserStatisticsEntity> findByUserId(String userId);
    
    /**
     * Find users with most articles
     */
    @Query("SELECT us FROM UserStatisticsEntity us ORDER BY us.articleCount DESC")
    List<UserStatisticsEntity> findTopByArticleCount();
    
    /**
     * Find users with most comments
     */
    @Query("SELECT us FROM UserStatisticsEntity us ORDER BY us.commentCount DESC")
    List<UserStatisticsEntity> findTopByCommentCount();
    
    /**
     * Find users with most likes
     */
    @Query("SELECT us FROM UserStatisticsEntity us ORDER BY us.likeCount DESC")
    List<UserStatisticsEntity> findTopByLikeCount();
    
    /**
     * Increment article count
     */
    @Modifying
    @Query("UPDATE UserStatisticsEntity us SET us.articleCount = us.articleCount + 1 WHERE us.userId = :userId")
    int incrementArticleCount(@Param("userId") String userId);
    
    /**
     * Decrement article count
     */
    @Modifying
    @Query("UPDATE UserStatisticsEntity us SET us.articleCount = CASE WHEN us.articleCount > 0 THEN us.articleCount - 1 ELSE 0 END WHERE us.userId = :userId")
    int decrementArticleCount(@Param("userId") String userId);
    
    /**
     * Increment comment count
     */
    @Modifying
    @Query("UPDATE UserStatisticsEntity us SET us.commentCount = us.commentCount + 1 WHERE us.userId = :userId")
    int incrementCommentCount(@Param("userId") String userId);
    
    /**
     * Decrement comment count
     */
    @Modifying
    @Query("UPDATE UserStatisticsEntity us SET us.commentCount = CASE WHEN us.commentCount > 0 THEN us.commentCount - 1 ELSE 0 END WHERE us.userId = :userId")
    int decrementCommentCount(@Param("userId") String userId);
    
    /**
     * Increment like count
     */
    @Modifying
    @Query("UPDATE UserStatisticsEntity us SET us.likeCount = us.likeCount + 1 WHERE us.userId = :userId")
    int incrementLikeCount(@Param("userId") String userId);
    
    /**
     * Decrement like count
     */
    @Modifying
    @Query("UPDATE UserStatisticsEntity us SET us.likeCount = CASE WHEN us.likeCount > 0 THEN us.likeCount - 1 ELSE 0 END WHERE us.userId = :userId")
    int decrementLikeCount(@Param("userId") String userId);
}