package com.blog.platform.article.infrastructure.persistence.repository;

import com.blog.platform.article.domain.repository.ArticleRepository;
import com.blog.platform.article.infrastructure.persistence.entity.ArticleEntity;
import com.blog.platform.article.infrastructure.persistence.entity.ArticleStatisticsEntity;
import com.blog.platform.common.domain.article.*;
import com.blog.platform.common.domain.user.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ArticleRepositoryImpl implements ArticleRepository {
    
    @Autowired
    private ArticleJpaRepository jpaRepository;
    
    @Autowired
    private ArticleStatisticsJpaRepository statisticsJpaRepository;
    
    @Override
    public Article save(Article article) {
        ArticleEntity entity = toEntity(article);
        
        if (entity.getStatistics() == null) {
            ArticleStatisticsEntity statisticsEntity = new ArticleStatisticsEntity();
            statisticsEntity.setArticleId(entity.getId());
            entity.setStatistics(statisticsEntity);
        }
        
        ArticleEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    public Optional<Article> findById(ArticleId articleId) {
        Optional<ArticleEntity> entityOpt = jpaRepository.findByIdWithStatistics(articleId.getValue());
        return entityOpt.map(this::toDomain);
    }
    
    @Override
    public List<Article> findByAuthorId(UserId authorId) {
        List<ArticleEntity> entities = jpaRepository.findByAuthorId(authorId.getValue());
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<Article> findByAuthorId(UserId authorId, Pageable pageable) {
        Page<ArticleEntity> entityPage = jpaRepository.findByAuthorId(authorId.getValue(), pageable);
        List<Article> articles = entityPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(articles, pageable, entityPage.getTotalElements());
    }
    
    @Override
    public Page<Article> findByStatus(ArticleStatus status, Pageable pageable) {
        Page<ArticleEntity> entityPage = jpaRepository.findByStatus(status, pageable);
        List<Article> articles = entityPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(articles, pageable, entityPage.getTotalElements());
    }
    
    @Override
    public Page<Article> findByAuthorIdAndStatus(UserId authorId, ArticleStatus status, Pageable pageable) {
        Page<ArticleEntity> entityPage = jpaRepository.findByAuthorIdAndStatus(
                authorId.getValue(), status, pageable);
        List<Article> articles = entityPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(articles, pageable, entityPage.getTotalElements());
    }
    
    @Override
    public Page<Article> findPublishedArticles(Pageable pageable) {
        Page<ArticleEntity> entityPage = jpaRepository.findPublishedArticles(pageable);
        List<Article> articles = entityPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(articles, pageable, entityPage.getTotalElements());
    }
    
    @Override
    public Page<Article> findPublishedArticlesByAuthor(UserId authorId, Pageable pageable) {
        Page<ArticleEntity> entityPage = jpaRepository.findPublishedArticlesByAuthor(
                authorId.getValue(), pageable);
        List<Article> articles = entityPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(articles, pageable, entityPage.getTotalElements());
    }
    
    @Override
    public void delete(Article article) {
        jpaRepository.deleteById(article.getId().getValue());
    }
    
    @Override
    public void deleteById(ArticleId articleId) {
        jpaRepository.deleteById(articleId.getValue());
    }
    
    @Override
    public boolean existsById(ArticleId articleId) {
        return jpaRepository.existsById(articleId.getValue());
    }
    
    @Override
    public long countByAuthorId(UserId authorId) {
        return jpaRepository.countByAuthorId(authorId.getValue());
    }
    
    @Override
    public long countPublishedArticlesByAuthor(UserId authorId) {
        return jpaRepository.countPublishedArticlesByAuthor(authorId.getValue());
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public List<Article> findAll() {
        List<ArticleEntity> entities = jpaRepository.findAll();
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    private ArticleEntity toEntity(Article article) {
        ArticleEntity entity = new ArticleEntity();
        entity.setId(article.getId().getValue());
        entity.setAuthorId(article.getAuthorId().getValue());
        entity.setTitle(article.getTitle().getValue());
        entity.setContent(article.getContent().getValue());
        entity.setStatus(article.getStatus());
        entity.setPublishTime(article.getPublishTime());
        entity.setCreatedAt(article.getCreatedAt());
        entity.setUpdatedAt(article.getUpdatedAt());
        
        if (article.getContent().getValue().length() > 200) {
            entity.setSummary(article.getContent().getValue().substring(0, 200) + "...");
        } else {
            entity.setSummary(article.getContent().getValue());
        }
        
        return entity;
    }
    
    private ArticleStatisticsEntity toStatisticsEntity(Article article) {
        ArticleStatisticsEntity entity = new ArticleStatisticsEntity();
        entity.setViewCount(article.getStatistics().getViewCount());
        entity.setLikeCount(article.getStatistics().getLikeCount());
        entity.setCommentCount(article.getStatistics().getCommentCount());
        entity.setShareCount(article.getStatistics().getShareCount());
        entity.setBookmarkCount(article.getStatistics().getBookmarkCount());
        return entity;
    }
    
    private Article toDomain(ArticleEntity entity) {
        ArticleStatistics statistics = ArticleStatistics.empty();
        if (entity.getStatistics() != null) {
            statistics = ArticleStatistics.of(
                    entity.getStatistics().getViewCount(),
                    entity.getStatistics().getLikeCount(),
                    entity.getStatistics().getCommentCount(),
                    entity.getStatistics().getShareCount(),
                    entity.getStatistics().getBookmarkCount()
            );
        }
        
        return Article.reconstruct(
                ArticleId.of(entity.getId()),
                UserId.of(entity.getAuthorId()),
                ArticleTitle.of(entity.getTitle()),
                ArticleContent.of(entity.getContent()),
                entity.getStatus(),
                entity.getPublishTime(),
                statistics,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
