package com.blog.platform.user.interfaces.rest;

import com.blog.platform.common.domain.user.UserStatus;
import com.blog.platform.user.application.dto.*;
import com.blog.platform.user.application.service.UserApplicationService;
import com.blog.platform.user.infrastructure.persistence.entity.UserEntity;
import com.blog.platform.user.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {
    
    private final UserJpaRepository userRepository;
    private final UserApplicationService userApplicationService;
    
    public AdminUserController(UserJpaRepository userRepository, UserApplicationService userApplicationService) {
        this.userRepository = userRepository;
        this.userApplicationService = userApplicationService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<UserEntity> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userRepository.findByKeyword(keyword.trim(), pageRequest);
        } else if (status != null && !status.trim().isEmpty()) {
            userPage = userRepository.findByStatus(UserStatus.valueOf(status), pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }
        
        List<Map<String, Object>> users = new ArrayList<>();
        for (UserEntity user : userPage.getContent()) {
            users.add(convertToMap(user));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", users);
        result.put("totalElements", userPage.getTotalElements());
        result.put("totalPages", userPage.getTotalPages());
        result.put("currentPage", page);
        result.put("size", size);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserById(@PathVariable String id) {
        return userRepository.findById(id)
            .map(user -> ResponseEntity.ok(ApiResponse.success(convertToMap(user))))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserStatus(
            @PathVariable String id, 
            @RequestBody Map<String, String> request) {
        
        String newStatus = request.get("status");
        if (newStatus == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "状态不能为空"));
        }
        
        return userRepository.findById(id)
            .map(user -> {
                user.setStatus(UserStatus.valueOf(newStatus));
                UserEntity saved = userRepository.save(user);
                return ResponseEntity.ok(ApiResponse.success("状态更新成功", convertToMap(saved)));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserRole(
            @PathVariable String id, 
            @RequestBody Map<String, String> request) {
        
        String newRole = request.get("role");
        if (newRole == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "角色不能为空"));
        }
        
        return userRepository.findById(id)
            .map(user -> {
                user.getRoles().clear();
                return ResponseEntity.ok(ApiResponse.success("角色更新成功", convertToMap(user)));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByStatus(UserStatus.ACTIVE));
        stats.put("bannedUsers", userRepository.countByStatus(UserStatus.BANNED));
        stats.put("inactiveUsers", userRepository.countByStatus(UserStatus.INACTIVE));
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    private Map<String, Object> convertToMap(UserEntity user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("nickname", user.getNickname());
        map.put("avatarUrl", user.getAvatarUrl());
        map.put("bio", user.getBio());
        map.put("status", user.getStatus().name());
        map.put("lastLoginAt", user.getLastLoginAt());
        map.put("loginCount", user.getLoginCount());
        map.put("createdAt", user.getCreatedAt());
        map.put("updatedAt", user.getUpdatedAt());
        
        if (user.getStatistics() != null) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("articleCount", user.getStatistics().getArticleCount());
            stats.put("commentCount", user.getStatistics().getCommentCount());
            stats.put("followerCount", user.getStatistics().getFollowerCount());
            stats.put("followingCount", user.getStatistics().getFollowingCount());
            map.put("statistics", stats);
        }
        
        List<String> roles = new ArrayList<>();
        if (user.getRoles() != null) {
            for (var role : user.getRoles()) {
                roles.add(role.getRoleName());
            }
        }
        map.put("roles", roles);
        
        return map;
    }
}
