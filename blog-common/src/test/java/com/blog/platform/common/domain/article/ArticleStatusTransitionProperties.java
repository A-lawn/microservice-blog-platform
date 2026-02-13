package com.blog.platform.common.domain.article;

import com.blog.platform.common.domain.user.UserId;
import net.jqwik.api.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * **Feature: microservice-blog-platform, Property 6: 文章发布状态转换**
 * **验证需求: Requirements 2.2**
 * 
 * 属性测试：验证文章状态转换的正确性
 */
class ArticleStatusTransitionProperties {
    
    @Property(tries = 100)
    @Label("对于任何草稿状态的文章，执行发布操作后文章状态应当变为已发布")
    void draftArticleCanBePublished(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validArticleTitles") String titleText,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 一个草稿状态的文章
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent content = ArticleContent.of(contentText);
        Article article = Article.create(authorId, title, content);
        
        // 验证初始状态为草稿
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(article.getStatus().isDraft()).isTrue();
        assertThat(article.canPublish()).isTrue();
        assertThat(article.getPublishTime()).isNull();
        
        // 记录发布前的时间
        LocalDateTime beforePublish = LocalDateTime.now();
        
        // When: 执行发布操作
        article.publish();
        
        // Then: 文章状态应当变为已发布
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(article.getStatus().isPublished()).isTrue();
        assertThat(article.getStatus().isDraft()).isFalse();
        
        // 验证发布时间被设置
        assertThat(article.getPublishTime()).isNotNull();
        assertThat(article.getPublishTime()).isAfterOrEqualTo(beforePublish);
        assertThat(article.getPublishTime()).isBeforeOrEqualTo(LocalDateTime.now());
        
        // 验证更新时间被更新
        assertThat(article.getUpdatedAt()).isAfterOrEqualTo(beforePublish);
        
        // 验证文章可见性
        assertThat(article.isVisible()).isTrue();
        assertThat(article.getStatus().isVisible()).isTrue();
        
        // 验证发布后的权限
        assertThat(article.canEdit()).isTrue(); // 已发布的文章仍可编辑
        assertThat(article.canPublish()).isFalse(); // 已发布的文章不能再次发布
        assertThat(article.canArchive()).isTrue(); // 已发布的文章可以归档
        
        // 验证领域事件被发布
        assertThat(article.hasDomainEvents()).isTrue();
        // 应该有两个事件：创建事件和发布事件
        assertThat(article.getDomainEvents()).hasSize(2);
        
        // 验证发布事件
        ArticlePublishedEvent publishEvent = (ArticlePublishedEvent) article.getDomainEvents().get(1);
        assertThat(publishEvent.getAggregateId()).isEqualTo(article.getId().getValue());
        assertThat(publishEvent.getAuthorId()).isEqualTo(authorId.getValue());
        assertThat(publishEvent.getTitle()).isEqualTo(titleText);
        assertThat(publishEvent.getPublishTime()).isEqualTo(article.getPublishTime());
    }
    
    @Property(tries = 100)
    @Label("对于任何已发布状态的文章，再次发布操作应当失败")
    void publishedArticleCannotBePublishedAgain(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validArticleTitles") String titleText,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 一个已发布的文章
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent content = ArticleContent.of(contentText);
        Article article = Article.create(authorId, title, content);
        article.publish(); // 先发布
        
        // 验证文章已发布
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(article.canPublish()).isFalse();
        
        // When & Then: 再次发布应该失败
        assertThatThrownBy(() -> article.publish())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("当前状态的文章不能发布: PUBLISHED");
    }
    
    @Property(tries = 100)
    @Label("对于任何已归档状态的文章，发布操作应当失败")
    void archivedArticleCannotBePublished(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validArticleTitles") String titleText,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 一个已归档的文章
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent content = ArticleContent.of(contentText);
        Article article = Article.create(authorId, title, content);
        article.publish(); // 先发布
        article.archive(); // 再归档
        
        // 验证文章已归档
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.ARCHIVED);
        assertThat(article.canPublish()).isFalse();
        
        // When & Then: 发布归档文章应该失败
        assertThatThrownBy(() -> article.publish())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("当前状态的文章不能发布: ARCHIVED");
    }
    
    @Property(tries = 100)
    @Label("对于任何已发布的文章，归档操作后状态应当变为已归档")
    void publishedArticleCanBeArchived(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validArticleTitles") String titleText,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 一个已发布的文章
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent content = ArticleContent.of(contentText);
        Article article = Article.create(authorId, title, content);
        article.publish();
        
        // 验证初始状态为已发布
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(article.canArchive()).isTrue();
        assertThat(article.isVisible()).isTrue();
        
        // 记录归档前的时间
        LocalDateTime beforeArchive = LocalDateTime.now();
        
        // When: 执行归档操作
        article.archive();
        
        // Then: 文章状态应当变为已归档
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.ARCHIVED);
        assertThat(article.getStatus().isArchived()).isTrue();
        assertThat(article.getStatus().isPublished()).isFalse();
        
        // 验证更新时间被更新
        assertThat(article.getUpdatedAt()).isAfterOrEqualTo(beforeArchive);
        
        // 验证文章不可见
        assertThat(article.isVisible()).isFalse();
        assertThat(article.getStatus().isVisible()).isFalse();
        
        // 验证归档后的权限
        assertThat(article.canEdit()).isFalse(); // 已归档的文章不能编辑
        assertThat(article.canPublish()).isFalse(); // 已归档的文章不能发布
        assertThat(article.canArchive()).isFalse(); // 已归档的文章不能再次归档
        
        // 验证领域事件被发布
        assertThat(article.hasDomainEvents()).isTrue();
        // 应该有三个事件：创建事件、发布事件和归档事件
        assertThat(article.getDomainEvents()).hasSize(3);
        
        // 验证归档事件
        ArticleArchivedEvent archiveEvent = (ArticleArchivedEvent) article.getDomainEvents().get(2);
        assertThat(archiveEvent.getAggregateId()).isEqualTo(article.getId().getValue());
        assertThat(archiveEvent.getAuthorId()).isEqualTo(authorId.getValue());
        assertThat(archiveEvent.getTitle()).isEqualTo(titleText);
    }
    
    @Property(tries = 100)
    @Label("对于任何草稿状态的文章，归档操作应当失败")
    void draftArticleCannotBeArchived(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validArticleTitles") String titleText,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 一个草稿状态的文章
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent content = ArticleContent.of(contentText);
        Article article = Article.create(authorId, title, content);
        
        // 验证文章是草稿状态
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(article.canArchive()).isFalse();
        
        // When & Then: 归档草稿文章应该失败
        assertThatThrownBy(() -> article.archive())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("当前状态的文章不能归档: DRAFT");
    }
    
    @Property(tries = 100)
    @Label("对于任何已归档状态的文章，再次归档操作应当失败")
    void archivedArticleCannotBeArchivedAgain(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validArticleTitles") String titleText,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 一个已归档的文章
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent content = ArticleContent.of(contentText);
        Article article = Article.create(authorId, title, content);
        article.publish();
        article.archive();
        
        // 验证文章已归档
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.ARCHIVED);
        assertThat(article.canArchive()).isFalse();
        
        // When & Then: 再次归档应该失败
        assertThatThrownBy(() -> article.archive())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("当前状态的文章不能归档: ARCHIVED");
    }
    
    @Property(tries = 100)
    @Label("对于任何文章状态，状态转换应当保持业务不变量")
    void statusTransitionMaintainsInvariants(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validArticleTitles") String titleText,
            @ForAll("validArticleContents") String contentText) {
        
        // Given: 一个新创建的文章
        ArticleTitle title = ArticleTitle.of(titleText);
        ArticleContent content = ArticleContent.of(contentText);
        Article article = Article.create(authorId, title, content);
        
        // 验证草稿状态的不变量
        assertThat(article.getStatus().isDraft()).isTrue();
        assertThat(article.getStatus().canPublish()).isTrue();
        assertThat(article.getStatus().canEdit()).isTrue();
        assertThat(article.getStatus().canArchive()).isFalse();
        assertThat(article.getStatus().isVisible()).isFalse();
        
        // When: 发布文章
        article.publish();
        
        // Then: 验证已发布状态的不变量
        assertThat(article.getStatus().isPublished()).isTrue();
        assertThat(article.getStatus().canPublish()).isFalse();
        assertThat(article.getStatus().canEdit()).isTrue();
        assertThat(article.getStatus().canArchive()).isTrue();
        assertThat(article.getStatus().isVisible()).isTrue();
        
        // When: 归档文章
        article.archive();
        
        // Then: 验证已归档状态的不变量
        assertThat(article.getStatus().isArchived()).isTrue();
        assertThat(article.getStatus().canPublish()).isFalse();
        assertThat(article.getStatus().canEdit()).isFalse();
        assertThat(article.getStatus().canArchive()).isFalse();
        assertThat(article.getStatus().isVisible()).isFalse();
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
}