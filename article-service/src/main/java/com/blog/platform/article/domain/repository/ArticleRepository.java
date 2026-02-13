package com.blog.platform.article.domain.repository;

import com.blog.platform.common.domain.article.Article;
import com.blog.platform.common.domain.article.ArticleId;
import com.blog.platform.common.domain.article.ArticleStatus;
import com.blog.platform.common.domain.user.UserId;
import com.blog.platform.common.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 文章领域仓储接口
 */
public interface ArticleRepository extends Repository<Article, ArticleId> {
    
    /**
     * 保存文章
     */
    Article save(Article article);
    
    /**
     * 根据ID查找文章
     */
    Optional<Article> findById(ArticleId articleId);
    
    /**
     * 根据作者ID查找文章
     */
    List<Article> findByAuthorId(UserId authorId);
    
    /**
     * 根据作者ID分页查找文章
     */
    Page<Article> findByAuthorId(UserId authorId, Pageable pageable);
    
    /**
     * 根据状态查找文章
     */
    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);
    
    /**
     * 根据作者ID和状态查找文章
     */
    Page<Article> findByAuthorIdAndStatus(UserId authorId, ArticleStatus status, Pageable pageable);
    
    /**
     * 查找已发布的文章
     */
    Page<Article> findPublishedArticles(Pageable pageable);
    
    /**
     * 根据作者查找已发布的文章
     */
    Page<Article> findPublishedArticlesByAuthor(UserId authorId, Pageable pageable);
    
    /**
     * 删除文章
     */
    void delete(Article article);
    
    /**
     * 根据ID删除文章
     */
    void deleteById(ArticleId articleId);
    
    /**
     * 检查文章是否存在
     */
    boolean existsById(ArticleId articleId);
    
    /**
     * 统计作者的文章数量
     */
    long countByAuthorId(UserId authorId);
    
    /**
     * 统计已发布文章数量
     */
    long countPublishedArticlesByAuthor(UserId authorId);
}