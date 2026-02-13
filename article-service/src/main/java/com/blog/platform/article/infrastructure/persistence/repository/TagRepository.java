package com.blog.platform.article.infrastructure.persistence.repository;

import com.blog.platform.article.infrastructure.persistence.entity.TagEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {
    
    Optional<TagEntity> findByName(String name);
    
    Optional<TagEntity> findBySlug(String slug);
    
    boolean existsByName(String name);
    
    @Query("SELECT t FROM TagEntity t ORDER BY t.articleCount DESC, t.name ASC")
    List<TagEntity> findAllOrderByArticleCountDesc();
    
    @Query("SELECT t FROM TagEntity t ORDER BY t.articleCount DESC, t.name ASC")
    Page<TagEntity> findAllOrderByArticleCountDesc(Pageable pageable);
    
    @Query("SELECT t FROM TagEntity t WHERE t.articleCount > 0 ORDER BY t.articleCount DESC, t.name ASC")
    List<TagEntity> findPopularTags(@Param("limit") int limit);
    
    @Query("SELECT t FROM TagEntity t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY t.articleCount DESC")
    List<TagEntity> searchByName(@Param("keyword") String keyword);
}
