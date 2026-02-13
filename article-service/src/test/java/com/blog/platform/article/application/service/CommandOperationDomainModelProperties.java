package com.blog.platform.article.application.service;

import com.blog.platform.article.application.dto.ArticleDto;
import com.blog.platform.article.application.dto.CreateArticleRequest;
import com.blog.platform.article.application.dto.UpdateArticleRequest;
import com.blog.platform.article.domain.repository.ArticleRepository;
import com.blog.platform.article.infrastructure.elasticsearch.service.ArticleReadModelSyncService;
import com.blog.platform.article.infrastructure.messaging.ArticleEventPublisher;
import com.blog.platform.common.domain.DomainEventPublisher;
import com.blog.platform.common.domain.article.*;
import com.blog.platform.common.domain.user.UserId;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * **Feature: microservice-blog-platform, Property 13: 命令操作领域模型更新**
 * **验证需求: Requirements 6.4**
 * 
 * 命令操作领域模型更新属性测试
 * 对于任何命令操作，系统应当通过领域服务正确更新领域模型状态
 */
class CommandOperationDomainModelProperties {
    
    /**
     * 属性测试：创建文章命令更新领域模型
     * 对于任何创建文章命令，系统应当创建草稿状态的文章并正确设置所有字段
     */
    @Property(tries = 100)
    void createArticleCommandUpdatesDomainModel(
            @ForAll("validAuthorId") String authorId,
            @ForAll("validTitle") String title,
            @ForAll("validContent") String content) {
        
        // Setup mocks
        ArticleRepository articleRepository = Mockito.mock(ArticleRepository.class);
        ArticleReadModelSyncService readModelSyncService = Mockito.mock(ArticleReadModelSyncService.class);
        DomainEventPublisher eventPublisher = Mockito.mock(DomainEventPublisher.class);
        ArticleEventPublisher articleEventPublisher = Mockito.mock(ArticleEventPublisher.class);
        com.blog.platform.common.cache.CacheProtectionService cacheProtectionService = 
            Mockito.mock(com.blog.platform.common.cache.CacheProtectionService.class);
        
        ArticleApplicationService articleService = new ArticleApplicationService();
        // Use reflection or setter injection for testing
        setField(articleService, "articleRepository", articleRepository);
        setField(articleService, "readModelSyncService", readModelSyncService);
        setField(articleService, "eventPublisher", eventPublisher);
        setField(articleService, "articleEventPublisher", articleEventPublisher);
        setField(articleService, "cacheProtectionService", cacheProtectionService);
        
        // Given: 创建文章请求
        CreateArticleRequest request = new CreateArticleRequest(title, content, null);
        
        // Mock repository behavior - 保存时返回保存的文章
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            return article;
        });
        
        // When: 执行创建文章命令
        ArticleDto result = articleService.createArticle(authorId, request);
        
        // Then: 文章应当被创建且状态为草稿
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(result.getAuthorId()).isEqualTo(authorId);
        assertThat(result.getCreatedAt()).isNotNull();
    }
    
    /**
     * 属性测试：更新文章命令更新领域模型
     * 对于任何更新文章命令，系统应当正确更新文章的状态
     */
    @Property(tries = 100)
    void updateArticleCommandUpdatesDomainModel(
            @ForAll("validAuthorId") String authorId,
            @ForAll("validTitle") String originalTitle,
            @ForAll("validContent") String originalContent,
            @ForAll("validTitle") String newTitle,
            @ForAll("validContent") String newContent) {
        
        // Setup mocks
        ArticleRepository articleRepository = Mockito.mock(ArticleRepository.class);
        ArticleReadModelSyncService readModelSyncService = Mockito.mock(ArticleReadModelSyncService.class);
        DomainEventPublisher eventPublisher = Mockito.mock(DomainEventPublisher.class);
        ArticleEventPublisher articleEventPublisher = Mockito.mock(ArticleEventPublisher.class);
        com.blog.platform.common.cache.CacheProtectionService cacheProtectionService = 
            Mockito.mock(com.blog.platform.common.cache.CacheProtectionService.class);
        
        ArticleApplicationService articleService = new ArticleApplicationService();
        setField(articleService, "articleRepository", articleRepository);
        setField(articleService, "readModelSyncService", readModelSyncService);
        setField(articleService, "eventPublisher", eventPublisher);
        setField(articleService, "articleEventPublisher", articleEventPublisher);
        setField(articleService, "cacheProtectionService", cacheProtectionService);
        
        // Given: 一个已存在的文章
        ArticleId articleId = ArticleId.generate();
        UserId userId = UserId.of(authorId);
        Article existingArticle = Article.create(
            userId,
            ArticleTitle.of(originalTitle),
            ArticleContent.of(originalContent)
        );
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            return article;
        });
        
        // When: 执行更新文章命令
        UpdateArticleRequest updateRequest = new UpdateArticleRequest(newTitle, newContent, null);
        ArticleDto result = articleService.updateArticle(articleId.getValue(), authorId, updateRequest);
        
        // Then: 文章应当被更新
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(newTitle);
        assertThat(result.getContent()).isEqualTo(newContent);
        assertThat(result.getUpdatedAt()).isNotNull();
    }
    
    /**
     * 属性测试：发布文章命令更新领域模型状态
     * 对于任何发布文章命令，系统应当将文章状态从草稿变为已发布
     */
    @Property(tries = 50)
    void publishArticleCommandUpdatesDomainModelStatus(
            @ForAll("validAuthorId") String authorId,
            @ForAll("validTitle") String title,
            @ForAll("validContent") String content) {
        
        // Setup mocks
        ArticleRepository articleRepository = Mockito.mock(ArticleRepository.class);
        ArticleReadModelSyncService readModelSyncService = Mockito.mock(ArticleReadModelSyncService.class);
        DomainEventPublisher eventPublisher = Mockito.mock(DomainEventPublisher.class);
        ArticleEventPublisher articleEventPublisher = Mockito.mock(ArticleEventPublisher.class);
        com.blog.platform.article.application.saga.ArticlePublishSaga articlePublishSaga = 
            Mockito.mock(com.blog.platform.article.application.saga.ArticlePublishSaga.class);
        com.blog.platform.common.cache.CacheProtectionService cacheProtectionService = 
            Mockito.mock(com.blog.platform.common.cache.CacheProtectionService.class);
        
        ArticleApplicationService articleService = new ArticleApplicationService();
        setField(articleService, "articleRepository", articleRepository);
        setField(articleService, "readModelSyncService", readModelSyncService);
        setField(articleService, "eventPublisher", eventPublisher);
        setField(articleService, "articleEventPublisher", articleEventPublisher);
        setField(articleService, "articlePublishSaga", Optional.of(articlePublishSaga));
        setField(articleService, "cacheProtectionService", cacheProtectionService);
        
        // Given: 一个草稿状态的文章
        ArticleId articleId = ArticleId.generate();
        UserId userId = UserId.of(authorId);
        Article draftArticle = Article.create(
            userId,
            ArticleTitle.of(title),
            ArticleContent.of(content)
        );
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(draftArticle));
        
        // Mock saga to update article status（高级版路径）
        Mockito.doAnswer(invocation -> {
            draftArticle.publish();
            return null;
        }).when(articlePublishSaga).publishArticle(articleId.getValue(), authorId);
        
        // When: 执行发布文章命令
        articleService.publishArticle(articleId.getValue(), authorId);
        
        // Then: 文章状态应当变为已发布
        assertThat(draftArticle.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(draftArticle.getPublishTime()).isNotNull();
    }
    
    /**
     * 属性测试：命令操作的一致性
     * 对于任何命令操作序列，领域模型状态应当保持一致
     */
    @Property(tries = 30)
    void commandOperationsMaintainDomainModelConsistency(
            @ForAll("validAuthorId") String authorId,
            @ForAll("validTitle") String title1,
            @ForAll("validContent") String content1,
            @ForAll("validTitle") String title2,
            @ForAll("validContent") String content2) {
        
        // Setup mocks
        ArticleRepository articleRepository = Mockito.mock(ArticleRepository.class);
        ArticleReadModelSyncService readModelSyncService = Mockito.mock(ArticleReadModelSyncService.class);
        DomainEventPublisher eventPublisher = Mockito.mock(DomainEventPublisher.class);
        ArticleEventPublisher articleEventPublisher = Mockito.mock(ArticleEventPublisher.class);
        com.blog.platform.common.cache.CacheProtectionService cacheProtectionService = 
            Mockito.mock(com.blog.platform.common.cache.CacheProtectionService.class);
        
        ArticleApplicationService articleService = new ArticleApplicationService();
        setField(articleService, "articleRepository", articleRepository);
        setField(articleService, "readModelSyncService", readModelSyncService);
        setField(articleService, "eventPublisher", eventPublisher);
        setField(articleService, "articleEventPublisher", articleEventPublisher);
        setField(articleService, "cacheProtectionService", cacheProtectionService);
        
        // Given: 创建文章
        CreateArticleRequest createRequest = new CreateArticleRequest(title1, content1, null);
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ArticleDto created = articleService.createArticle(authorId, createRequest);
        ArticleId articleId = ArticleId.of(created.getId());
        
        // When: 更新文章
        Article existingArticle = Article.create(
            UserId.of(authorId),
            ArticleTitle.of(title1),
            ArticleContent.of(content1)
        );
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));
        
        UpdateArticleRequest updateRequest = new UpdateArticleRequest(title2, content2, null);
        ArticleDto updated = articleService.updateArticle(articleId.getValue(), authorId, updateRequest);
        
        // Then: 更新后的状态应当反映最新的命令操作
        assertThat(updated.getTitle()).isEqualTo(title2);
        assertThat(updated.getContent()).isEqualTo(content2);
        assertThat(updated.getAuthorId()).isEqualTo(authorId); // 作者ID不应改变
    }
    
    // Helper method to set private fields using reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
    
    @Provide
    Arbitrary<String> validAuthorId() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(10)
                .ofMaxLength(36);
    }
    
    @Provide
    Arbitrary<String> validTitle() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars(' ', '-', '_', '，', '。')
                .ofMinLength(5)
                .ofMaxLength(100);
    }
    
    @Provide
    Arbitrary<String> validContent() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars(' ', '\n', '\t', '，', '。', '！', '？')
                .ofMinLength(10)
                .ofMaxLength(1000);
    }
}

