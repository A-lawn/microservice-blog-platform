package com.blog.platform.user.interfaces.rest;

import com.blog.platform.user.application.dto.*;
import com.blog.platform.user.application.service.UserApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserApplicationService userApplicationService;
    
    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody UserRegistrationRequest request) {
        UserDto user = userApplicationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("注册成功", user));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenDto>> login(@Valid @RequestBody LoginRequest request) {
        AuthTokenDto authToken = userApplicationService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登录成功", authToken));
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserProfile(@PathVariable String userId) {
        UserDto user = userApplicationService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PutMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@PathVariable String userId,
                                            @Valid @RequestBody UpdateProfileRequest request) {
        userApplicationService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDto>> findByUsername(@PathVariable String username) {
        UserDto user = userApplicationService.findByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<UserDto>> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "未授权"));
        }

        String token = authHeader.substring(7);
        UserDto user = userApplicationService.validateTokenAndGetUser(token);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUserProfile(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "未授权"));
        }

        String token = authHeader.substring(7);
        UserDto user = userApplicationService.validateTokenAndGetUser(token);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PostMapping("/{userId}/statistics/increment-article")
    public ResponseEntity<ApiResponse<Void>> incrementArticleCount(@PathVariable String userId,
                                                    @RequestBody java.util.Map<String, Object> request) {
        String articleId = (String) request.get("articleId");
        String operation = (String) request.get("operation");
        
        userApplicationService.incrementArticleCount(userId, articleId, operation);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PostMapping("/{userId}/statistics/decrement-article")
    public ResponseEntity<ApiResponse<Void>> decrementArticleCount(@PathVariable String userId,
                                                    @RequestBody java.util.Map<String, Object> request) {
        String articleId = (String) request.get("articleId");
        String operation = (String) request.get("operation");
        
        userApplicationService.decrementArticleCount(userId, articleId, operation);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
