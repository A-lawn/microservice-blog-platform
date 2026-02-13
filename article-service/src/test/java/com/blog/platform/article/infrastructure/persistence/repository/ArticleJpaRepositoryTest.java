package com.blog.platform.article.infrastructure.persistence.repository;

import com.blog.platform.article.infrastructure.persistence.entity.ArticleEntity;
import com.blog.platform.common.domain.article.ArticleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ArticleJpaRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ArticleJpaRepository articleRepository;
    
    private ArticleEntity testArticle;
    
    @BeforeEach
    void setUp() {
        testArticle = new ArticleEntity("article-001", "author-001", "Test Article", "This is test content");
        testArticle.setSummary("Test summary");
        testArticle.setStatus(ArticleStatus.PUBLISHED);
        testArticle.setPublishTime(LocalDateTime.now());
        entityManager.persistAndFlush(testArticle);
    }
    
    @Test
    void findByAuthorId_ShouldReturnArticlesByAuthor() {
        // Given
        ArticleEntity anotherArticle = new ArticleEntity("article-002", "author-001", "Another Article", "Another content");
        entityManager.persistAndFlush(anotherArticle);
        
        // When
        List<ArticleEntity> articles = articleRepository.findByAuthorId("author-001");
        
        // Then
        assertThat(articles).hasSize(2);
        assertThat(articles).allMatch(article -> article.getAuthorId().equals("author-001"));
    }
    
    @Test
    void findByAuthorIdWithPagination_ShouldReturnPagedResults() {
        // Given
        for (int i = 2; i <= 5; i++) {
            ArticleEntity article = new ArticleEntity("article-00" + i, "author-001", "Article " + i, "Content " + i);
            entityManager.persistAndFlush(article);
        }
        
        // When
        Page<ArticleEntity> result = articleRepository.findByAuthorId("author-001", PageRequest.of(0, 2));
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }
    
    @Test
    void findByStatus_ShouldReturnArticlesByStatus() {
        // Given
        ArticleEntity draftArticle = new ArticleEntity("article-002", "author-002", "Draft Article", "Draft content");
        draftArticle.setStatus(ArticleStatus.DRAFT);
        entityManager.persistAndFlush(draftArticle);
        
        // When
        List<ArticleEntity> publishedArticles = articleRepository.findByStatus(ArticleStatus.PUBLISHED);
        List<ArticleEntity> draftArticles = articleRepository.findByStatus(ArticleStatus.DRAFT);
        
        // Then
        assertThat(publishedArticles).hasSize(1);
        assertThat(publishedArticles.get(0).getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(draftArticles).hasSize(1);
        assertThat(draftArticles.get(0).getStatus()).isEqualTo(ArticleStatus.DRAFT);
    }
    
    @Test
    void findPublishedArticles_ShouldReturnOnlyPublishedArticles() {
        // Given
        ArticleEntity draftArticle = new ArticleEntity("article-002", "author-002", "Draft Article", "Draft content");
        draftArticle.setStatus(ArticleStatus.DRAFT);
        entityManager.persistAndFlush(draftArticle);
        
        ArticleEntity archivedArticle = new ArticleEntity("article-003", "author-003", "Archived Article", "Archived content");
        archivedArticle.setStatus(ArticleStatus.ARCHIVED);
        entityManager.persistAndFlush(archivedArticle);
        
        // When
        Page<ArticleEntity> result = articleRepository.findPublishedArticles(PageRequest.of(0, 10));
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
    }
    
    @Test
    void findPublishedArticlesByAuthor_ShouldReturnAuthorPublishedArticles() {
        // Given
        ArticleEntity publishedByAuthor = new ArticleEntity("article-002", "author-001", "Published by Author", "Content");
        publishedByAuthor.setStatus(ArticleStatus.PUBLISHED);
        publishedByAuthor.setPublishTime(LocalDateTime.now());
        entityManager.persistAndFlush(publishedByAuthor);
        
        ArticleEntity draftByAuthor = new ArticleEntity("article-003", "author-001", "Draft by Author", "Content");
        draftByAuthor.setStatus(ArticleStatus.DRAFT);
        entityManager.persistAndFlush(draftByAuthor);
        
        // When
        Page<ArticleEntity> result = articleRepository.findPublishedArticlesByAuthor("author-001", PageRequest.of(0, 10));
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(article -> 
            article.getAuthorId().equals("author-001") && article.getStatus() == ArticleStatus.PUBLISHED);
    }
    
    @Test
    void searchByTitle_ShouldReturnMatchingArticles() {
        // Given
        ArticleEntity searchableArticle = new ArticleEntity("article-002", "author-002", "Searchable Title", "Content");
        searchableArticle.setStatus(ArticleStatus.PUBLISHED);
        entityManager.persistAndFlush(searchableArticle);
        
        // When
        Page<ArticleEntity> result = articleRepository.searchByTitle("searchable", PageRequest.of(0, 10));
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).containsIgnoringCase("searchable");
    }
    
    @Test
    void searchByTitleOrContent_ShouldReturnMatchingArticles() {
        // Given
        ArticleEntity titleMatch = new ArticleEntity("article-002", "author-002", "Keyword in Title", "Some content");
        titleMatch.setStatus(ArticleStatus.PUBLISHED);
        entityManager.persistAndFlush(titleMatch);
        
        ArticleEntity contentMatch = new ArticleEntity("article-003", "author-003", "Some Title", "Content with keyword");
        contentMatch.setStatus(ArticleStatus.PUBLISHED);
        entityManager.persistAndFlush(contentMatch);
        
        // When
        Page<ArticleEntity> result = articleRepository.searchByTitleOrContent("keyword", PageRequest.of(0, 10));
        
        // Then
        assertThat(result.getContent()).hasSize(2);
    }
    
    @Test
    void countByAuthorId_ShouldReturnCorrectCount() {
        // Given
        ArticleEntity anotherArticle = new ArticleEntity("article-002", "author-001", "Another Article", "Content");
        entityManager.persistAndFlush(anotherArticle);
        
        // When
        long count = articleRepository.countByAuthorId("author-001");
        
        // Then
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // Given
        ArticleEntity draftArticle = new ArticleEntity("article-002", "author-002", "Draft Article", "Content");
        draftArticle.setStatus(ArticleStatus.DRAFT);
        entityManager.persistAndFlush(draftArticle);
        
        // When
        long publishedCount = articleRepository.countByStatus(ArticleStatus.PUBLISHED);
        long draftCount = articleRepository.countByStatus(ArticleStatus.DRAFT);
        
        // Then
        assertThat(publishedCount).isEqualTo(1);
        assertThat(draftCount).isEqualTo(1);
    }
    
    @Test
    void countPublishedArticlesByAuthor_ShouldReturnCorrectCount() {
        // Given
        ArticleEntity publishedByAuthor = new ArticleEntity("article-002", "author-001", "Published", "Content");
        publishedByAuthor.setStatus(ArticleStatus.PUBLISHED);
        entityManager.persistAndFlush(publishedByAuthor);
        
        ArticleEntity draftByAuthor = new ArticleEntity("article-003", "author-001", "Draft", "Content");
        draftByAuthor.setStatus(ArticleStatus.DRAFT);
        entityManager.persistAndFlush(draftByAuthor);
        
        // When
        long count = articleRepository.countPublishedArticlesByAuthor("author-001");
        
        // Then
        assertThat(count).isEqualTo(2); // testArticle + publishedByAuthor
    }
}