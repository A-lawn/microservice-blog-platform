package com.blog.platform.article.application.service;

import com.blog.platform.article.application.dto.*;
import com.blog.platform.article.application.saga.ArticlePublishSaga;
import com.blog.platform.article.domain.exception.ArticleDomainException;
import com.blog.platform.article.domain.repository.ArticleRepository;
import com.blog.platform.article.infrastructure.elasticsearch.model.ArticleReadModel;
import com.blog.platform.article.infrastructure.elasticsearch.service.ArticleReadModelSyncService;
import com.blog.platform.article.infrastructure.elasticsearch.service.ArticleSearchService;
import com.blog.platform.article.infrastructure.messaging.ArticleEventPublisher;
import com.blog.platform.article.infrastructure.persistence.entity.ArticleBookmarkEntity;
import com.blog.platform.article.infrastructure.persistence.entity.ArticleLikeEntity;
import com.blog.platform.article.infrastructure.persistence.repository.ArticleBookmarkRepository;
import com.blog.platform.article.infrastructure.persistence.repository.ArticleLikeRepository;
import com.blog.platform.article.infrastructure.persistence.repository.ArticleJpaRepository;
import com.blog.platform.common.domain.DomainEventPublisher;
import com.blog.platform.common.domain.article.*;
import com.blog.platform.common.domain.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArticleApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleApplicationService.class);
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired(required = false)
    private ArticleSearchService searchService;
    
    @Autowired(required = false)
    private ArticleReadModelSyncService readModelSyncService;
    
    @Autowired
    private DomainEventPublisher eventPublisher;
    
    @Autowired(required = false)
    private Optional<ArticlePublishSaga> articlePublishSaga;
    
    @Autowired
    private ArticleEventPublisher articleEventPublisher;
    
    @Autowired
    private ArticleLikeRepository articleLikeRepository;
    
    @Autowired
    private ArticleBookmarkRepository articleBookmarkRepository;
    
    @Autowired
    private ArticleJpaRepository articleJpaRepository;
    
    @Value("${feature.cqrs.enabled:false}")
    private boolean cqrsEnabled;
    
    @Autowired
    private com.blog.platform.common.cache.CacheProtectionService cacheProtectionService;
    
    public ArticleDto createArticle(String authorId, CreateArticleRequest request) {
        if (authorId == null || authorId.trim().isEmpty()) {
            throw new ArticleDomainException("INVALID_AUTHOR", "作者ID不能为空");
        }
        
        try {
            UserId userId = UserId.of(authorId);
            ArticleTitle title = ArticleTitle.of(request.getTitle());
            ArticleContent content = ArticleContent.of(request.getContent());
            
            Article article = Article.create(userId, title, content);
            Article savedArticle = articleRepository.save(article);
            
            eventPublisher.publishEvents(savedArticle);
            savedArticle.clearDomainEvents();
            
            ArticleCreatedEvent event = new ArticleCreatedEvent(
                savedArticle.getId().getValue(),
                savedArticle.getAuthorId().getValue(),
                savedArticle.getTitle().getValue()
            );
            articleEventPublisher.publishArticleCreatedEvent(event);
            
            if (cqrsEnabled && readModelSyncService != null) {
                readModelSyncService.syncArticle(savedArticle.getId().getValue());
            }
            
            return toDto(savedArticle);
        } catch (IllegalArgumentException e) {
            throw new ArticleDomainException("INVALID_ARTICLE_DATA", e.getMessage());
        }
    }
    
    public ArticleDto updateArticle(String articleId, String authorId, UpdateArticleRequest request) {
        Article article = findArticleById(articleId);
        
        if (authorId != null && !article.isAuthor(UserId.of(authorId))) {
            throw new ArticleDomainException("UNAUTHORIZED", "只有作者可以编辑文章");
        }
        
        try {
            if (request.getTitle() != null && !request.getTitle().equals(article.getTitle().getValue())) {
                article.updateTitle(ArticleTitle.of(request.getTitle()));
            }
            
            if (request.getContent() != null && !request.getContent().equals(article.getContent().getValue())) {
                article.updateContent(ArticleContent.of(request.getContent()));
            }
            
            Article savedArticle = articleRepository.save(article);
            
            eventPublisher.publishEvents(savedArticle);
            savedArticle.clearDomainEvents();
            
            ArticleUpdatedEvent event = new ArticleUpdatedEvent(
                savedArticle.getId().getValue(),
                savedArticle.getAuthorId().getValue(),
                savedArticle.getTitle().getValue(),
                "CONTENT"
            );
            articleEventPublisher.publishArticleUpdatedEvent(event);
            
            if (cqrsEnabled && readModelSyncService != null) {
                readModelSyncService.syncArticle(savedArticle.getId().getValue());
            }
            
            return toDto(savedArticle);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ArticleDomainException("UPDATE_FAILED", e.getMessage());
        }
    }
    
    public void publishArticle(String articleId, String authorId) {
        Article article = findArticleById(articleId);
        
        if (authorId != null && !article.isAuthor(UserId.of(authorId))) {
            throw new ArticleDomainException("UNAUTHORIZED", "只有作者可以发布文章");
        }
        if (article.getStatus() != ArticleStatus.DRAFT) {
            throw new ArticleDomainException("INVALID_STATUS", "只有草稿状态的文章可以发布");
        }
        
        if (articlePublishSaga != null && articlePublishSaga.isPresent()) {
            try {
                articlePublishSaga.get().publishArticle(articleId, authorId);
                ArticlePublishedEvent event = new ArticlePublishedEvent(
                    articleId, authorId, article.getTitle().getValue(), LocalDateTime.now());
                articleEventPublisher.publishArticlePublishedEvent(event);
                if (cqrsEnabled && readModelSyncService != null) {
                    readModelSyncService.syncArticle(articleId);
                }
            } catch (Exception e) {
                throw new ArticleDomainException("PUBLISH_FAILED", "文章发布失败: " + e.getMessage());
            }
        } else {
            article.publish();
            articleRepository.save(article);
            eventPublisher.publishEvents(article);
            article.clearDomainEvents();
            ArticlePublishedEvent event = new ArticlePublishedEvent(
                articleId, authorId, article.getTitle().getValue(), LocalDateTime.now());
            articleEventPublisher.publishArticlePublishedEvent(event);
            if (cqrsEnabled && readModelSyncService != null) {
                readModelSyncService.syncArticle(articleId);
            }
        }
    }
    
    public void archiveArticle(String articleId, String authorId) {
        Article article = findArticleById(articleId);
        
        if (authorId != null && !article.isAuthor(UserId.of(authorId))) {
            throw new ArticleDomainException("UNAUTHORIZED", "只有作者可以归档文章");
        }
        
        try {
            article.archive();
            Article savedArticle = articleRepository.save(article);
            
            eventPublisher.publishEvents(savedArticle);
            savedArticle.clearDomainEvents();
            
            ArticleArchivedEvent event = new ArticleArchivedEvent(
                savedArticle.getId().getValue(),
                savedArticle.getAuthorId().getValue(),
                savedArticle.getTitle().getValue()
            );
            articleEventPublisher.publishArticleArchivedEvent(event);
            
            if (cqrsEnabled && readModelSyncService != null) {
                readModelSyncService.syncArticle(savedArticle.getId().getValue());
            }
        } catch (IllegalStateException e) {
            throw new ArticleDomainException("ARCHIVE_FAILED", e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public ArticleDto getArticleDetail(String articleId) {
        String cacheKey = "article:detail:" + articleId;
        
        ArticleDto cachedArticle = cacheProtectionService.getWithAsyncRefresh(
            cacheKey,
            ArticleDto.class,
            () -> {
                Article article = findArticleById(articleId);
                return toDto(article);
            },
            Duration.ofMinutes(30)
        );
        
        if (cachedArticle != null && "PUBLISHED".equals(cachedArticle.getStatus())) {
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    Article article = findArticleById(articleId);
                    if (article.getStatus().isVisible()) {
                        article.incrementViewCount();
                        articleRepository.save(article);
                        if (cqrsEnabled && readModelSyncService != null) {
                            readModelSyncService.syncArticle(article.getId().getValue());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("异步更新文章浏览量失败: {}", articleId, e);
                }
            });
        }
        
        return cachedArticle;
    }
    
    @Transactional(readOnly = true)
    public PageResult<ArticleListDto> getArticles(int page, int size, String status, String authorId, 
                                                    Long categoryId, String tag, String sort) {
        Sort sortObj = Sort.by("createdAt").descending();
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                sortObj = "desc".equalsIgnoreCase(sortParts[1]) 
                    ? Sort.by(sortParts[0]).descending() 
                    : Sort.by(sortParts[0]).ascending();
            }
        }
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<Article> articlePage;
        
        if (categoryId != null) {
            articlePage = articleRepository.findPublishedArticles(pageable);
        } else if (authorId != null && !authorId.trim().isEmpty()) {
            UserId userId = UserId.of(authorId);
            if (status != null && !status.trim().isEmpty()) {
                ArticleStatus articleStatus = ArticleStatus.valueOf(status.toUpperCase());
                articlePage = articleRepository.findByAuthorIdAndStatus(userId, articleStatus, pageable);
            } else {
                articlePage = articleRepository.findByAuthorId(userId, pageable);
            }
        } else if (status != null && !status.trim().isEmpty()) {
            ArticleStatus articleStatus = ArticleStatus.valueOf(status.toUpperCase());
            articlePage = articleRepository.findByStatus(articleStatus, pageable);
        } else {
            articlePage = articleRepository.findPublishedArticles(pageable);
        }
        
        List<ArticleListDto> articles = articlePage.getContent().stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
        
        return new PageResult<>(
                articles,
                articlePage.getNumber(),
                articlePage.getSize(),
                articlePage.getTotalElements(),
                articlePage.getTotalPages(),
                articlePage.isFirst(),
                articlePage.isLast(),
                articlePage.hasNext(),
                articlePage.hasPrevious()
        );
    }
    
    @Transactional(readOnly = true)
    public PageResult<ArticleListDto> searchArticles(String keyword, int page, int size) {
        if (searchService == null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Article> articlePage = articleRepository.findPublishedArticles(pageable);
            List<ArticleListDto> articles = articlePage.getContent().stream()
                    .map(this::toListDto)
                    .collect(Collectors.toList());
            return new PageResult<>(articles, articlePage.getNumber(), articlePage.getSize(),
                    articlePage.getTotalElements(), articlePage.getTotalPages(),
                    articlePage.isFirst(), articlePage.isLast(), articlePage.hasNext(), articlePage.hasPrevious());
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ArticleReadModel> searchResults = searchService.searchArticles(keyword, pageable);
        
        List<ArticleListDto> articles = searchResults.getContent().stream()
                .map(this::fromReadModel)
                .collect(Collectors.toList());
        
        return new PageResult<>(
                articles,
                searchResults.getNumber(),
                searchResults.getSize(),
                searchResults.getTotalElements(),
                searchResults.getTotalPages(),
                searchResults.isFirst(),
                searchResults.isLast(),
                searchResults.hasNext(),
                searchResults.hasPrevious()
        );
    }
    
    @Transactional(readOnly = true)
    public PageResult<ArticleListDto> getPopularArticles(int page, int size, Integer minViewCount) {
        if (searchService == null) {
            return getArticles(page, size, "PUBLISHED", null, null, null, "viewCount,desc");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ArticleReadModel> searchResults = searchService.getPopularArticles(
                minViewCount != null ? minViewCount : 100, pageable);
        
        List<ArticleListDto> articles = searchResults.getContent().stream()
                .map(this::fromReadModel)
                .collect(Collectors.toList());
        
        return new PageResult<>(
                articles,
                searchResults.getNumber(),
                searchResults.getSize(),
                searchResults.getTotalElements(),
                searchResults.getTotalPages(),
                searchResults.isFirst(),
                searchResults.isLast(),
                searchResults.hasNext(),
                searchResults.hasPrevious()
        );
    }
    
    @Transactional(readOnly = true)
    public PageResult<ArticleListDto> getTrendingArticles(int page, int size, Integer daysBack) {
        if (searchService == null) {
            return getArticles(page, size, "PUBLISHED", null, null, null, "publishTime,desc");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ArticleReadModel> searchResults = searchService.getTrendingArticles(
                daysBack != null ? daysBack : 7, pageable);
        
        List<ArticleListDto> articles = searchResults.getContent().stream()
                .map(this::fromReadModel)
                .collect(Collectors.toList());
        
        return new PageResult<>(
                articles,
                searchResults.getNumber(),
                searchResults.getSize(),
                searchResults.getTotalElements(),
                searchResults.getTotalPages(),
                searchResults.isFirst(),
                searchResults.isLast(),
                searchResults.hasNext(),
                searchResults.hasPrevious()
        );
    }
    
    public void deleteArticle(String articleId, String authorId) {
        Article article = findArticleById(articleId);
        
        if (authorId != null && !article.isAuthor(UserId.of(authorId))) {
            throw new ArticleDomainException("UNAUTHORIZED", "只有作者可以删除文章");
        }
        
        articleRepository.deleteById(ArticleId.of(articleId));
        if (cqrsEnabled && readModelSyncService != null) {
            readModelSyncService.syncArticle(articleId);
        }
    }
    
    public void likeArticle(String articleId, String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ArticleDomainException("UNAUTHORIZED", "请先登录");
        }
        
        Article article = findArticleById(articleId);
        
        if (articleLikeRepository.existsByArticleIdAndUserId(articleId, userId)) {
            logger.info("用户已点赞过该文章: userId={}, articleId={}", userId, articleId);
            return;
        }
        
        ArticleLikeEntity like = new ArticleLikeEntity(articleId, userId);
        articleLikeRepository.save(like);
        
        article.getStatistics().incrementLikeCount();
        articleRepository.save(article);
        
        if (cqrsEnabled && readModelSyncService != null) {
            readModelSyncService.syncArticle(articleId);
        }
        
        logger.info("文章点赞成功: userId={}, articleId={}", userId, articleId);
    }
    
    public void unlikeArticle(String articleId, String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ArticleDomainException("UNAUTHORIZED", "请先登录");
        }
        
        Article article = findArticleById(articleId);
        
        if (!articleLikeRepository.existsByArticleIdAndUserId(articleId, userId)) {
            logger.info("用户未点赞过该文章: userId={}, articleId={}", userId, articleId);
            return;
        }
        
        articleLikeRepository.deleteByArticleIdAndUserId(articleId, userId);
        
        article.getStatistics().decrementLikeCount();
        articleRepository.save(article);
        
        if (cqrsEnabled && readModelSyncService != null) {
            readModelSyncService.syncArticle(articleId);
        }
        
        logger.info("取消点赞成功: userId={}, articleId={}", userId, articleId);
    }
    
    public void bookmarkArticle(String articleId, String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ArticleDomainException("UNAUTHORIZED", "请先登录");
        }
        
        Article article = findArticleById(articleId);
        
        if (articleBookmarkRepository.existsByArticleIdAndUserId(articleId, userId)) {
            logger.info("用户已收藏过该文章: userId={}, articleId={}", userId, articleId);
            return;
        }
        
        ArticleBookmarkEntity bookmark = new ArticleBookmarkEntity(articleId, userId);
        articleBookmarkRepository.save(bookmark);
        
        article.getStatistics().incrementBookmarkCount();
        articleRepository.save(article);
        
        if (cqrsEnabled && readModelSyncService != null) {
            readModelSyncService.syncArticle(articleId);
        }
        
        logger.info("文章收藏成功: userId={}, articleId={}", userId, articleId);
    }
    
    public void unbookmarkArticle(String articleId, String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ArticleDomainException("UNAUTHORIZED", "请先登录");
        }
        
        Article article = findArticleById(articleId);
        
        if (!articleBookmarkRepository.existsByArticleIdAndUserId(articleId, userId)) {
            logger.info("用户未收藏过该文章: userId={}, articleId={}", userId, articleId);
            return;
        }
        
        articleBookmarkRepository.deleteByArticleIdAndUserId(articleId, userId);
        
        article.getStatistics().decrementBookmarkCount();
        articleRepository.save(article);
        
        if (cqrsEnabled && readModelSyncService != null) {
            readModelSyncService.syncArticle(articleId);
        }
        
        logger.info("取消收藏成功: userId={}, articleId={}", userId, articleId);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Boolean> getLikeAndBookmarkStatus(String articleId, String userId) {
        Map<String, Boolean> status = new HashMap<>();
        
        if (userId == null || userId.trim().isEmpty()) {
            status.put("liked", false);
            status.put("bookmarked", false);
            return status;
        }
        
        status.put("liked", articleLikeRepository.existsByArticleIdAndUserId(articleId, userId));
        status.put("bookmarked", articleBookmarkRepository.existsByArticleIdAndUserId(articleId, userId));
        
        return status;
    }
    
    @Transactional(readOnly = true)
    public PageResult<ArticleListDto> getBookmarkedArticles(String userId, int page, int size) {
        if (userId == null || userId.trim().isEmpty()) {
            return new PageResult<>(List.of(), 0, size, 0, 0, true, true, false, false);
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ArticleBookmarkEntity> bookmarks = articleBookmarkRepository.findByUserId(userId, pageable);
        
        List<ArticleListDto> articles = bookmarks.getContent().stream()
                .map(bookmark -> {
                    try {
                        Article article = findArticleById(bookmark.getArticleId());
                        return toListDto(article);
                    } catch (Exception e) {
                        logger.warn("获取收藏文章失败: {}", bookmark.getArticleId());
                        return null;
                    }
                })
                .filter(a -> a != null)
                .collect(Collectors.toList());
        
        return new PageResult<>(
                articles,
                bookmarks.getNumber(),
                bookmarks.getSize(),
                bookmarks.getTotalElements(),
                bookmarks.getTotalPages(),
                bookmarks.isFirst(),
                bookmarks.isLast(),
                bookmarks.hasNext(),
                bookmarks.hasPrevious()
        );
    }
    
    private Article findArticleById(String articleId) {
        return articleRepository.findById(ArticleId.of(articleId))
                .orElseThrow(() -> new ArticleDomainException("ARTICLE_NOT_FOUND", "文章不存在: " + articleId));
    }
    
    private ArticleDto toDto(Article article) {
        return new ArticleDto(
                article.getId().getValue(),
                article.getAuthorId().getValue(),
                article.getTitle().getValue(),
                article.getContent().getValue(),
                null,
                article.getStatus(),
                article.getPublishTime(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                toStatisticsDto(article.getStatistics())
        );
    }
    
    private ArticleListDto toListDto(Article article) {
        String summary = article.getContent().getValue();
        if (summary.length() > 200) {
            summary = summary.substring(0, 200) + "...";
        }
        
        return new ArticleListDto(
                article.getId().getValue(),
                article.getAuthorId().getValue(),
                article.getTitle().getValue(),
                summary,
                article.getStatus(),
                article.getPublishTime(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                toStatisticsDto(article.getStatistics())
        );
    }
    
    private ArticleListDto fromReadModel(ArticleReadModel readModel) {
        return new ArticleListDto(
                readModel.getId(),
                readModel.getAuthorId(),
                readModel.getTitle(),
                readModel.getSummary(),
                ArticleStatus.valueOf(readModel.getStatus()),
                readModel.getPublishTime(),
                readModel.getCreatedAt(),
                readModel.getUpdatedAt(),
                new ArticleStatisticsDto(
                        readModel.getViewCount(),
                        readModel.getLikeCount(),
                        readModel.getCommentCount(),
                        readModel.getShareCount()
                )
        );
    }
    
    private ArticleStatisticsDto toStatisticsDto(ArticleStatistics statistics) {
        return new ArticleStatisticsDto(
                statistics.getViewCount(),
                statistics.getLikeCount(),
                statistics.getCommentCount(),
                statistics.getShareCount(),
                statistics.getBookmarkCount()
        );
    }
    
    public void incrementCommentCount(String articleId, String commentId, String operation) {
        Article article = findArticleById(articleId);
        articleRepository.save(article);
        if (cqrsEnabled && readModelSyncService != null) {
            readModelSyncService.syncArticle(articleId);
        }
    }
    
    public void decrementCommentCount(String articleId, String commentId, String operation) {
        Article article = findArticleById(articleId);
        articleRepository.save(article);
        if (cqrsEnabled && readModelSyncService != null) {
            readModelSyncService.syncArticle(articleId);
        }
    }
}
