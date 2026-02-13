package com.blog.platform.comment.interfaces.rest;

import com.blog.platform.comment.application.dto.*;
import com.blog.platform.comment.application.service.CommentApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {
    
    private final CommentApplicationService commentApplicationService;
    
    @Autowired
    public CommentController(CommentApplicationService commentApplicationService) {
        this.commentApplicationService = commentApplicationService;
    }
    
    /**
     * 创建评论
     */
    @PostMapping
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CreateCommentRequest request) {
        CommentDto comment = commentApplicationService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
    
    /**
     * 回复评论
     */
    @PostMapping("/{commentId}/reply")
    public ResponseEntity<CommentDto> replyComment(
            @PathVariable String commentId,
            @Valid @RequestBody ReplyCommentRequest request) {
        CommentDto reply = commentApplicationService.replyComment(commentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }
    
    /**
     * 获取评论详情
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable String commentId) {
        CommentDto comment = commentApplicationService.getCommentById(commentId);
        return ResponseEntity.ok(comment);
    }
    
    /**
     * 获取文章的评论列表（分页）
     */
    @GetMapping("/article/{articleId}")
    public ResponseEntity<PageResult<CommentDto>> getCommentsByArticle(
            @PathVariable String articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<CommentDto> comments = commentApplicationService.getCommentsByArticleId(articleId, page, size);
        return ResponseEntity.ok(comments);
    }
    
    /**
     * 获取文章的评论树形结构
     */
    @GetMapping("/article/{articleId}/tree")
    public ResponseEntity<List<CommentTreeDto>> getCommentTreeByArticle(@PathVariable String articleId) {
        List<CommentTreeDto> commentTree = commentApplicationService.getCommentTreeByArticleId(articleId);
        return ResponseEntity.ok(commentTree);
    }
    
    /**
     * 获取用户的评论列表（分页）
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<PageResult<CommentDto>> getCommentsByAuthor(
            @PathVariable String authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<CommentDto> comments = commentApplicationService.getCommentsByAuthorId(authorId, page, size);
        return ResponseEntity.ok(comments);
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String commentId,
            @RequestParam String userId) {
        commentApplicationService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Comment Service is running");
    }
}