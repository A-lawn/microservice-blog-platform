package com.blog.platform.comment.infrastructure.elasticsearch.service;

import com.blog.platform.comment.infrastructure.elasticsearch.model.CommentReadModel;
import com.blog.platform.comment.infrastructure.elasticsearch.repository.CommentReadModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(name = "comment-service.enableSearch", havingValue = "true")
public class CommentSearchService {
    
    @Autowired
    private CommentReadModelRepository readModelRepository;
    
    /**
     * Get comments for an article
     */
    public Page<CommentReadModel> getArticleComments(String articleId, Pageable pageable) {
        return readModelRepository.findByArticleIdAndStatus(articleId, "ACTIVE", pageable);
    }
    
    /**
     * Get root comments for an article (no parent)
     */
    public Page<CommentReadModel> getRootComments(String articleId, Pageable pageable) {
        return readModelRepository.findRootCommentsByArticleIdAndStatus(articleId, "ACTIVE", pageable);
    }
    
    /**
     * Get replies to a specific comment
     */
    public Page<CommentReadModel> getCommentReplies(String parentId, Pageable pageable) {
        return readModelRepository.findByParentIdAndStatus(parentId, "ACTIVE", pageable);
    }
    
    /**
     * Get comments by author
     */
    public Page<CommentReadModel> getCommentsByAuthor(String authorId, Pageable pageable) {
        return readModelRepository.findByAuthorId(authorId, pageable);
    }
    
    /**
     * Search comments by content
     */
    public Page<CommentReadModel> searchComments(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return readModelRepository.findByStatusAndCreatedAtBetween("ACTIVE", 
                LocalDateTime.now().minusYears(1), LocalDateTime.now(), pageable);
        }
        return readModelRepository.searchByContent(keyword.trim(), pageable);
    }
    
    /**
     * Get popular comments (high like count)
     */
    public Page<CommentReadModel> getPopularComments(Integer minLikeCount, Pageable pageable) {
        return readModelRepository.findPopularCommentsByLikeCount(minLikeCount, pageable);
    }
    
    /**
     * Get recent comments for an article
     */
    public Page<CommentReadModel> getRecentComments(String articleId, Pageable pageable) {
        return readModelRepository.findByArticleIdAndStatusOrderByCreatedAtDesc(articleId, "ACTIVE", pageable);
    }
    
    /**
     * Get comments in a time range
     */
    public Page<CommentReadModel> getCommentsInTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return readModelRepository.findByStatusAndCreatedAtBetween("ACTIVE", startTime, endTime, pageable);
    }
    
    /**
     * Get comment thread (parent and all descendants)
     */
    public List<CommentReadModel> getCommentThread(String commentId) {
        return readModelRepository.findCommentThread(commentId);
    }
    
    /**
     * Get comments by hierarchy level
     */
    public Page<CommentReadModel> getCommentsByLevel(String articleId, Integer level, Pageable pageable) {
        return readModelRepository.findByArticleIdAndStatusAndLevel(articleId, "ACTIVE", level, pageable);
    }
    
    /**
     * Get most replied comments
     */
    public Page<CommentReadModel> getMostRepliedComments(Integer minReplyCount, Pageable pageable) {
        return readModelRepository.findMostRepliedComments(minReplyCount, pageable);
    }
    
    /**
     * Get comment statistics for an article
     */
    public CommentSearchStats getCommentStats(String articleId) {
        long totalComments = readModelRepository.countByArticleIdAndStatus(articleId, "ACTIVE");
        
        // Get root comments count
        Page<CommentReadModel> rootComments = readModelRepository.findRootCommentsByArticleIdAndStatus(
                articleId, "ACTIVE", Pageable.ofSize(1));
        long rootCommentsCount = rootComments.getTotalElements();
        
        return new CommentSearchStats(totalComments, rootCommentsCount, totalComments - rootCommentsCount);
    }
    
    /**
     * Get global comment statistics
     */
    public CommentSearchStats getGlobalCommentStats() {
        long totalActive = readModelRepository.countByAuthorIdAndStatus("", "ACTIVE");
        long totalHidden = readModelRepository.countByAuthorIdAndStatus("", "HIDDEN");
        long totalDeleted = readModelRepository.countByAuthorIdAndStatus("", "DELETED");
        
        return new CommentSearchStats(totalActive, totalHidden, totalDeleted);
    }
    
    /**
     * Comment search statistics DTO
     */
    public static class CommentSearchStats {
        private final long totalCount;
        private final long rootCount;
        private final long replyCount;
        
        public CommentSearchStats(long totalCount, long rootCount, long replyCount) {
            this.totalCount = totalCount;
            this.rootCount = rootCount;
            this.replyCount = replyCount;
        }
        
        public long getTotalCount() { return totalCount; }
        public long getRootCount() { return rootCount; }
        public long getReplyCount() { return replyCount; }
    }
}