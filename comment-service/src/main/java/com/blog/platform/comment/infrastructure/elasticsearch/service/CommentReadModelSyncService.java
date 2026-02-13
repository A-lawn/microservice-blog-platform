package com.blog.platform.comment.infrastructure.elasticsearch.service;

import com.blog.platform.comment.infrastructure.elasticsearch.model.CommentReadModel;
import com.blog.platform.comment.infrastructure.elasticsearch.repository.CommentReadModelRepository;
import com.blog.platform.comment.infrastructure.persistence.entity.CommentEntity;
import com.blog.platform.comment.infrastructure.persistence.repository.CommentJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "comment-service.enableSearch", havingValue = "true")
public class CommentReadModelSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentReadModelSyncService.class);
    
    @Autowired
    private CommentReadModelRepository readModelRepository;
    
    @Autowired
    private CommentJpaRepository commentJpaRepository;
    
    /**
     * Sync single comment to read model
     */
    @Transactional(readOnly = true)
    public void syncComment(String commentId) {
        try {
            Optional<CommentEntity> commentOpt = commentJpaRepository.findByIdWithStatistics(commentId);
            if (commentOpt.isPresent()) {
                CommentEntity comment = commentOpt.get();
                CommentReadModel readModel = convertToReadModel(comment);
                readModelRepository.save(readModel);
                logger.info("Synced comment {} to read model", commentId);
            } else {
                // Comment might have been deleted, remove from read model
                readModelRepository.deleteById(commentId);
                logger.info("Removed comment {} from read model", commentId);
            }
        } catch (Exception e) {
            logger.error("Failed to sync comment {} to read model", commentId, e);
        }
    }
    
    /**
     * Sync all comments to read model (for initial sync or recovery)
     */
    @Transactional(readOnly = true)
    public void syncAllComments() {
        try {
            logger.info("Starting full comment sync to read model");
            List<CommentEntity> comments = commentJpaRepository.findAll();
            
            for (CommentEntity comment : comments) {
                try {
                    CommentReadModel readModel = convertToReadModel(comment);
                    readModelRepository.save(readModel);
                } catch (Exception e) {
                    logger.error("Failed to sync comment {} during full sync", comment.getId(), e);
                }
            }
            
            logger.info("Completed full comment sync, processed {} comments", comments.size());
        } catch (Exception e) {
            logger.error("Failed to complete full comment sync", e);
        }
    }
    
    /**
     * Remove comment from read model
     */
    public void removeComment(String commentId) {
        try {
            readModelRepository.deleteById(commentId);
            logger.info("Removed comment {} from read model", commentId);
        } catch (Exception e) {
            logger.error("Failed to remove comment {} from read model", commentId, e);
        }
    }
    
    /**
     * Update comment statistics in read model
     */
    public void updateCommentStatistics(String commentId, Long likeCount, Long replyCount) {
        try {
            Optional<CommentReadModel> readModelOpt = readModelRepository.findById(commentId);
            if (readModelOpt.isPresent()) {
                CommentReadModel readModel = readModelOpt.get();
                readModel.setLikeCount(likeCount);
                readModel.setReplyCount(replyCount);
                readModelRepository.save(readModel);
                logger.debug("Updated statistics for comment {} in read model", commentId);
            }
        } catch (Exception e) {
            logger.error("Failed to update statistics for comment {} in read model", commentId, e);
        }
    }
    
    /**
     * Convert JPA entity to read model
     */
    private CommentReadModel convertToReadModel(CommentEntity comment) {
        CommentReadModel readModel = new CommentReadModel();
        
        readModel.setId(comment.getId());
        readModel.setArticleId(comment.getArticleId());
        readModel.setAuthorId(comment.getAuthorId());
        readModel.setParentId(comment.getParentId());
        readModel.setContent(comment.getContent());
        readModel.setStatus(comment.getStatus().name());
        readModel.setCreatedAt(comment.getCreatedAt());
        readModel.setUpdatedAt(comment.getUpdatedAt());
        
        // Set statistics if available
        if (comment.getStatistics() != null) {
            readModel.setLikeCount(comment.getStatistics().getLikeCount());
            readModel.setReplyCount(comment.getStatistics().getReplyCount());
        }
        
        // Calculate hierarchy level and path
        calculateHierarchyInfo(readModel, comment);
        
        return readModel;
    }
    
    /**
     * Calculate hierarchy level and path for the comment
     */
    private void calculateHierarchyInfo(CommentReadModel readModel, CommentEntity comment) {
        if (comment.getParentId() == null) {
            // Root comment
            readModel.setLevel(0);
            readModel.setPath(comment.getId());
        } else {
            // Reply comment - need to find parent to calculate level and path
            try {
                Optional<CommentReadModel> parentReadModel = readModelRepository.findById(comment.getParentId());
                if (parentReadModel.isPresent()) {
                    CommentReadModel parent = parentReadModel.get();
                    readModel.setLevel(parent.getLevel() + 1);
                    readModel.setPath(parent.getPath() + "/" + comment.getId());
                } else {
                    // Parent not found in read model, try to get from JPA
                    Optional<CommentEntity> parentEntity = commentJpaRepository.findById(comment.getParentId());
                    if (parentEntity.isPresent()) {
                        readModel.setLevel(1); // Assume parent is root for now
                        readModel.setPath(comment.getParentId() + "/" + comment.getId());
                    } else {
                        // Orphaned comment, treat as root
                        readModel.setLevel(0);
                        readModel.setPath(comment.getId());
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to calculate hierarchy for comment {}, treating as root", comment.getId(), e);
                readModel.setLevel(0);
                readModel.setPath(comment.getId());
            }
        }
    }
}