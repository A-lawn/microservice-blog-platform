package com.blog.platform.common.domain.article;

import com.blog.platform.common.domain.AggregateRoot;
import com.blog.platform.common.domain.user.UserId;

import java.time.LocalDateTime;

/**
 * 文章聚合根
 */
public class Article extends AggregateRoot<ArticleId> {
    
    private final ArticleId id;
    private final UserId authorId;
    private ArticleTitle title;
    private ArticleContent content;
    private ArticleStatus status;
    private LocalDateTime publishTime;
    private ArticleStatistics statistics;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 私有构造函数，强制使用工厂方法
    private Article(ArticleId id, UserId authorId, ArticleTitle title, ArticleContent content,
                   ArticleStatus status, LocalDateTime publishTime, ArticleStatistics statistics,
                   LocalDateTime createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.publishTime = publishTime;
        this.statistics = statistics;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
    
    /**
     * 创建文章工厂方法
     */
    public static Article create(UserId authorId, ArticleTitle title, ArticleContent content) {
        if (authorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        if (title == null) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        if (content == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        
        ArticleId articleId = ArticleId.generate();
        ArticleStatus status = ArticleStatus.DRAFT;
        ArticleStatistics statistics = ArticleStatistics.empty();
        LocalDateTime now = LocalDateTime.now();
        
        Article article = new Article(articleId, authorId, title, content, status, 
                                    null, statistics, now);
        
        // 发布文章创建事件
        article.addDomainEvent(new ArticleCreatedEvent(
            articleId.getValue(),
            authorId.getValue(),
            title.getValue()
        ));
        
        return article;
    }
    
    /**
     * 从持久化数据重建文章聚合根
     */
    public static Article reconstruct(ArticleId id, UserId authorId, ArticleTitle title, 
                                    ArticleContent content, ArticleStatus status, 
                                    LocalDateTime publishTime, ArticleStatistics statistics,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        Article article = new Article(id, authorId, title, content, status, 
                                    publishTime, statistics, createdAt);
        article.updatedAt = updatedAt;
        return article;
    }
    
    /**
     * 发布文章
     */
    public void publish() {
        if (!status.canPublish()) {
            throw new IllegalStateException("当前状态的文章不能发布: " + status);
        }
        
        this.status = ArticleStatus.PUBLISHED;
        this.publishTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // 发布文章发布事件
        addDomainEvent(new ArticlePublishedEvent(
            id.getValue(),
            authorId.getValue(),
            title.getValue(),
            publishTime
        ));
    }
    
    /**
     * 更新文章内容
     */
    public void updateContent(ArticleContent newContent) {
        if (newContent == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        
        if (!status.canEdit()) {
            throw new IllegalStateException("当前状态的文章不能编辑: " + status);
        }
        
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
        
        // 发布文章更新事件
        addDomainEvent(new ArticleUpdatedEvent(
            id.getValue(),
            authorId.getValue(),
            title.getValue(),
            "CONTENT"
        ));
    }
    
    /**
     * 更新文章标题
     */
    public void updateTitle(ArticleTitle newTitle) {
        if (newTitle == null) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        
        if (!status.canEdit()) {
            throw new IllegalStateException("当前状态的文章不能编辑: " + status);
        }
        
        this.title = newTitle;
        this.updatedAt = LocalDateTime.now();
        
        // 发布文章更新事件
        addDomainEvent(new ArticleUpdatedEvent(
            id.getValue(),
            authorId.getValue(),
            title.getValue(),
            "TITLE"
        ));
    }
    
    /**
     * 归档文章
     */
    public void archive() {
        if (!status.canArchive()) {
            throw new IllegalStateException("当前状态的文章不能归档: " + status);
        }
        
        this.status = ArticleStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
        
        // 发布文章归档事件
        addDomainEvent(new ArticleArchivedEvent(
            id.getValue(),
            authorId.getValue(),
            title.getValue()
        ));
    }
    
    /**
     * 增加浏览次数
     */
    public void incrementViewCount() {
        if (!status.isVisible()) {
            return; // 不可见的文章不增加浏览次数
        }
        
        this.statistics = statistics.incrementViewCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加点赞次数
     */
    public void incrementLikeCount() {
        if (!status.isVisible()) {
            throw new IllegalStateException("不可见的文章不能点赞");
        }
        
        this.statistics = statistics.incrementLikeCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 减少点赞次数
     */
    public void decrementLikeCount() {
        if (!status.isVisible()) {
            throw new IllegalStateException("不可见的文章不能取消点赞");
        }
        
        this.statistics = statistics.decrementLikeCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加评论次数
     */
    public void incrementCommentCount() {
        if (!status.isVisible()) {
            return; // 不可见的文章不增加评论次数
        }
        
        this.statistics = statistics.incrementCommentCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 减少评论次数
     */
    public void decrementCommentCount() {
        if (!status.isVisible()) {
            return; // 不可见的文章不减少评论次数
        }
        
        this.statistics = statistics.decrementCommentCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加分享次数
     */
    public void incrementShareCount() {
        if (!status.isVisible()) {
            throw new IllegalStateException("不可见的文章不能分享");
        }
        
        this.statistics = statistics.incrementShareCount();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查是否是作者
     */
    public boolean isAuthor(UserId userId) {
        return this.authorId.equals(userId);
    }
    
    /**
     * 检查文章是否可见
     */
    public boolean isVisible() {
        return status.isVisible();
    }
    
    /**
     * 检查文章是否可以编辑
     */
    public boolean canEdit() {
        return status.canEdit();
    }
    
    /**
     * 检查文章是否可以发布
     */
    public boolean canPublish() {
        return status.canPublish();
    }
    
    /**
     * 检查文章是否可以归档
     */
    public boolean canArchive() {
        return status.canArchive();
    }
    
    // Getters
    @Override
    public ArticleId getId() {
        return id;
    }
    
    public UserId getAuthorId() {
        return authorId;
    }
    
    public ArticleTitle getTitle() {
        return title;
    }
    
    public ArticleContent getContent() {
        return content;
    }
    
    public ArticleStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getPublishTime() {
        return publishTime;
    }
    
    public ArticleStatistics getStatistics() {
        return statistics;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}