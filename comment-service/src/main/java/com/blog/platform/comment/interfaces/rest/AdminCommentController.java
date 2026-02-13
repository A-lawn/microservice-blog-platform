package com.blog.platform.comment.interfaces.rest;

import com.blog.platform.comment.application.service.CommentApplicationService;
import com.blog.platform.comment.infrastructure.persistence.entity.CommentEntity;
import com.blog.platform.comment.infrastructure.persistence.repository.CommentJpaRepository;
import com.blog.platform.common.domain.comment.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/comments")
@CrossOrigin(origins = "*")
public class AdminCommentController {
    
    private final CommentJpaRepository commentRepository;
    
    public AdminCommentController(CommentJpaRepository commentRepository, CommentApplicationService commentApplicationService) {
        this.commentRepository = commentRepository;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String articleId) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<CommentEntity> commentPage;
        if (status != null && !status.trim().isEmpty()) {
            commentPage = commentRepository.findByStatus(CommentStatus.valueOf(status), pageRequest);
        } else if (articleId != null && !articleId.trim().isEmpty()) {
            commentPage = commentRepository.findByArticleId(articleId, pageRequest);
        } else {
            commentPage = commentRepository.findAll(pageRequest);
        }
        
        List<Map<String, Object>> comments = new ArrayList<>();
        for (CommentEntity comment : commentPage.getContent()) {
            comments.add(convertToMap(comment));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", comments);
        result.put("totalElements", commentPage.getTotalElements());
        result.put("totalPages", commentPage.getTotalPages());
        result.put("currentPage", page);
        result.put("size", size);
        
        return ResponseEntity.ok(Collections.singletonMap("data", result));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCommentById(@PathVariable String id) {
        return commentRepository.findById(id)
            .map(comment -> ResponseEntity.ok(Map.of("data", convertToMap(comment))))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCommentStatus(
            @PathVariable String id, 
            @RequestBody Map<String, String> request) {
        
        String newStatus = request.get("status");
        if (newStatus == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("code", 400, "message", "状态不能为空"));
        }
        
        return commentRepository.findById(id)
            .map(comment -> {
                comment.setStatus(CommentStatus.valueOf(newStatus));
                CommentEntity saved = commentRepository.save(comment);
                return ResponseEntity.ok(Map.of("data", convertToMap(saved)));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable String id) {
        if (!commentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        commentRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("data", (Object) null));
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCommentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalComments", commentRepository.count());
        stats.put("pendingComments", commentRepository.countByStatus(CommentStatus.PENDING));
        stats.put("approvedComments", commentRepository.countByStatus(CommentStatus.APPROVED));
        stats.put("rejectedComments", commentRepository.countByStatus(CommentStatus.REJECTED));
        
        return ResponseEntity.ok(Collections.singletonMap("data", stats));
    }
    
    @PostMapping("/batch-approve")
    public ResponseEntity<Map<String, Object>> batchApprove(@RequestBody List<String> ids) {
        for (String id : ids) {
            commentRepository.findById(id).ifPresent(comment -> {
                comment.setStatus(CommentStatus.APPROVED);
                commentRepository.save(comment);
            });
        }
        return ResponseEntity.ok(Map.of("data", (Object) null));
    }
    
    @PostMapping("/batch-reject")
    public ResponseEntity<Map<String, Object>> batchReject(@RequestBody List<String> ids) {
        for (String id : ids) {
            commentRepository.findById(id).ifPresent(comment -> {
                comment.setStatus(CommentStatus.REJECTED);
                commentRepository.save(comment);
            });
        }
        return ResponseEntity.ok(Map.of("data", (Object) null));
    }
    
    @PostMapping("/batch-delete")
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody List<String> ids) {
        commentRepository.deleteAllById(ids);
        return ResponseEntity.ok(Map.of("data", (Object) null));
    }
    
    private Map<String, Object> convertToMap(CommentEntity comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("articleId", comment.getArticleId());
        map.put("authorId", comment.getAuthorId());
        map.put("content", comment.getContent());
        map.put("status", comment.getStatus().name());
        map.put("parentId", comment.getParentId());
        map.put("createdAt", comment.getCreatedAt());
        map.put("updatedAt", comment.getUpdatedAt());
        
        if (comment.getStatistics() != null) {
            map.put("likeCount", comment.getStatistics().getLikeCount());
        } else {
            map.put("likeCount", 0);
        }
        
        return map;
    }
}
