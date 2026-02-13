package com.blog.platform.user.interfaces.rest;

import com.blog.platform.user.infrastructure.persistence.entity.NotificationEntity;
import com.blog.platform.user.infrastructure.persistence.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnreadCount(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null || userId.isEmpty()) {
            result.put("count", 0);
            return ResponseEntity.ok(ApiResponse.success(result));
        }
        
        long count = notificationRepository.countUnreadByUserId(userId);
        result.put("count", count);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotifications(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null || userId.isEmpty()) {
            result.put("content", new ArrayList<>());
            result.put("page", page);
            result.put("size", size);
            result.put("totalElements", 0);
            result.put("totalPages", 0);
            return ResponseEntity.ok(ApiResponse.success(result));
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationEntity> notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<Map<String, Object>> notifications = new ArrayList<>();
        for (NotificationEntity entity : notificationPage.getContent()) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", entity.getId());
            notification.put("type", entity.getType().name());
            notification.put("title", entity.getTitle());
            notification.put("content", entity.getContent());
            notification.put("referenceId", entity.getReferenceId());
            notification.put("referenceType", entity.getReferenceType());
            notification.put("isRead", entity.getIsRead());
            notification.put("createdAt", entity.getCreatedAt());
            notifications.add(notification);
        }
        
        result.put("content", notifications);
        result.put("page", notificationPage.getNumber());
        result.put("size", notificationPage.getSize());
        result.put("totalElements", notificationPage.getTotalElements());
        result.put("totalPages", notificationPage.getTotalPages());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        notificationRepository.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("标记已读成功", null));
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId != null && !userId.isEmpty()) {
            notificationRepository.markAllAsReadByUserId(userId);
        }
        return ResponseEntity.ok(ApiResponse.success("全部标记已读成功", null));
    }
}
