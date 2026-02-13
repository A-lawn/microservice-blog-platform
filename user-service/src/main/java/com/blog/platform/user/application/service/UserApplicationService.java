package com.blog.platform.user.application.service;

import com.blog.platform.common.domain.user.*;
import com.blog.platform.user.domain.exception.UserDomainException;
import com.blog.platform.user.application.dto.*;
import com.blog.platform.user.domain.repository.UserRepository;
import com.blog.platform.user.infrastructure.security.JwtTokenProvider;
import com.blog.platform.user.infrastructure.messaging.UserEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户应用服务
 */
@Service
@Transactional
public class UserApplicationService {
    
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserEventPublisher userEventPublisher;
    
    public UserApplicationService(UserRepository userRepository, 
                                JwtTokenProvider jwtTokenProvider,
                                UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userEventPublisher = userEventPublisher;
    }
    
    /**
     * 用户注册
     */
    public UserDto register(UserRegistrationRequest request) {
        // 检查用户名是否已存在
        Username username = Username.of(request.getUsername());
        if (userRepository.existsByUsername(username)) {
            throw new UserDomainException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        Email email = Email.of(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new UserDomainException("邮箱已存在");
        }
        
        // 创建用户
        Password password = Password.fromRawPassword(request.getPassword());
        User user = User.register(username, email, password);
        
        // 保存用户
        User savedUser = userRepository.save(user);
        
        // 发布用户注册事件
        UserRegisteredEvent event = new UserRegisteredEvent(
            savedUser.getId().getValue(),
            savedUser.getUsername().getValue(),
            savedUser.getEmail().getValue()
        );
        userEventPublisher.publishUserRegisteredEvent(event);
        
        return UserDto.fromDomain(savedUser);
    }
    
    /**
     * 用户登录
     */
    public AuthTokenDto login(LoginRequest request) {
        Username username = Username.of(request.getUsername());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserDomainException("用户名或密码错误"));
        
        if (!user.verifyPassword(request.getPassword())) {
            throw new UserDomainException("用户名或密码错误");
        }
        
        if (!user.canLogin()) {
            throw new UserDomainException("用户账户已被禁用");
        }
        
        userRepository.recordLogin(user.getId());
        
        String token = jwtTokenProvider.createToken(
            user.getId().getValue(), 
            user.getUsername().getValue(),
            user.getRole().getCode()
        );
        
        LocalDateTime expiresAt = jwtTokenProvider.getExpirationTime(token);
        long expiresIn = jwtTokenProvider.getValidityInSeconds();
        
        UserDto userDto = UserDto.fromDomain(user);
        
        return new AuthTokenDto(token, "Bearer", expiresIn, expiresAt, userDto);
    }
    
    /**
     * 获取用户资料
     */
    @Transactional(readOnly = true)
    public UserDto getUserProfile(String userId) {
        UserId id = UserId.of(userId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserDomainException("用户不存在"));
        
        return UserDto.fromDomain(user);
    }
    
    /**
     * 更新用户资料
     */
    public void updateProfile(String userId, UpdateProfileRequest request) {
        UserId id = UserId.of(userId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserDomainException("用户不存在"));
        
        // 更新用户资料
        UserProfile newProfile = UserProfile.of(
            request.getNickname() != null ? request.getNickname() : user.getProfile().getNickname(),
            request.getAvatar() != null ? request.getAvatar() : user.getProfile().getAvatar(),
            request.getBio() != null ? request.getBio() : user.getProfile().getBio()
        );
        
        user.updateProfile(newProfile);
        
        // 保存更新
        User savedUser = userRepository.save(user);
        
        // 发布用户资料更新事件
        UserProfileUpdatedEvent event = new UserProfileUpdatedEvent(
            savedUser.getId().getValue(),
            savedUser.getProfile().getNickname(),
            savedUser.getProfile().getAvatar(),
            savedUser.getProfile().getBio()
        );
        userEventPublisher.publishUserProfileUpdatedEvent(event);
    }
    
    /**
     * 根据用户名查找用户
     */
    @Transactional(readOnly = true)
    public UserDto findByUsername(String username) {
        Username usernameObj = Username.of(username);
        User user = userRepository.findByUsername(usernameObj)
                .orElseThrow(() -> new UserDomainException("用户不存在"));
        
        return UserDto.fromDomain(user);
    }
    
    /**
     * 验证JWT令牌并获取用户信息
     */
    @Transactional(readOnly = true)
    public UserDto validateTokenAndGetUser(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UserDomainException("无效的令牌");
        }
        
        String userId = jwtTokenProvider.getUserId(token);
        return getUserProfile(userId);
    }
    
    /**
     * 增加用户文章统计
     */
    public void incrementArticleCount(String userId, String articleId, String operation) {
        UserId id = UserId.of(userId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserDomainException("用户不存在"));
        
        // This would typically update user statistics
        // For now, we'll just log the operation
        // In a real implementation, you would update user statistics entity
        
        userRepository.save(user);
    }
    
    /**
     * 减少用户文章统计
     */
    public void decrementArticleCount(String userId, String articleId, String operation) {
        UserId id = UserId.of(userId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserDomainException("用户不存在"));
        
        // This would typically update user statistics
        // For now, we'll just log the operation
        // In a real implementation, you would update user statistics entity
        
        userRepository.save(user);
    }
}