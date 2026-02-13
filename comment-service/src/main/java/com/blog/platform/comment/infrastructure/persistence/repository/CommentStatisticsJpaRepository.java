package com.blog.platform.comment.infrastructure.persistence.repository;

import com.blog.platform.comment.infrastructure.persistence.entity.CommentStatisticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentStatisticsJpaRepository extends JpaRepository<CommentStatisticsEntity, String> {
    
    /**
     * Find statistics by comment ID
     */
    Optional<CommentStatisticsEntity> findByCommentId(String commentId);
    
    /**
     * Find comments with most likes
     */
    @Query("SELECT s FROM CommentStatisticsEntity s ORDER BY s.likeCount DESC")
    List<CommentStatisticsEntity> findTopByLikeCount();
    
    /**
     * Find comments with most replies
     */
    @Query("SELECT s FROM CommentStatisticsEntity s ORDER BY s.replyCount DESC")
    List<CommentStatisticsEntity> findTopByReplyCount();
    
    /**
     * Increment like count
     */
    @Modifying
    @Query("UPDATE CommentStatisticsEntity s SET s.likeCount = s.likeCount + 1 WHERE s.commentId = :commentId")
    int incrementLikeCount(@Param("commentId") String commentId);
    
    /**
     * Decrement like count
     */
    @Modifying
    @Query("UPDATE CommentStatisticsEntity s SET s.likeCount = CASE WHEN s.likeCount > 0 THEN s.likeCount - 1 ELSE 0 END WHERE s.commentId = :commentId")
    int decrementLikeCount(@Param("commentId") String commentId);
    
    /**
     * Increment reply count
     */
    @Modifying
    @Query("UPDATE CommentStatisticsEntity s SET s.replyCount = s.replyCount + 1 WHERE s.commentId = :commentId")
    int incrementReplyCount(@Param("commentId") String commentId);
    
    /**
     * Decrement reply count
     */
    @Modifying
    @Query("UPDATE CommentStatisticsEntity s SET s.replyCount = CASE WHEN s.replyCount > 0 THEN s.replyCount - 1 ELSE 0 END WHERE s.commentId = :commentId")
    int decrementReplyCount(@Param("commentId") String commentId);
}