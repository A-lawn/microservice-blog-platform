package com.blog.platform.article.infrastructure.persistence.repository;

import com.blog.platform.article.infrastructure.persistence.entity.ArticleBookmarkEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleBookmarkRepository extends JpaRepository<ArticleBookmarkEntity, Long> {
    
    Optional<ArticleBookmarkEntity> findByArticleIdAndUserId(String articleId, String userId);
    
    boolean existsByArticleIdAndUserId(String articleId, String userId);
    
    @Modifying
    @Query("DELETE FROM ArticleBookmarkEntity ab WHERE ab.articleId = :articleId AND ab.userId = :userId")
    void deleteByArticleIdAndUserId(@Param("articleId") String articleId, @Param("userId") String userId);
    
    @Query("SELECT ab FROM ArticleBookmarkEntity ab WHERE ab.userId = :userId ORDER BY ab.createdAt DESC")
    Page<ArticleBookmarkEntity> findByUserId(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT COUNT(ab) FROM ArticleBookmarkEntity ab WHERE ab.articleId = :articleId")
    long countByArticleId(@Param("articleId") String articleId);
    
    @Query("SELECT COUNT(ab) FROM ArticleBookmarkEntity ab WHERE ab.userId = :userId")
    long countByUserId(@Param("userId") String userId);
}
