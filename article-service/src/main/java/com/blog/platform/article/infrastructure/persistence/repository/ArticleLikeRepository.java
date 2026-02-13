package com.blog.platform.article.infrastructure.persistence.repository;

import com.blog.platform.article.infrastructure.persistence.entity.ArticleLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLikeEntity, Long> {
    
    Optional<ArticleLikeEntity> findByArticleIdAndUserId(String articleId, String userId);
    
    boolean existsByArticleIdAndUserId(String articleId, String userId);
    
    @Modifying
    @Query("DELETE FROM ArticleLikeEntity al WHERE al.articleId = :articleId AND al.userId = :userId")
    void deleteByArticleIdAndUserId(@Param("articleId") String articleId, @Param("userId") String userId);
    
    @Query("SELECT COUNT(al) FROM ArticleLikeEntity al WHERE al.articleId = :articleId")
    long countByArticleId(@Param("articleId") String articleId);
}
