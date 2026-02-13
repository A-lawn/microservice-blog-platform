package com.blog.platform.common.domain.comment;

import com.blog.platform.common.domain.article.ArticleId;
import com.blog.platform.common.domain.user.UserId;
import net.jqwik.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: microservice-blog-platform, Property 9: 评论层级关系维护**
 * **验证需求: Requirements 3.3**
 * 
 * 属性测试：验证评论回复操作时系统正确维护父子评论的层级关系
 */
class CommentHierarchyProperties {
    
    @Property(tries = 100)
    @Label("对于任何评论回复操作，系统应当正确维护父子评论的层级关系")
    void commentReplyMaintainsHierarchyRelationship(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validUserIds") UserId parentAuthorId,
            @ForAll("validUserIds") UserId replyAuthorId,
            @ForAll("validCommentContents") String parentContentText,
            @ForAll("validCommentContents") String replyContentText) {
        
        // Given: 创建一个父评论
        CommentContent parentContent = CommentContent.of(parentContentText);
        Comment parentComment = Comment.create(articleId, parentAuthorId, parentContent);
        
        // When: 创建一个回复评论
        CommentContent replyContent = CommentContent.of(replyContentText);
        Comment replyComment = Comment.createReply(articleId, replyAuthorId, replyContent, parentComment.getId());
        
        // Then: 验证层级关系被正确维护
        
        // 1. 父评论应该是顶级评论
        assertThat(parentComment.isTopLevel()).isTrue();
        assertThat(parentComment.isReply()).isFalse();
        assertThat(parentComment.getParentId()).isNull();
        
        // 2. 回复评论应该正确引用父评论
        assertThat(replyComment.isReply()).isTrue();
        assertThat(replyComment.isTopLevel()).isFalse();
        assertThat(replyComment.getParentId()).isNotNull();
        assertThat(replyComment.getParentId()).isEqualTo(parentComment.getId());
        
        // 3. 两个评论都应该属于同一篇文章
        assertThat(parentComment.getArticleId()).isEqualTo(articleId);
        assertThat(replyComment.getArticleId()).isEqualTo(articleId);
        assertThat(replyComment.getArticleId()).isEqualTo(parentComment.getArticleId());
        
        // 4. 验证评论的独立性（不同的ID，内容可以相同）
        assertThat(replyComment.getId()).isNotEqualTo(parentComment.getId());
        
        // 5. 验证作者关系
        assertThat(parentComment.isAuthor(parentAuthorId)).isTrue();
        assertThat(replyComment.isAuthor(replyAuthorId)).isTrue();
        
        // 6. 验证回复评论的事件包含正确的父评论信息
        assertThat(replyComment.hasDomainEvents()).isTrue();
        CommentCreatedEvent replyEvent = (CommentCreatedEvent) replyComment.getDomainEvents().get(0);
        assertThat(replyEvent.isReply()).isTrue();
        assertThat(replyEvent.getParentId()).isEqualTo(parentComment.getId().getValue());
        assertThat(replyEvent.getArticleId()).isEqualTo(articleId.getValue());
        
        // 7. 验证父评论的事件不包含父评论信息（因为它是顶级评论）
        assertThat(parentComment.hasDomainEvents()).isTrue();
        CommentCreatedEvent parentEvent = (CommentCreatedEvent) parentComment.getDomainEvents().get(0);
        assertThat(parentEvent.isReply()).isFalse();
        assertThat(parentEvent.getParentId()).isNull();
    }
    
    @Property(tries = 100)
    @Label("对于任何多层级回复链，每个回复都应当正确维护与其直接父评论的关系")
    void multiLevelReplyChainMaintainsDirectParentRelationship(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validUserIds") UserId authorId,
            @ForAll("commentChains") List<String> contentChain) {
        
        // Given: 创建一个评论链（至少3个评论）
        Assume.that(contentChain.size() >= 3);
        
        List<Comment> commentChain = new ArrayList<>();
        
        // 创建根评论
        CommentContent rootContent = CommentContent.of(contentChain.get(0));
        Comment rootComment = Comment.create(articleId, authorId, rootContent);
        commentChain.add(rootComment);
        
        // 创建回复链
        for (int i = 1; i < contentChain.size(); i++) {
            CommentContent replyContent = CommentContent.of(contentChain.get(i));
            Comment parentComment = commentChain.get(i - 1);
            Comment replyComment = Comment.createReply(articleId, authorId, replyContent, parentComment.getId());
            commentChain.add(replyComment);
        }
        
        // When & Then: 验证每个评论的层级关系
        for (int i = 0; i < commentChain.size(); i++) {
            Comment comment = commentChain.get(i);
            
            if (i == 0) {
                // 根评论应该是顶级评论
                assertThat(comment.isTopLevel()).isTrue();
                assertThat(comment.isReply()).isFalse();
                assertThat(comment.getParentId()).isNull();
            } else {
                // 回复评论应该正确引用其直接父评论
                Comment expectedParent = commentChain.get(i - 1);
                assertThat(comment.isReply()).isTrue();
                assertThat(comment.isTopLevel()).isFalse();
                assertThat(comment.getParentId()).isEqualTo(expectedParent.getId());
                
                // 验证事件中的父评论信息
                CommentCreatedEvent event = (CommentCreatedEvent) comment.getDomainEvents().get(0);
                assertThat(event.isReply()).isTrue();
                assertThat(event.getParentId()).isEqualTo(expectedParent.getId().getValue());
            }
            
            // 所有评论都应该属于同一篇文章
            assertThat(comment.getArticleId()).isEqualTo(articleId);
            assertThat(comment.isAuthor(authorId)).isTrue();
        }
    }
    
    @Property(tries = 100)
    @Label("对于任何评论，其层级状态应当与父评论ID的存在性保持一致")
    void commentHierarchyStatusConsistentWithParentId(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validCommentContents") String contentText,
            @ForAll("optionalParentIds") CommentId parentId) {
        
        // Given & When: 根据是否有父评论ID创建评论
        Comment comment;
        if (parentId == null) {
            CommentContent content = CommentContent.of(contentText);
            comment = Comment.create(articleId, authorId, content);
        } else {
            CommentContent content = CommentContent.of(contentText);
            comment = Comment.createReply(articleId, authorId, content, parentId);
        }
        
        // Then: 验证层级状态与父评论ID的一致性
        if (parentId == null) {
            // 没有父评论ID的评论应该是顶级评论
            assertThat(comment.getParentId()).isNull();
            assertThat(comment.isTopLevel()).isTrue();
            assertThat(comment.isReply()).isFalse();
            
            // 事件中也不应该有父评论信息
            CommentCreatedEvent event = (CommentCreatedEvent) comment.getDomainEvents().get(0);
            assertThat(event.getParentId()).isNull();
            assertThat(event.isReply()).isFalse();
        } else {
            // 有父评论ID的评论应该是回复评论
            assertThat(comment.getParentId()).isEqualTo(parentId);
            assertThat(comment.isReply()).isTrue();
            assertThat(comment.isTopLevel()).isFalse();
            
            // 事件中应该包含父评论信息
            CommentCreatedEvent event = (CommentCreatedEvent) comment.getDomainEvents().get(0);
            assertThat(event.getParentId()).isEqualTo(parentId.getValue());
            assertThat(event.isReply()).isTrue();
        }
    }
    
    @Property(tries = 100)
    @Label("对于任何回复评论，其父评论ID应当是不可变的")
    void replyCommentParentIdIsImmutable(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validUserIds") UserId authorId,
            @ForAll("validCommentContents") String contentText,
            @ForAll("validCommentIds") CommentId parentId) {
        
        // Given: 创建一个回复评论
        CommentContent content = CommentContent.of(contentText);
        Comment replyComment = Comment.createReply(articleId, authorId, content, parentId);
        
        // When: 获取父评论ID
        CommentId initialParentId = replyComment.getParentId();
        
        // Then: 父评论ID应当保持不变（通过多次访问验证）
        assertThat(replyComment.getParentId()).isEqualTo(initialParentId);
        assertThat(replyComment.getParentId()).isEqualTo(parentId);
        
        // 即使进行其他操作（如更新内容），父评论ID也应该保持不变
        CommentContent newContent = CommentContent.of("Updated: " + contentText);
        replyComment.updateContent(newContent);
        
        assertThat(replyComment.getParentId()).isEqualTo(initialParentId);
        assertThat(replyComment.getParentId()).isEqualTo(parentId);
        assertThat(replyComment.isReply()).isTrue();
        assertThat(replyComment.isTopLevel()).isFalse();
    }
    
    @Property(tries = 100)
    @Label("对于任何评论树结构，同一文章下的所有评论都应当共享相同的文章ID")
    void commentTreeSharesSameArticleId(
            @ForAll("validArticleIds") ArticleId articleId,
            @ForAll("validUserIds") UserId authorId,
            @ForAll("commentChains") List<String> contentChain) {
        
        // Given: 创建一个评论树结构
        Assume.that(contentChain.size() >= 2);
        
        List<Comment> comments = new ArrayList<>();
        
        // 创建根评论
        CommentContent rootContent = CommentContent.of(contentChain.get(0));
        Comment rootComment = Comment.create(articleId, authorId, rootContent);
        comments.add(rootComment);
        
        // 创建多个回复（可能形成分支）
        for (int i = 1; i < contentChain.size(); i++) {
            CommentContent replyContent = CommentContent.of(contentChain.get(i));
            // 随机选择一个已存在的评论作为父评论（创建树状结构）
            Comment parentComment = comments.get(i % comments.size());
            Comment replyComment = Comment.createReply(articleId, authorId, replyContent, parentComment.getId());
            comments.add(replyComment);
        }
        
        // When & Then: 验证所有评论都属于同一篇文章
        for (Comment comment : comments) {
            assertThat(comment.getArticleId()).isEqualTo(articleId);
            
            // 验证事件中的文章ID也是一致的
            CommentCreatedEvent event = (CommentCreatedEvent) comment.getDomainEvents().get(0);
            assertThat(event.getArticleId()).isEqualTo(articleId.getValue());
        }
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
            .map(s -> "Comment content: " + s + ". This is a meaningful discussion point.");
    }
    
    @Provide
    Arbitrary<CommentId> optionalParentIds() {
        return Arbitraries.oneOf(
            Arbitraries.just((CommentId) null),
            validCommentIds()
        );
    }
    
    @Provide
    Arbitrary<List<String>> commentChains() {
        return Arbitraries.integers()
            .between(2, 5)
            .flatMap(size -> 
                Arbitraries.strings()
                    .withCharRange('a', 'z')
                    .ofMinLength(10)
                    .ofMaxLength(100)
                    .list()
                    .ofSize(size)
                    .map(list -> {
                        List<String> result = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            result.add("Level " + i + " comment: " + list.get(i));
                        }
                        return result;
                    })
            );
    }
}