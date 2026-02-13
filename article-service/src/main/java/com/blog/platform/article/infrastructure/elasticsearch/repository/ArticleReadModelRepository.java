package com.blog.platform.article.infrastructure.elasticsearch.repository;

import com.blog.platform.article.infrastructure.elasticsearch.model.ArticleReadModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArticleReadModelRepository extends ElasticsearchRepository<ArticleReadModel, String> {
    
    /**
     * Find articles by author ID
     */
    Page<ArticleReadModel> findByAuthorId(String authorId, Pageable pageable);
    
    /**
     * Find articles by status
     */
    Page<ArticleReadModel> findByStatus(String status, Pageable pageable);
    
    /**
     * Find published articles
     */
    Page<ArticleReadModel> findByStatusOrderByPublishTimeDesc(String status, Pageable pageable);
    
    /**
     * Find articles by author and status
     */
    Page<ArticleReadModel> findByAuthorIdAndStatus(String authorId, String status, Pageable pageable);
    
    /**
     * Search articles by title
     */
    @Query("{\"match\": {\"title\": \"?0\"}}")
    Page<ArticleReadModel> searchByTitle(String keyword, Pageable pageable);
    
    /**
     * Search articles by title or content
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^2\", \"content\", \"summary\"]}}")
    Page<ArticleReadModel> searchByTitleOrContent(String keyword, Pageable pageable);
    
    /**
     * Find articles by tag
     */
    Page<ArticleReadModel> findByTagsContaining(String tag, Pageable pageable);
    
    /**
     * Find articles by category
     */
    Page<ArticleReadModel> findByCategoryNamesContaining(String categoryName, Pageable pageable);
    
    /**
     * Find popular articles by view count
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"status\": \"PUBLISHED\"}}], \"filter\": [{\"range\": {\"viewCount\": {\"gte\": ?0}}}]}}")
    Page<ArticleReadModel> findPopularArticlesByViewCount(Integer minViewCount, Pageable pageable);
    
    /**
     * Find trending articles (high engagement in recent time)
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"status\": \"PUBLISHED\"}}, {\"range\": {\"publishTime\": {\"gte\": \"?0\"}}}], \"should\": [{\"range\": {\"viewCount\": {\"boost\": 2.0}}}, {\"range\": {\"likeCount\": {\"boost\": 3.0}}}, {\"range\": {\"commentCount\": {\"boost\": 4.0}}}]}}")
    Page<ArticleReadModel> findTrendingArticles(LocalDateTime since, Pageable pageable);
    
    /**
     * Find articles published after a specific date
     */
    Page<ArticleReadModel> findByStatusAndPublishTimeAfter(String status, LocalDateTime publishTime, Pageable pageable);
    
    /**
     * Advanced search with multiple criteria
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"status\": \"PUBLISHED\"}}], \"should\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"content\", \"summary^2\"]}}, {\"terms\": {\"tags\": [\"?1\"]}}, {\"terms\": {\"categoryNames\": [\"?2\"]}}], \"minimum_should_match\": 1}}")
    Page<ArticleReadModel> advancedSearch(String keyword, List<String> tags, List<String> categories, Pageable pageable);
    
    /**
     * Find similar articles based on tags and categories
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"status\": \"PUBLISHED\"}}, {\"bool\": {\"must_not\": [{\"term\": {\"id\": \"?0\"}}]}}], \"should\": [{\"terms\": {\"tags\": ?1}}, {\"terms\": {\"categoryNames\": ?2}}], \"minimum_should_match\": 1}}")
    Page<ArticleReadModel> findSimilarArticles(String excludeId, List<String> tags, List<String> categories, Pageable pageable);
    
    /**
     * Count articles by author
     */
    long countByAuthorId(String authorId);
    
    /**
     * Count articles by status
     */
    long countByStatus(String status);
}