package com.blog.platform.comment.domain.repository;

import com.blog.platform.common.domain.article.ArticleId;
import com.blog.platform.common.domain.comment.Comment;
import com.blog.platform.common.domain.comment.CommentId;
import com.blog.platform.common.domain.user.UserId;
import com.blog.platform.common.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评论仓储接口
 */
public interface CommentRepository extends Repository<Comment, CommentId> {
    
    /**
     * 根据ID查找评论
     */
    Optional<Comment> findById(CommentId commentId);
    
    /**
     * 保存评论
     */
    Comment save(Comment comment);
    
    /**
     * 删除评论
     */
    void delete(CommentId commentId);
    
    /**
     * 根据文章ID查找所有评论
     */
    List<Comment> findByArticleId(ArticleId articleId);
    
    /**
     * 根据文章ID查找顶级评论（无父评论）
     */
    List<Comment> findRootCommentsByArticleId(ArticleId articleId);
    
    /**
     * 根据父评论ID查找回复
     */
    List<Comment> findRepliesByParentId(CommentId parentId);
    
    /**
     * 根据作者ID查找评论
     */
    List<Comment> findByAuthorId(UserId authorId);
    
    /**
     * 统计文章的评论数量
     */
    long countByArticleId(ArticleId articleId);
    
    /**
     * 统计评论的回复数量
     */
    long countRepliesByParentId(CommentId parentId);
    
    /**
     * 检查评论是否存在
     */
    boolean existsById(CommentId commentId);
    
    /**
     * 检查用户是否对文章有评论
     */
    boolean existsByArticleIdAndAuthorId(ArticleId articleId, UserId authorId);
}