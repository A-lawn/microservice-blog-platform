package com.blog.platform.article.interfaces.rest;

import com.blog.platform.article.application.dto.*;
import com.blog.platform.article.application.service.ArticleApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "*")
public class ArticleController {
    
    @Autowired
    private ArticleApplicationService articleService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ArticleDto>> createArticle(
            @RequestHeader(value = "X-User-Id", required = false) String authorId,
            @Valid @RequestBody CreateArticleRequest request) {
        
        ArticleDto article = articleService.createArticle(authorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(article));
    }
    
    @PutMapping("/{articleId}")
    public ResponseEntity<ApiResponse<ArticleDto>> updateArticle(
            @PathVariable String articleId,
            @RequestHeader(value = "X-User-Id", required = false) String authorId,
            @Valid @RequestBody UpdateArticleRequest request) {
        
        ArticleDto article = articleService.updateArticle(articleId, authorId, request);
        return ResponseEntity.ok(ApiResponse.success(article));
    }
    
    @PutMapping("/{articleId}/publish")
    public ResponseEntity<ApiResponse<Void>> publishArticle(
            @PathVariable String articleId,
            @RequestHeader(value = "X-User-Id", required = false) String authorId) {
        
        articleService.publishArticle(articleId, authorId);
        return ResponseEntity.ok(ApiResponse.success("发布成功", null));
    }
    
    @PutMapping("/{articleId}/archive")
    public ResponseEntity<ApiResponse<Void>> archiveArticle(
            @PathVariable String articleId,
            @RequestHeader(value = "X-User-Id", required = false) String authorId) {
        
        articleService.archiveArticle(articleId, authorId);
        return ResponseEntity.ok(ApiResponse.success("归档成功", null));
    }
    
    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(
            @PathVariable String articleId,
            @RequestHeader(value = "X-User-Id", required = false) String authorId) {
        
        articleService.deleteArticle(articleId, authorId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
    
    @GetMapping("/{articleId}")
    public ResponseEntity<ApiResponse<ArticleDto>> getArticle(@PathVariable String articleId) {
        ArticleDto article = articleService.getArticleDetail(articleId);
        return ResponseEntity.ok(ApiResponse.success(article));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<ArticleListDto>>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String sort) {
        
        PageResult<ArticleListDto> articles = articleService.getArticles(page, size, status, authorId, categoryId, tag, sort);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResult<ArticleListDto>>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResult<ArticleListDto> articles = articleService.searchArticles(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
    
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<PageResult<ArticleListDto>>> getPopularArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer minViewCount) {
        
        PageResult<ArticleListDto> articles = articleService.getPopularArticles(page, size, minViewCount);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
    
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<PageResult<ArticleListDto>>> getTrendingArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer daysBack) {
        
        PageResult<ArticleListDto> articles = articleService.getTrendingArticles(page, size, daysBack);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
    
    @GetMapping("/author/{authorId}")
    public ResponseEntity<ApiResponse<PageResult<ArticleListDto>>> getArticlesByAuthor(
            @PathVariable String authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        PageResult<ArticleListDto> articles = articleService.getArticles(page, size, status, authorId, null, null, null);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
    
    @GetMapping("/{articleId}/author")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getArticleAuthor(@PathVariable String articleId) {
        ArticleDto article = articleService.getArticleDetail(articleId);
        Map<String, Object> authorInfo = new HashMap<>();
        authorInfo.put("authorId", article.getAuthorId());
        return ResponseEntity.ok(ApiResponse.success(authorInfo));
    }
    
    @PostMapping("/{articleId}/like")
    public ResponseEntity<ApiResponse<Void>> likeArticle(
            @PathVariable String articleId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        articleService.likeArticle(articleId, userId);
        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }
    
    @DeleteMapping("/{articleId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikeArticle(
            @PathVariable String articleId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        articleService.unlikeArticle(articleId, userId);
        return ResponseEntity.ok(ApiResponse.success("取消点赞成功", null));
    }
    
    @PostMapping("/{articleId}/bookmark")
    public ResponseEntity<ApiResponse<Void>> bookmarkArticle(
            @PathVariable String articleId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        articleService.bookmarkArticle(articleId, userId);
        return ResponseEntity.ok(ApiResponse.success("收藏成功", null));
    }
    
    @DeleteMapping("/{articleId}/bookmark")
    public ResponseEntity<ApiResponse<Void>> unbookmarkArticle(
            @PathVariable String articleId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        articleService.unbookmarkArticle(articleId, userId);
        return ResponseEntity.ok(ApiResponse.success("取消收藏成功", null));
    }
    
    @GetMapping("/{articleId}/like-status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getLikeStatus(
            @PathVariable String articleId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        Map<String, Boolean> status = articleService.getLikeAndBookmarkStatus(articleId, userId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
    
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResult<ArticleListDto>>> getMyArticles(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        PageResult<ArticleListDto> articles = articleService.getArticles(page, size, status, userId, null, null, null);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
    
    @GetMapping("/bookmarked")
    public ResponseEntity<ApiResponse<PageResult<ArticleListDto>>> getBookmarkedArticles(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResult<ArticleListDto> articles = articleService.getBookmarkedArticles(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
    
    @PostMapping("/{articleId}/statistics/increment-comment")
    public ResponseEntity<Void> incrementCommentCount(@PathVariable String articleId,
                                                    @RequestBody Map<String, Object> request) {
        String commentId = (String) request.get("commentId");
        String operation = (String) request.get("operation");
        
        articleService.incrementCommentCount(articleId, commentId, operation);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{articleId}/statistics/decrement-comment")
    public ResponseEntity<Void> decrementCommentCount(@PathVariable String articleId,
                                                    @RequestBody Map<String, Object> request) {
        String commentId = (String) request.get("commentId");
        String operation = (String) request.get("operation");
        
        articleService.decrementCommentCount(articleId, commentId, operation);
        return ResponseEntity.ok().build();
    }
}
