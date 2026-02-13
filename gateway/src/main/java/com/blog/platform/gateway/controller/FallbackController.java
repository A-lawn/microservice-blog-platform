package com.blog.platform.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 熔断降级控制器
 * 当微服务不可用时提供降级响应
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * 用户服务降级响应
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "User service is temporarily unavailable");
        response.put("message", "Please try again later");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "user-service");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 文章服务降级响应
     */
    @GetMapping("/article")
    public ResponseEntity<Map<String, Object>> articleServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Article service is temporarily unavailable");
        response.put("message", "Please try again later");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "article-service");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 评论服务降级响应
     */
    @GetMapping("/comment")
    public ResponseEntity<Map<String, Object>> commentServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Comment service is temporarily unavailable");
        response.put("message", "Please try again later");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "comment-service");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 通用降级响应
     */
    @GetMapping("/default")
    public ResponseEntity<Map<String, Object>> defaultFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service is temporarily unavailable");
        response.put("message", "Please try again later");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "unknown");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}