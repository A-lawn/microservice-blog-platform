package com.blog.platform.article.infrastructure.elasticsearch.service;

import com.blog.platform.article.infrastructure.elasticsearch.model.ArticleReadModel;
import com.blog.platform.article.infrastructure.elasticsearch.repository.ArticleReadModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(name = "article-service.enableSearch", havingValue = "true")
public class ArticleSearchService {
    
    @Autowired
    private ArticleReadModelRepository readModelRepository;
    
    /**
     * Search articles by keyword (title, content, summary)
     */
    public Page<ArticleReadModel> searchArticles(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return readModelRepository.findByStatusOrderByPublishTimeDesc("PUBLISHED", pageable);
        }
        return readModelRepository.searchByTitleOrContent(keyword.trim(), pageable);
    }
    
    /**
     * Get published articles with pagination
     */
    public Page<ArticleReadModel> getPublishedArticles(Pageable pageable) {
        return readModelRepository.findByStatusOrderByPublishTimeDesc("PUBLISHED", pageable);
    }
    
    /**
     * Get articles by author
     */
    public Page<ArticleReadModel> getArticlesByAuthor(String authorId, Pageable pageable) {
        return readModelRepository.findByAuthorIdAndStatus(authorId, "PUBLISHED", pageable);
    }
    
    /**
     * Get articles by tag
     */
    public Page<ArticleReadModel> getArticlesByTag(String tag, Pageable pageable) {
        return readModelRepository.findByTagsContaining(tag, pageable);
    }
    
    /**
     * Get articles by category
     */
    public Page<ArticleReadModel> getArticlesByCategory(String categoryName, Pageable pageable) {
        return readModelRepository.findByCategoryNamesContaining(categoryName, pageable);
    }
    
    /**
     * Get popular articles (high view count)
     */
    public Page<ArticleReadModel> getPopularArticles(Integer minViewCount, Pageable pageable) {
        return readModelRepository.findPopularArticlesByViewCount(minViewCount, pageable);
    }
    
    /**
     * Get trending articles (recent with high engagement)
     */
    public Page<ArticleReadModel> getTrendingArticles(int daysBack, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return readModelRepository.findTrendingArticles(since, pageable);
    }
    
    /**
     * Get recent articles
     */
    public Page<ArticleReadModel> getRecentArticles(int daysBack, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return readModelRepository.findByStatusAndPublishTimeAfter("PUBLISHED", since, pageable);
    }
    
    /**
     * Advanced search with multiple criteria
     */
    public Page<ArticleReadModel> advancedSearch(String keyword, List<String> tags, 
                                                List<String> categories, Pageable pageable) {
        return readModelRepository.advancedSearch(keyword, tags, categories, pageable);
    }
    
    /**
     * Find similar articles
     */
    public Page<ArticleReadModel> findSimilarArticles(String articleId, List<String> tags, 
                                                     List<String> categories, Pageable pageable) {
        return readModelRepository.findSimilarArticles(articleId, tags, categories, pageable);
    }
    
    /**
     * Get article statistics
     */
    public ArticleSearchStats getSearchStats() {
        long totalPublished = readModelRepository.countByStatus("PUBLISHED");
        long totalDrafts = readModelRepository.countByStatus("DRAFT");
        long totalArchived = readModelRepository.countByStatus("ARCHIVED");
        
        return new ArticleSearchStats(totalPublished, totalDrafts, totalArchived);
    }
    
    /**
     * Search statistics DTO
     */
    public static class ArticleSearchStats {
        private final long publishedCount;
        private final long draftCount;
        private final long archivedCount;
        
        public ArticleSearchStats(long publishedCount, long draftCount, long archivedCount) {
            this.publishedCount = publishedCount;
            this.draftCount = draftCount;
            this.archivedCount = archivedCount;
        }
        
        public long getPublishedCount() { return publishedCount; }
        public long getDraftCount() { return draftCount; }
        public long getArchivedCount() { return archivedCount; }
        public long getTotalCount() { return publishedCount + draftCount + archivedCount; }
    }
}