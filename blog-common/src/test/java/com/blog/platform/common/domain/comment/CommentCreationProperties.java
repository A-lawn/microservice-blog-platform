package com.blog.platform.common.domain.comment;

import com.blog.platform.common.domain.article.ArticleId;
import com.blog.platform.common.domain.user.UserId;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * **Feature: microservice-blog-platform, Property 8: 评论创建身份验证**
 * **验证需求: Requirements 3.1**
 * 
 * 属性测试：验证评论创建时的身份验证和关联正确性
 */
class CommentCreationProperties {
    
    @Property(tries = 100)
    @Label("对于任何评论创建请求，系统应当验证用户身份并将评论关联到正确的文章和用户")
    void validCommentCreationVerifiesIdentityAndAssociation(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validCommentContents") String contentText) {
        
        // Given: 有效的文章ID、用户ID和评论内容
        CommentContent content = CommentContent.of(contentText);
        
        // When: 创建评论
        Comment comment = Comment.create(articleId, authorId, content);
        
        // Then: 评论应该被成功创建并正确关联
        assertThat(comment).isNotNull();
        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getId().getValue()).isNotEmpty();
        
        // 验证评论关联到正确的文章
        assertThat(comment.getArticleId()).isEqualTo(articleId);
        
        // 验证评论关联到正确的用户（身份验证）
        assertThat(comment.getAuthorId()).isEqualTo(authorId);
        assertThat(comment.isAuthor(authorId)).isTrue();
        
        // 验证评论内容
        assertThat(comment.getContent()).isEqualTo(content);
        
        // 验证评论状态为活跃（默认状态）
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(comment.isVisible()).isTrue();
        assertThat(comment.canEdit()).isTrue();
        assertThat(comment.canReply()).isTrue();
        
        // 验证这是顶级评论（不是回复）
        assertThat(comment.getParentId()).isNull();
        assertThat(comment.isTopLevel()).isTrue();
        assertThat(comment.isReply()).isFalse();
        
        // 验证时间戳
        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(comment.getUpdatedAt()).isNotNull();
        assertThat(comment.getCreatedAt()).isEqualTo(comment.getUpdatedAt());
        
        // 验证领域事件被发布
        assertThat(comment.hasDomainEvents()).isTrue();
        assertThat(comment.getDomainEvents()).hasSize(1);
        assertThat(comment.getDomainEvents().get(0)).isInstanceOf(CommentCreatedEvent.class);
        
        CommentCreatedEvent event = (CommentCreatedEvent) comment.getDomainEvents().get(0);
        assertThat(event.getAggregateId()).isEqualTo(comment.getId().getValue());
        assertThat(event.getArticleId()).isEqualTo(articleId.getValue());
        assertThat(event.getAuthorId()).isEqualTo(authorId.getValue());
        assertThat(event.getParentId()).isNull();
        assertThat(event.getContent()).isEqualTo(contentText);
        assertThat(event.isReply()).isFalse();
    }
    
    @Property(tries = 100)
    @Label("对于任何回复评论创建请求，系统应当验证用户身份并正确维护层级关系")
    void validReplyCommentCreationVerifiesIdentityAndHierarchy(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validCommentContents") String contentText,
            @ForAll("validCommentIds") CommentId parentId) {
        
        // Given: 有效的文章ID、用户ID、评论内容和父评论ID
        CommentContent content = CommentContent.of(contentText);
        
        // When: 创建回复评论
        Comment replyComment = Comment.createReply(articleId, authorId, content, parentId);
        
        // Then: 回复评论应该被成功创建并正确关联
        assertThat(replyComment).isNotNull();
        assertThat(replyComment.getId()).isNotNull();
        assertThat(replyComment.getId().getValue()).isNotEmpty();
        
        // 验证回复评论关联到正确的文章
        assertThat(replyComment.getArticleId()).isEqualTo(articleId);
        
        // 验证回复评论关联到正确的用户（身份验证）
        assertThat(replyComment.getAuthorId()).isEqualTo(authorId);
        assertThat(replyComment.isAuthor(authorId)).isTrue();
        
        // 验证回复评论内容
        assertThat(replyComment.getContent()).isEqualTo(content);
        
        // 验证回复评论的层级关系
        assertThat(replyComment.getParentId()).isEqualTo(parentId);
        assertThat(replyComment.isReply()).isTrue();
        assertThat(replyComment.isTopLevel()).isFalse();
        
        // 验证回复评论状态为活跃（默认状态）
        assertThat(replyComment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(replyComment.isVisible()).isTrue();
        
        // 验证领域事件被发布
        assertThat(replyComment.hasDomainEvents()).isTrue();
        assertThat(replyComment.getDomainEvents()).hasSize(1);
        assertThat(replyComment.getDomainEvents().get(0)).isInstanceOf(CommentCreatedEvent.class);
        
        CommentCreatedEvent event = (CommentCreatedEvent) replyComment.getDomainEvents().get(0);
        assertThat(event.getAggregateId()).isEqualTo(replyComment.getId().getValue());
        assertThat(event.getArticleId()).isEqualTo(articleId.getValue());
        assertThat(event.getAuthorId()).isEqualTo(authorId.getValue());
        assertThat(event.getParentId()).isEqualTo(parentId.getValue());
        assertThat(event.getContent()).isEqualTo(contentText);
        assertThat(event.isReply()).isTrue();
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的文章ID，评论创建应当失败")
    void nullArticleIdCommentCreationFails(
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validCommentContents") String contentText) {
        
        // Given: 空的文章ID
        ArticleId nullArticleId = null;
        CommentContent content = CommentContent.of(contentText);
        
        // When & Then: 创建评论应该失败
        assertThatThrownBy(() -> Comment.create(nullArticleId, authorId, content))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("文章ID不能为空");
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的用户ID，评论创建应当失败")
    void nullUserIdCommentCreationFails(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validCommentContents") String contentText) {
        
        // Given: 空的用户ID
        UserId nullUserId = null;
        CommentContent content = CommentContent.of(contentText);
        
        // When & Then: 创建评论应该失败
        assertThatThrownBy(() -> Comment.create(articleId, nullUserId, content))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("作者ID不能为空");
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的评论内容，评论创建应当失败")
    void nullContentCommentCreationFails(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validUserIds") UserId authorId) {
        
        // Given: 空的评论内容
        CommentContent nullContent = null;
        
        // When & Then: 创建评论应该失败
        assertThatThrownBy(() -> Comment.create(articleId, authorId, nullContent))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("评论内容不能为空");
    }
    
    @Property(tries = 100)
    @Label("对于任何用户，创建的评论应当只能被该用户识别为作者")
    void commentOwnershipVerification(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validUserIds") UserId otherUserId,
            @ForAll("validCommentContents") String contentText) {
        
        // Given: 一个用户创建的评论
        CommentContent content = CommentContent.of(contentText);
        Comment comment = Comment.create(articleId, authorId, content);
        
        // When & Then: 验证所有权
        assertThat(comment.isAuthor(authorId)).isTrue();
        
        // 如果是不同的用户，应该不是作者
        if (!authorId.equals(otherUserId)) {
            assertThat(comment.isAuthor(otherUserId)).isFalse();
        }
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的评论内容格式，内容创建应当失败")
    void invalidContentCreationFails(@ForAll("invalidContents") String invalidContent) {
        // When & Then: 创建无效内容应该失败
        assertThatThrownBy(() -> CommentContent.of(invalidContent))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的父评论ID，回复创建应当失败")
    void nullParentIdReplyCreationFails(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validCommentContents") String contentText) {
        
        // Given: 空的父评论ID
        CommentContent content = CommentContent.of(contentText);
        CommentId nullParentId = null;
        
        // When & Then: 创建回复应该失败
        assertThatThrownBy(() -> Comment.createReply(articleId, authorId, content, nullParentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("父评论ID不能为空");
    }
    
    // 生成器方法
    @Provide
    Arbitrary<ArticleId> validArticleIds() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(5)
            .ofMaxLength(20)
            .map(s -> s + "-" + Arbitraries.integers().between(1, 999).sample())
            .map(ArticleId::of);
    }
    
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
    Arbitrary<CommentId> validCommentIds() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(5)
            .ofMaxLength(20)
            .map(s -> s + "-" + Arbitraries.integers().between(1, 999).sample())
            .map(CommentId::of);
    }
    
    @Provide
    Arbitrary<String> validCommentContents() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(1)
            .ofMaxLength(500)
            .map(s -> "This is a comment: " + s + ". It contains meaningful discussion.");
    }
    
    @Provide
    Arbitrary<String> invalidContents() {
        return Arbitraries.oneOf(
            // 空字符串
            Arbitraries.just(""),
            // 只有空格
            Arbitraries.just("   "),
            // 太长的内容 (超过2000字符)
            Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2001)
                .ofMaxLength(2010)
        );
    }
}