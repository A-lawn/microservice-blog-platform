package com.blog.platform.user.application.service;

import com.blog.platform.common.domain.user.*;
import com.blog.platform.user.application.dto.UpdateProfileRequest;
import com.blog.platform.user.application.dto.UserDto;
import com.blog.platform.user.domain.exception.UserDomainException;
import com.blog.platform.user.domain.repository.UserRepository;
import com.blog.platform.user.infrastructure.security.JwtTokenProvider;
import com.blog.platform.user.infrastructure.messaging.UserEventPublisher;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * **Feature: microservice-blog-platform, Property 3: 用户信息更新一致性**
 * **验证需求: Requirements 1.3**
 * 
 * 用户信息更新属性测试
 */
class UserProfileUpdateProperties {
    
    @Property(tries = 100)
    void updateProfileShouldReturnUpdatedData(
            @ForAll("validUsername") String username,
            @ForAll("validPassword") String password,
            @ForAll("validNickname") String newNickname,
            @ForAll("validAvatar") String newAvatar,
            @ForAll("validBio") String newBio) {
        
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        UserEventPublisher userEventPublisher = Mockito.mock(UserEventPublisher.class);
        UserApplicationService userApplicationService = new UserApplicationService(userRepository, jwtTokenProvider, userEventPublisher);
        
        Username usernameObj = Username.of(username);
        Email email = Email.of(username + "@example.com");
        Password passwordObj = Password.fromRawPassword(password);
        
        User existingUser = User.register(usernameObj, email, passwordObj);
        UserId userId = existingUser.getId();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });
        
        UpdateProfileRequest updateRequest = new UpdateProfileRequest(newNickname, newAvatar, newBio);
        userApplicationService.updateProfile(userId.getValue(), updateRequest);
        
        User updatedUser = User.reconstruct(
            userId, usernameObj, email, passwordObj,
            UserProfile.of(newNickname, newAvatar, newBio),
            UserStatus.ACTIVE,
            UserRole.defaultRole(),
            existingUser.getCreatedAt(),
            LocalDateTime.now()
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(updatedUser));
        
        UserDto result = userApplicationService.getUserProfile(userId.getValue());
        
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(newNickname);
        assertThat(result.getAvatar()).isEqualTo(newAvatar);
        assertThat(result.getBio()).isEqualTo(newBio);
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo(email.getValue());
    }
    
    @Property(tries = 100)
    void updateNonExistentUserShouldThrowException(
            @ForAll("validUserId") String userId,
            @ForAll("validNickname") String newNickname,
            @ForAll("validAvatar") String newAvatar,
            @ForAll("validBio") String newBio) {
        
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        UserEventPublisher userEventPublisher = Mockito.mock(UserEventPublisher.class);
        UserApplicationService userApplicationService = new UserApplicationService(userRepository, jwtTokenProvider, userEventPublisher);
        
        UserId userIdObj = UserId.of(userId);
        when(userRepository.findById(userIdObj)).thenReturn(Optional.empty());
        
        UpdateProfileRequest updateRequest = new UpdateProfileRequest(newNickname, newAvatar, newBio);
        assertThatThrownBy(() -> userApplicationService.updateProfile(userId, updateRequest))
                .isInstanceOf(UserDomainException.class)
                .hasMessage("用户不存在");
    }
    
    @Property(tries = 100)
    void partialUpdateShouldPreserveUnchangedFields(
            @ForAll("validUsername") String username,
            @ForAll("validPassword") String password,
            @ForAll("validNickname") String originalNickname,
            @ForAll("validAvatar") String originalAvatar,
            @ForAll("validBio") String originalBio,
            @ForAll("validNickname") String newNickname) {
        
        Assume.that(!originalNickname.equals(newNickname));
        
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        UserEventPublisher userEventPublisher = Mockito.mock(UserEventPublisher.class);
        UserApplicationService userApplicationService = new UserApplicationService(userRepository, jwtTokenProvider, userEventPublisher);
        
        Username usernameObj = Username.of(username);
        Email email = Email.of(username + "@example.com");
        Password passwordObj = Password.fromRawPassword(password);
        
        User existingUser = User.reconstruct(
            UserId.generate(), usernameObj, email, passwordObj,
            UserProfile.of(originalNickname, originalAvatar, originalBio),
            UserStatus.ACTIVE,
            UserRole.defaultRole(),
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1)
        );
        
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        UpdateProfileRequest updateRequest = new UpdateProfileRequest(newNickname, null, null);
        userApplicationService.updateProfile(existingUser.getId().getValue(), updateRequest);
        
        User updatedUser = User.reconstruct(
            existingUser.getId(), usernameObj, email, passwordObj,
            UserProfile.of(newNickname, originalAvatar, originalBio),
            UserStatus.ACTIVE,
            UserRole.defaultRole(),
            existingUser.getCreatedAt(),
            LocalDateTime.now()
        );
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(updatedUser));
        
        UserDto result = userApplicationService.getUserProfile(existingUser.getId().getValue());
        
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(newNickname);
        assertThat(result.getAvatar()).isEqualTo(originalAvatar);
        assertThat(result.getBio()).isEqualTo(originalBio);
    }
    
    @Provide
    Arbitrary<String> validUsername() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(20);
    }
    
    @Provide
    Arbitrary<String> validPassword() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(6)
                .ofMaxLength(20);
    }
    
    @Provide
    Arbitrary<String> validNickname() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(50);
    }
    
    @Provide
    Arbitrary<String> validAvatar() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(100)
                .map(s -> "https://example.com/avatar/" + s + ".jpg");
    }
    
    @Provide
    Arbitrary<String> validBio() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(200);
    }
    
    @Provide
    Arbitrary<String> validUserId() {
        return Arbitraries.strings()
                .withCharRange('a', 'f')
                .withCharRange('0', '9')
                .ofLength(36)
                .map(s -> s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16, 20) + "-" + s.substring(20, 32));
    }
}
