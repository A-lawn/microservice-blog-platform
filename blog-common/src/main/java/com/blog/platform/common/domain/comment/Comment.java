package com.blog.platform.common.domain.comment;

import com.blog.platform.common.domain.AggregateRoot;
import com.blog.platform.common.domain.article.ArticleId;
import com.blog.platform.common.domain.user.UserId;

import java.time.LocalDateTime;

/**
 * 评论聚合根
 */
public class Comment extends AggregateRoot<CommentId> {
    
    private final CommentId id;
    private final ArticleId articleId;
    private final UserId authorId;
    private CommentContent content;
    private final CommentId parentId; // 父评论ID，支持回复层级
    private CommentStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 私有构造函数，强制使用工厂方法
    private Comment(CommentId id, ArticleId articleId, UserId authorId, 
                   CommentContent content, CommentId parentId, CommentStatus status,
                   LocalDateTime createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.authorId = authorId;
        this.content = content;
        this.parentId = parentId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
    
    /**
     * 创建评论工厂方法
     */
    public static Comment create(ArticleId articleId, UserId authorId, CommentContent content) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        if (content == null) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        
        CommentId commentId = CommentId.generate();
        CommentStatus status = CommentStatus.ACTIVE; // 默认为活跃状态
        LocalDateTime now = LocalDateTime.now();
        
        Comment comment = new Comment(commentId, articleId, authorId, content, 
                                    null, status, now);
        
        // 发布评论创建事件
        comment.addDomainEvent(new CommentCreatedEvent(
            commentId.getValue(),
            articleId.getValue(),
            authorId.getValue(),
            null,
            content.getValue()
        ));
        
        return comment;
    }
    
    /**
     * 创建回复评论工厂方法
     */
    public static Comment createReply(ArticleId articleId, UserId authorId, 
                                    CommentContent content, CommentId parentId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        if (content == null) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (parentId == null) {
            throw new IllegalArgumentException("父评论ID不能为空");
        }
        
        CommentId commentId = CommentId.generate();
        CommentStatus status = CommentStatus.ACTIVE; // 默认为活跃状态
        LocalDateTime now = LocalDateTime.now();
        
        Comment comment = new Comment(commentId, articleId, authorId, content, 
                                    parentId, status, now);
        
        // 发布评论创建事件（回复）
        comment.addDomainEvent(new CommentCreatedEvent(
            commentId.getValue(),
            articleId.getValue(),
            authorId.getValue(),
            parentId.getValue(),
            content.getValue()
        ));
        
        return comment;
    }
    
    /**
     * 从持久化数据重建评论聚合根
     */
    public static Comment reconstruct(CommentId id, ArticleId articleId, UserId authorId,
                                    CommentContent content, CommentId parentId, 
                                    CommentStatus status, LocalDateTime createdAt, 
                                    LocalDateTime updatedAt) {
        Comment comment = new Comment(id, articleId, authorId, content, parentId, 
                                    status, createdAt);
        comment.updatedAt = updatedAt;
        return comment;
    }
    
    /**
     * 更新评论内容
     */
    public void updateContent(CommentContent newContent) {
        if (newContent == null) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        
        if (!status.canEdit()) {
            throw new IllegalStateException("当前状态的评论不能编辑: " + status);
        }
        
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 删除评论
     */
    public void delete(String reason) {
        if (!status.canDelete()) {
            throw new IllegalStateException("当前状态的评论不能删除: " + status);
        }
        
        this.status = CommentStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
        
        // 发布评论删除事件
        addDomainEvent(new CommentDeletedEvent(
            id.getValue(),
            articleId.getValue(),
            authorId.getValue(),
            parentId != null ? parentId.getValue() : null,
            reason
        ));
    }
    
    /**
     * 审核评论 - 审核通过
     */
    public void approve(UserId moderatorId) {
        if (moderatorId == null) {
            throw new IllegalArgumentException("审核员ID不能为空");
        }
        
        if (!status.canApprove()) {
            throw new IllegalStateException("当前状态的评论不能审核通过: " + status);
        }
        
        CommentStatus oldStatus = this.status;
        this.status = CommentStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        
        // 发布评论审核事件
        addDomainEvent(new CommentModeratedEvent(
            id.getValue(),
            articleId.getValue(),
            authorId.getValue(),
            moderatorId.getValue(),
            oldStatus,
            this.status,
            "审核通过"
        ));
    }
    
    /**
     * 隐藏评论
     */
    public void hide(UserId moderatorId, String reason) {
        if (moderatorId == null) {
            throw new IllegalArgumentException("审核员ID不能为空");
        }
        
        if (!status.canHide()) {
            throw new IllegalStateException("当前状态的评论不能隐藏: " + status);
        }
        
        CommentStatus oldStatus = this.status;
        this.status = CommentStatus.HIDDEN;
        this.updatedAt = LocalDateTime.now();
        
        // 发布评论审核事件
        addDomainEvent(new CommentModeratedEvent(
            id.getValue(),
            articleId.getValue(),
            authorId.getValue(),
            moderatorId.getValue(),
            oldStatus,
            this.status,
            reason != null ? reason : "违规内容"
        ));
    }
    
    /**
     * 恢复评论
     */
    public void restore(UserId moderatorId, String reason) {
        if (moderatorId == null) {
            throw new IllegalArgumentException("审核员ID不能为空");
        }
        
        if (!status.canRestore()) {
            throw new IllegalStateException("当前状态的评论不能恢复: " + status);
        }
        
        CommentStatus oldStatus = this.status;
        this.status = CommentStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        
        // 发布评论审核事件
        addDomainEvent(new CommentModeratedEvent(
            id.getValue(),
            articleId.getValue(),
            authorId.getValue(),
            moderatorId.getValue(),
            oldStatus,
            this.status,
            reason != null ? reason : "恢复评论"
        ));
    }
    
    /**
     * 检查是否是作者
     */
    public boolean isAuthor(UserId userId) {
        return this.authorId.equals(userId);
    }
    
    /**
     * 检查是否是回复评论
     */
    public boolean isReply() {
        return parentId != null;
    }
    
    /**
     * 检查是否是顶级评论
     */
    public boolean isTopLevel() {
        return parentId == null;
    }
    
    /**
     * 检查评论是否可见
     */
    public boolean isVisible() {
        return status.isVisible();
    }
    
    /**
     * 检查评论是否可以编辑
     */
    public boolean canEdit() {
        return status.canEdit();
    }
    
    /**
     * 检查评论是否可以删除
     */
    public boolean canDelete() {
        return status.canDelete();
    }
    
    /**
     * 检查评论是否可以回复
     */
    public boolean canReply() {
        return status.canReply();
    }
    
    // Getters
    @Override
    public CommentId getId() {
        return id;
    }
    
    public ArticleId getArticleId() {
        return articleId;
    }
    
    public UserId getAuthorId() {
        return authorId;
    }
    
    public CommentContent getContent() {
        return content;
    }
    
    public CommentId getParentId() {
        return parentId;
    }
    
    public CommentStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}