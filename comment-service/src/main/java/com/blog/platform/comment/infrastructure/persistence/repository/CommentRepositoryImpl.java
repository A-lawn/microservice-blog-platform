package com.blog.platform.comment.infrastructure.persistence.repository;

import com.blog.platform.comment.domain.repository.CommentRepository;
import com.blog.platform.comment.infrastructure.persistence.entity.CommentEntity;
import com.blog.platform.common.domain.article.ArticleId;
import com.blog.platform.common.domain.comment.*;
import com.blog.platform.common.domain.user.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 评论仓储实现
 */
@Repository
public class CommentRepositoryImpl implements CommentRepository {
    
    private final CommentJpaRepository commentJpaRepository;
    
    @Autowired
    public CommentRepositoryImpl(CommentJpaRepository commentJpaRepository) {
        this.commentJpaRepository = commentJpaRepository;
    }
    
    @Override
    public Optional<Comment> findById(CommentId commentId) {
        return commentJpaRepository.findById(commentId.getValue())
                .map(this::toDomainModel);
    }
    
    @Override
    public Comment save(Comment comment) {
        CommentEntity entity = toEntity(comment);
        CommentEntity savedEntity = commentJpaRepository.save(entity);
        return toDomainModel(savedEntity);
    }
    
    @Override
    public void delete(CommentId commentId) {
        commentJpaRepository.deleteById(commentId.getValue());
    }
    
    @Override
    public long count() {
        return commentJpaRepository.count();
    }
    
    @Override
    public List<Comment> findAll() {
        return commentJpaRepository.findAll()
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }
    
    @Override
    public void delete(Comment comment) {
        commentJpaRepository.deleteById(comment.getId().getValue());
    }
    
    @Override
    public void deleteById(CommentId commentId) {
        commentJpaRepository.deleteById(commentId.getValue());
    }
    
    @Override
    public List<Comment> findByArticleId(ArticleId articleId) {
        return commentJpaRepository.findActiveCommentsByArticleId(articleId.getValue())
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Comment> findRootCommentsByArticleId(ArticleId articleId) {
        return commentJpaRepository.findRootCommentsByArticleId(articleId.getValue())
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Comment> findRepliesByParentId(CommentId parentId) {
        return commentJpaRepository.findActiveRepliesByParentId(parentId.getValue())
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Comment> findByAuthorId(UserId authorId) {
        return commentJpaRepository.findByAuthorId(authorId.getValue())
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countByArticleId(ArticleId articleId) {
        return commentJpaRepository.countActiveCommentsByArticleId(articleId.getValue());
    }
    
    @Override
    public long countRepliesByParentId(CommentId parentId) {
        return commentJpaRepository.countActiveRepliesByParentId(parentId.getValue());
    }
    
    @Override
    public boolean existsById(CommentId commentId) {
        return commentJpaRepository.existsById(commentId.getValue());
    }
    
    @Override
    public boolean existsByArticleIdAndAuthorId(ArticleId articleId, UserId authorId) {
        return commentJpaRepository.findByArticleId(articleId.getValue())
                .stream()
                .anyMatch(entity -> entity.getAuthorId().equals(authorId.getValue()));
    }
    
    /**
     * 将实体转换为领域模型
     */
    private Comment toDomainModel(CommentEntity entity) {
        CommentId commentId = new CommentId(entity.getId());
        ArticleId articleId = new ArticleId(entity.getArticleId());
        UserId authorId = new UserId(entity.getAuthorId());
        CommentContent content = new CommentContent(entity.getContent());
        CommentId parentId = entity.getParentId() != null ? new CommentId(entity.getParentId()) : null;
        CommentStatus status = entity.getStatus();
        
        return Comment.reconstruct(
                commentId,
                articleId,
                authorId,
                content,
                parentId,
                status,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    /**
     * 将领域模型转换为实体
     */
    private CommentEntity toEntity(Comment comment) {
        CommentEntity entity = new CommentEntity();
        entity.setId(comment.getId().getValue());
        entity.setArticleId(comment.getArticleId().getValue());
        entity.setAuthorId(comment.getAuthorId().getValue());
        entity.setContent(comment.getContent().getValue());
        entity.setParentId(comment.getParentId() != null ? comment.getParentId().getValue() : null);
        entity.setStatus(comment.getStatus());
        entity.setCreatedAt(comment.getCreatedAt());
        entity.setUpdatedAt(comment.getUpdatedAt());
        
        return entity;
    }
}