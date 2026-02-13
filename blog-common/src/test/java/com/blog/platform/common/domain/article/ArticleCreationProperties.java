package com.blog.platform.common.domain.article;

import com.blog.platform.common.domain.user.UserId;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * **Feature: microservice-blog-platform, Property 5: 文章创建权限验证**
 * **验证需求: Requirements 2.1**
 * 
 * 属性测试：验证文章创建权限的正确性
 */
class ArticleCreationProperties {
    
    @Property(tries = 100)
    @Label("对于任何有效用户，创建文章应当生成草稿状态的文章并关联到该用户")
    void validUserCanCreateArticle(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validArticleTitles") String titleText,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 有效的用户ID、标题和内容
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent content = ArticleContent.of(contentText);
        
        // When: 创建文章
        Article article = Article.create(authorId, title, content);
        
        // Then: 文章应该被成功创建并关联到用户
        assertThat(article).isNotNull();
        assertThat(article.getId()).isNotNull();
        assertThat(article.getId().getValue()).isNotEmpty();
        
        // 验证文章关联到正确的用户
        assertThat(article.getAuthorId()).isEqualTo(authorId);
        assertThat(article.isAuthor(authorId)).isTrue();
        
        // 验证文章内容
        assertThat(article.getTitle()).isEqualTo(title);
        assertThat(article.getContent()).isEqualTo(content);
        
        // 验证文章状态为草稿
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(article.getStatus().isDraft()).isTrue();
        assertThat(article.canEdit()).isTrue();
        assertThat(article.canPublish()).isTrue();
        
        // 验证时间戳
        assertThat(article.getCreatedAt()).isNotNull();
        assertThat(article.getUpdatedAt()).isNotNull();
        assertThat(article.getPublishTime()).isNull(); // 草稿状态没有发布时间
        
        // 验证统计信息初始化
        assertThat(article.getStatistics()).isNotNull();
        assertThat(article.getStatistics().getViewCount()).isEqualTo(0);
        assertThat(article.getStatistics().getLikeCount()).isEqualTo(0);
        assertThat(article.getStatistics().getCommentCount()).isEqualTo(0);
        assertThat(article.getStatistics().getShareCount()).isEqualTo(0);
        
        // 验证领域事件被发布
        assertThat(article.hasDomainEvents()).isTrue();
        assertThat(article.getDomainEvents()).hasSize(1);
        assertThat(article.getDomainEvents().get(0)).isInstanceOf(ArticleCreatedEvent.class);
        
        ArticleCreatedEvent event = (ArticleCreatedEvent) article.getDomainEvents().get(0);
        assertThat(event.getAggregateId()).isEqualTo(article.getId().getValue());
        assertThat(event.getAuthorId()).isEqualTo(authorId.getValue());
        assertThat(event.getTitle()).isEqualTo(titleText);
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的用户ID，文章创建应当失败")
    void nullUserIdArticleCreationFails(
            @ForAll("validArticleTitles") String titleText,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 空的用户ID
        UserId nullUserId = null;
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent content = ArticleContent.of(contentText);
        
        // When & Then: 创建文章应该失败
        assertThatThrownBy(() -> Article.create(nullUserId, title, content))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("作者ID不能为空");
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的文章标题，文章创建应当失败")
    void nullTitleArticleCreationFails(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 空的文章标题
        ArticleTitle nullTitle = null;
        ArticleContent content = ArticleContent.of(contentText);
        
        // When & Then: 创建文章应该失败
        assertThatThrownBy(() -> Article.create(authorId, nullTitle, content))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("文章标题不能为空");
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的文章内容，文章创建应当失败")
    void nullContentArticleCreationFails(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validArticleTitles") String titleText) {
        
        // Given: 空的文章内容
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent nullContent = null;
        
        // When & Then: 创建文章应该失败
        assertThatThrownBy(() -> Article.create(authorId, title, nullContent))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("文章内容不能为空");
    }
    
    @Property(tries = 100)
    @Label("对于任何用户，创建的文章应当只能被该用户编辑")
    void articleOwnershipVerification(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validUserIds") UserId otherUserId,
            @ForAll("validArticleTitles") String titleText,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 一个用户创建的文章
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent content = ArticleContent.of(contentText);
        Article article = Article.create(authorId, title, content);
        
        // When & Then: 验证所有权
        assertThat(article.isAuthor(authorId)).isTrue();
        
        // 如果是不同的用户，应该不是作者
        if (!authorId.equals(otherUserId)) {
            assertThat(article.isAuthor(otherUserId)).isFalse();
        }
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的标题格式，标题创建应当失败")
    void invalidTitleCreationFails(@ForAll("invalidTitles") String invalidTitle) {
        // When & Then: 创建无效标题应该失败
        assertThatThrownBy(() -> ArticleTitle.of(invalidTitle))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的内容格式，内容创建应当失败")
    void invalidContentCreationFails(@ForAll("invalidContents") String invalidContent) {
        // When & Then: 创建无效内容应该失败
        assertThatThrownBy(() -> ArticleContent.of(invalidContent))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    // 生成器方法
    @Provide
    Arbitrary<UserId> validUserIds() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(5)
            .ofMaxLength(20)
            .map(s -> s + "-" + Arbitraries.integers().between(1, 999).sample())
            .map(UserId::of);
    }
    
    @Provide
    Arbitrary<String> validArticleTitles() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(1)
            .ofMaxLength(100)
            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1) + " Article");
    }
    
    @Provide
    Arbitrary<String> validArticleContents() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(10)
            .ofMaxLength(1000)
            .map(s -> "This is article content: " + s + ". It contains meaningful information.");
    }
    
    @Provide
    Arbitrary<String> invalidTitles() {
        return Arbitraries.oneOf(
            // 空字符串
            Arbitraries.just(""),
            // 只有空格
            Arbitraries.just("   "),
            // 太长的标题 (超过200字符)
            Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(201)
                .ofMaxLength(300)
        );
    }
    
    @Provide
    Arbitrary<String> invalidContents() {
        return Arbitraries.oneOf(
            // 空字符串
            Arbitraries.just(""),
            // 只有空格
            Arbitraries.just("   "),
            // 太长的内容 (超过100000字符)
            Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(100001)
                .ofMaxLength(100010)
        );
    }
}