package com.blog.platform.article.infrastructure.elasticsearch.service;

import com.blog.platform.article.infrastructure.elasticsearch.model.ArticleReadModel;
import com.blog.platform.article.infrastructure.elasticsearch.repository.ArticleReadModelRepository;
import com.blog.platform.article.infrastructure.persistence.entity.ArticleEntity;
import com.blog.platform.article.infrastructure.persistence.entity.ArticleTagEntity;
import com.blog.platform.article.infrastructure.persistence.repository.ArticleJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "article-service.enableSearch", havingValue = "true")
public class ArticleReadModelSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleReadModelSyncService.class);
    
    @Autowired
    private ArticleReadModelRepository readModelRepository;
    
    @Autowired
    private ArticleJpaRepository articleJpaRepository;
    
    @Transactional(readOnly = true)
    public void syncArticle(String articleId) {
        try {
            Optional<ArticleEntity> articleOpt = articleJpaRepository.findByIdWithTags(articleId);
            if (articleOpt.isPresent()) {
                ArticleEntity article = articleOpt.get();
                ArticleReadModel readModel = convertToReadModel(article);
                readModelRepository.save(readModel);
                logger.info("Synced article {} to read model", articleId);
            } else {
                readModelRepository.deleteById(articleId);
                logger.info("Removed article {} from read model", articleId);
            }
        } catch (Exception e) {
            logger.error("Failed to sync article {} to read model", articleId, e);
        }
    }
    
    @Transactional(readOnly = true)
    public void syncAllArticles() {
        try {
            logger.info("Starting full article sync to read model");
            List<ArticleEntity> articles = articleJpaRepository.findAll();
            
            for (ArticleEntity article : articles) {
                try {
                    ArticleReadModel readModel = convertToReadModel(article);
                    readModelRepository.save(readModel);
                } catch (Exception e) {
                    logger.error("Failed to sync article {} during full sync", article.getId(), e);
                }
            }
            
            logger.info("Completed full article sync, processed {} articles", articles.size());
        } catch (Exception e) {
            logger.error("Failed to complete full article sync", e);
        }
    }
    
    public void removeArticle(String articleId) {
        try {
            readModelRepository.deleteById(articleId);
            logger.info("Removed article {} from read model", articleId);
        } catch (Exception e) {
            logger.error("Failed to remove article {} from read model", articleId, e);
        }
    }
    
    public void updateArticleStatistics(String articleId, Long viewCount, Long likeCount, 
                                       Long commentCount, Long shareCount) {
        try {
            Optional<ArticleReadModel> readModelOpt = readModelRepository.findById(articleId);
            if (readModelOpt.isPresent()) {
                ArticleReadModel readModel = readModelOpt.get();
                readModel.setViewCount(viewCount);
                readModel.setLikeCount(likeCount);
                readModel.setCommentCount(commentCount);
                readModel.setShareCount(shareCount);
                readModelRepository.save(readModel);
                logger.debug("Updated statistics for article {} in read model", articleId);
            }
        } catch (Exception e) {
            logger.error("Failed to update statistics for article {} in read model", articleId, e);
        }
    }
    
    private ArticleReadModel convertToReadModel(ArticleEntity article) {
        ArticleReadModel readModel = new ArticleReadModel();
        
        readModel.setId(article.getId());
        readModel.setTitle(article.getTitle());
        readModel.setSummary(article.getSummary());
        readModel.setContent(article.getContent());
        readModel.setAuthorId(article.getAuthorId());
        readModel.setPublishTime(article.getPublishTime());
        readModel.setCreatedAt(article.getCreatedAt());
        readModel.setUpdatedAt(article.getUpdatedAt());
        readModel.setStatus(article.getStatus().name());
        
        if (article.getStatistics() != null) {
            readModel.setViewCount(article.getStatistics().getViewCount());
            readModel.setLikeCount(article.getStatistics().getLikeCount());
            readModel.setCommentCount(article.getStatistics().getCommentCount());
            readModel.setShareCount(article.getStatistics().getShareCount());
            readModel.setBookmarkCount(article.getStatistics().getBookmarkCount());
        }
        
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            List<String> tags = article.getTags().stream()
                    .map(ArticleTagEntity::getTagName)
                    .collect(Collectors.toList());
            readModel.setTags(tags);
        }
        
        if (article.getCategories() != null && !article.getCategories().isEmpty()) {
            List<String> categoryNames = article.getCategories().stream()
                    .map(ac -> ac.getCategory() != null ? ac.getCategory().getName() : null)
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            readModel.setCategoryNames(categoryNames);
        }
        
        return readModel;
    }
}
