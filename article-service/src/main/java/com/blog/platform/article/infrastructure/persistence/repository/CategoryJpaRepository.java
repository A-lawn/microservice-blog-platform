package com.blog.platform.article.infrastructure.persistence.repository;

import com.blog.platform.article.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    
    /**
     * Find category by name
     */
    Optional<CategoryEntity> findByName(String name);
    
    /**
     * Check if category name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find root categories (no parent)
     */
    List<CategoryEntity> findByParentIdIsNullOrderBySortOrder();
    
    /**
     * Find child categories by parent ID
     */
    List<CategoryEntity> findByParentIdOrderBySortOrder(Long parentId);
    
    /**
     * Find categories by parent ID with children
     */
    @Query("SELECT c FROM CategoryEntity c LEFT JOIN FETCH c.children WHERE c.parentId = :parentId ORDER BY c.sortOrder")
    List<CategoryEntity> findByParentIdWithChildren(@Param("parentId") Long parentId);
    
    /**
     * Find all categories ordered by sort order
     */
    List<CategoryEntity> findAllByOrderBySortOrder();
    
    /**
     * Find categories with article count
     */
    @Query("SELECT c, COUNT(ac) as articleCount FROM CategoryEntity c " +
           "LEFT JOIN c.articles ac " +
           "GROUP BY c.id " +
           "ORDER BY c.sortOrder")
    List<Object[]> findCategoriesWithArticleCount();
    
    /**
     * Search categories by name
     */
    @Query("SELECT c FROM CategoryEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<CategoryEntity> searchByName(@Param("name") String name);
    
    /**
     * Find category hierarchy (parent and all children)
     */
    @Query("SELECT c FROM CategoryEntity c LEFT JOIN FETCH c.children WHERE c.id = :categoryId")
    Optional<CategoryEntity> findByIdWithChildren(@Param("categoryId") Long categoryId);
    
    /**
     * Count articles in category
     */
    @Query("SELECT COUNT(ac) FROM ArticleCategoryEntity ac WHERE ac.categoryId = :categoryId")
    long countArticlesInCategory(@Param("categoryId") Long categoryId);
}