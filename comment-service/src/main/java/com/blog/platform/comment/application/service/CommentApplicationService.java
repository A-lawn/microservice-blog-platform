package com.blog.platform.comment.application.service;

import com.blog.platform.comment.application.dto.*;
import com.blog.platform.comment.application.saga.CommentPublishSaga;
import com.blog.platform.comment.domain.exception.CommentDomainException;
import com.blog.platform.comment.domain.repository.CommentRepository;
import com.blog.platform.comment.infrastructure.persistence.entity.CommentEntity;
import com.blog.platform.comment.infrastructure.persistence.repository.CommentJpaRepository;
import com.blog.platform.comment.infrastructure.messaging.CommentEventPublisher;
import com.blog.platform.common.domain.article.ArticleId;
import com.blog.platform.common.domain.comment.*;
import com.blog.platform.common.domain.user.UserId;
import com.blog.platform.common.domain.DomainEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论应用服务
 */
@Service
@Transactional
public class CommentApplicationService {
    
    private final CommentRepository commentRepository;
    private final CommentJpaRepository commentJpaRepository;
    private final DomainEventPublisher eventPublisher;
    private final Optional<CommentPublishSaga> commentPublishSaga;
    private final CommentEventPublisher commentEventPublisher;
    
    @Autowired
    public CommentApplicationService(CommentRepository commentRepository,
                                   CommentJpaRepository commentJpaRepository,
                                   DomainEventPublisher eventPublisher,
                                   java.util.Optional<CommentPublishSaga> commentPublishSaga,
                                   CommentEventPublisher commentEventPublisher) {
        this.commentRepository = commentRepository;
        this.commentJpaRepository = commentJpaRepository;
        this.eventPublisher = eventPublisher;
        this.commentPublishSaga = commentPublishSaga != null ? commentPublishSaga : Optional.empty();
        this.commentEventPublisher = commentEventPublisher;
    }
    
    /**
     * 创建评论：标准版本地事务；高级版（feature.seata.enabled=true）可走 Saga 协调跨服务
     */
    public CommentDto createComment(CreateCommentRequest request) {
        validateCreateCommentRequest(request);
        
        ArticleId articleId = new ArticleId(request.getArticleId());
        UserId authorId = new UserId(request.getAuthorId());
        CommentContent content = new CommentContent(request.getContent());
        
        Comment comment = Comment.create(articleId, authorId, content);
        Comment savedComment = commentRepository.save(comment);
        
        eventPublisher.publishEvents(savedComment);
        
        CommentCreatedEvent event = new CommentCreatedEvent(
            savedComment.getId().getValue(),
            savedComment.getArticleId().getValue(),
            savedComment.getAuthorId().getValue(),
            savedComment.getParentId() != null ? savedComment.getParentId().getValue() : null,
            savedComment.getContent().getValue()
        );
        commentEventPublisher.publishCommentCreatedEvent(event);
        
        if (commentPublishSaga != null && commentPublishSaga.isPresent()) {
            try {
                commentPublishSaga.get().publishComment(
                    savedComment.getId().getValue(),
                    request.getArticleId(),
                    request.getAuthorId()
                );
            } catch (Exception e) {
                throw new CommentDomainException("COMMENT_PUBLISH_FAILED", "评论发布失败: " + e.getMessage());
            }
        }
        
        return toCommentDto(savedComment);
    }
    
    /**
     * 回复评论：标准版本地事务；高级版可走 Saga
     */
    public CommentDto replyComment(String parentCommentId, ReplyCommentRequest request) {
        validateReplyCommentRequest(request);
        
        CommentId parentId = new CommentId(parentCommentId);
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new CommentDomainException.CommentNotFoundException(parentCommentId));
        
        if (!parentComment.canReply()) {
            throw new CommentDomainException.CommentNotReplyableException(parentCommentId);
        }
        
        ArticleId articleId = new ArticleId(request.getArticleId());
        UserId authorId = new UserId(request.getAuthorId());
        CommentContent content = new CommentContent(request.getContent());
        
        Comment replyComment = Comment.createReply(articleId, authorId, content, parentId);
        Comment savedComment = commentRepository.save(replyComment);
        
        eventPublisher.publishEvents(savedComment);
        
        CommentCreatedEvent event = new CommentCreatedEvent(
            savedComment.getId().getValue(),
            savedComment.getArticleId().getValue(),
            savedComment.getAuthorId().getValue(),
            savedComment.getParentId() != null ? savedComment.getParentId().getValue() : null,
            savedComment.getContent().getValue()
        );
        commentEventPublisher.publishCommentCreatedEvent(event);
        
        if (commentPublishSaga != null && commentPublishSaga.isPresent()) {
            try {
                commentPublishSaga.get().publishComment(
                    savedComment.getId().getValue(),
                    request.getArticleId(),
                    request.getAuthorId()
                );
            } catch (Exception e) {
                throw new CommentDomainException("COMMENT_PUBLISH_FAILED", "回复评论发布失败: " + e.getMessage());
            }
        }
        
        return toCommentDto(savedComment);
    }
    
    /**
     * 获取文章的评论列表（分页）
     */
    @Transactional(readOnly = true)
    public PageResult<CommentDto> getCommentsByArticleId(String articleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentEntity> commentPage = commentJpaRepository.findActiveCommentsByArticleId(articleId, pageable);
        
        List<CommentDto> commentDtos = commentPage.getContent().stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
        
        return new PageResult<>(commentDtos, page, size, commentPage.getTotalElements());
    }
    
    /**
     * 获取文章的评论树形结构
     */
    @Transactional(readOnly = true)
    public List<CommentTreeDto> getCommentTreeByArticleId(String articleId) {
        // 获取所有活跃评论
        List<CommentEntity> allComments = commentJpaRepository.findActiveCommentsByArticleId(articleId);
        
        // 构建评论树
        return buildCommentTree(allComments);
    }
    
    /**
     * 获取评论详情
     */
    @Transactional(readOnly = true)
    public CommentDto getCommentById(String commentId) {
        CommentId id = new CommentId(commentId);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentDomainException.CommentNotFoundException(commentId));
        
        return toCommentDto(comment);
    }
    
    /**
     * 获取用户的评论列表（分页）
     */
    @Transactional(readOnly = true)
    public PageResult<CommentDto> getCommentsByAuthorId(String authorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentEntity> commentPage = commentJpaRepository.findByAuthorId(authorId, pageable);
        
        List<CommentDto> commentDtos = commentPage.getContent().stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
        
        return new PageResult<>(commentDtos, page, size, commentPage.getTotalElements());
    }
    
    /**
     * 删除评论
     */
    public void deleteComment(String commentId, String userId) {
        CommentId id = new CommentId(commentId);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentDomainException.CommentNotFoundException(commentId));
        
        // 检查权限（只有作者可以删除自己的评论）
        UserId authorId = new UserId(userId);
        if (!comment.isAuthor(authorId)) {
            throw new CommentDomainException.UnauthorizedCommentOperationException("删除评论");
        }
        
        // 删除评论
        comment.delete("用户删除");
        
        // 保存更改
        Comment savedComment = commentRepository.save(comment);
        
        // 发布领域事件
        eventPublisher.publishEvents(savedComment);
        
        // 发布评论删除事件
        CommentDeletedEvent event = new CommentDeletedEvent(
            savedComment.getId().getValue(),
            savedComment.getArticleId().getValue(),
            savedComment.getAuthorId().getValue(),
            savedComment.getParentId() != null ? savedComment.getParentId().getValue() : null,
            "用户删除"
        );
        commentEventPublisher.publishCommentDeletedEvent(event);
    }
    
    /**
     * 审核评论
     */
    public void moderateComment(String commentId, String moderatorId, CommentStatus newStatus, String reason) {
        CommentId id = new CommentId(commentId);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentDomainException.CommentNotFoundException(commentId));
        
        // 执行审核操作
        if (newStatus == CommentStatus.ACTIVE) {
            comment.approve(UserId.of(moderatorId));
        } else if (newStatus == CommentStatus.HIDDEN) {
            comment.hide(UserId.of(moderatorId), reason);
        } else {
            comment.restore(UserId.of(moderatorId), reason);
        }
        
        // 保存更改
        Comment savedComment = commentRepository.save(comment);
        
        // 发布领域事件
        eventPublisher.publishEvents(savedComment);
        
        // 发布评论审核事件
        CommentModeratedEvent event = new CommentModeratedEvent(
            savedComment.getId().getValue(),
            savedComment.getArticleId().getValue(),
            savedComment.getAuthorId().getValue(),
            moderatorId,
            comment.getStatus(), // 旧状态
            newStatus, // 新状态
            reason
        );
        commentEventPublisher.publishCommentModeratedEvent(event);
    }
    
    /**
     * 构建评论树形结构
     */
    private List<CommentTreeDto> buildCommentTree(List<CommentEntity> comments) {
        // 按父子关系分组
        Map<String, List<CommentEntity>> commentsByParent = comments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getParentId() != null ? comment.getParentId() : "ROOT"
                ));
        
        // 获取根评论
        List<CommentEntity> rootComments = commentsByParent.getOrDefault("ROOT", new ArrayList<>());
        
        // 构建树形结构
        return rootComments.stream()
                .map(comment -> buildCommentTreeNode(comment, commentsByParent, 0))
                .collect(Collectors.toList());
    }
    
    /**
     * 构建评论树节点
     */
    private CommentTreeDto buildCommentTreeNode(CommentEntity comment, 
                                              Map<String, List<CommentEntity>> commentsByParent, 
                                              int level) {
        CommentTreeDto node = toCommentTreeDto(comment, level);
        
        // 获取子评论
        List<CommentEntity> children = commentsByParent.getOrDefault(comment.getId(), new ArrayList<>());
        
        // 递归构建子节点
        List<CommentTreeDto> childNodes = children.stream()
                .map(child -> buildCommentTreeNode(child, commentsByParent, level + 1))
                .collect(Collectors.toList());
        
        node.setChildren(childNodes);
        
        return node;
    }
    
    /**
     * 验证创建评论请求
     */
    private void validateCreateCommentRequest(CreateCommentRequest request) {
        if (request.getArticleId() == null || request.getArticleId().trim().isEmpty()) {
            throw new CommentDomainException.InvalidCommentContentException("文章ID不能为空");
        }
        if (request.getAuthorId() == null || request.getAuthorId().trim().isEmpty()) {
            throw new CommentDomainException.InvalidCommentContentException("作者ID不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new CommentDomainException.InvalidCommentContentException("评论内容不能为空");
        }
        if (request.getContent().length() > 5000) {
            throw new CommentDomainException.InvalidCommentContentException("评论内容不能超过5000字符");
        }
    }
    
    /**
     * 验证回复评论请求
     */
    private void validateReplyCommentRequest(ReplyCommentRequest request) {
        if (request.getArticleId() == null || request.getArticleId().trim().isEmpty()) {
            throw new CommentDomainException.InvalidCommentContentException("文章ID不能为空");
        }
        if (request.getAuthorId() == null || request.getAuthorId().trim().isEmpty()) {
            throw new CommentDomainException.InvalidCommentContentException("作者ID不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new CommentDomainException.InvalidCommentContentException("评论内容不能为空");
        }
        if (request.getContent().length() > 5000) {
            throw new CommentDomainException.InvalidCommentContentException("评论内容不能超过5000字符");
        }
    }
    
    /**
     * 转换为CommentDto
     */
    private CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId().getValue());
        dto.setArticleId(comment.getArticleId().getValue());
        dto.setAuthorId(comment.getAuthorId().getValue());
        dto.setContent(comment.getContent().getValue());
        dto.setParentId(comment.getParentId() != null ? comment.getParentId().getValue() : null);
        dto.setStatus(comment.getStatus());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        
        // 统计信息（这里简化处理，实际应该从统计表获取）
        dto.setLikeCount(0);
        dto.setReplyCount(0);
        
        return dto;
    }
    
    /**
     * 转换为CommentDto（从实体）
     */
    private CommentDto toCommentDto(CommentEntity entity) {
        CommentDto dto = new CommentDto();
        dto.setId(entity.getId());
        dto.setArticleId(entity.getArticleId());
        dto.setAuthorId(entity.getAuthorId());
        dto.setContent(entity.getContent());
        dto.setParentId(entity.getParentId());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        // 统计信息（这里简化处理）
        dto.setLikeCount(0);
        dto.setReplyCount(0);
        
        return dto;
    }
    
    /**
     * 转换为CommentTreeDto
     */
    private CommentTreeDto toCommentTreeDto(CommentEntity entity, int level) {
        CommentTreeDto dto = new CommentTreeDto();
        dto.setId(entity.getId());
        dto.setArticleId(entity.getArticleId());
        dto.setAuthorId(entity.getAuthorId());
        dto.setContent(entity.getContent());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setLevel(level);
        
        // 统计信息（这里简化处理）
        dto.setLikeCount(0);
        
        return dto;
    }
}