package com.blog.platform.user.application.service;

import com.blog.platform.common.domain.user.*;
import com.blog.platform.user.application.dto.AuthTokenDto;
import com.blog.platform.user.application.dto.LoginRequest;
import com.blog.platform.user.domain.exception.UserDomainException;
import com.blog.platform.user.domain.repository.UserRepository;
import com.blog.platform.user.infrastructure.security.JwtTokenProvider;
import com.blog.platform.user.infrastructure.messaging.UserEventPublisher;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * **Feature: microservice-blog-platform, Property 2: 登录凭据验证**
 * **验证需求: Requirements 1.2**
 * 
 * 用户认证属性测试
 */
class UserAuthenticationProperties {
    
    @Property(tries = 100)
    void validCredentialsShouldReturnAuthToken(
            @ForAll("validUsername") String username,
            @ForAll("validPassword") String password) {
        
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        UserEventPublisher userEventPublisher = Mockito.mock(UserEventPublisher.class);
        UserApplicationService userApplicationService = new UserApplicationService(userRepository, jwtTokenProvider, userEventPublisher);
        
        Username usernameObj = Username.of(username);
        Email email = Email.of(username + "@example.com");
        Password passwordObj = Password.fromRawPassword(password);
        
        User existingUser = User.register(usernameObj, email, passwordObj);
        
        when(userRepository.findByUsername(usernameObj)).thenReturn(Optional.of(existingUser));
        when(jwtTokenProvider.createToken(any(String.class), any(String.class), any(String.class))).thenReturn("mock-jwt-token");
        when(jwtTokenProvider.getExpirationTime(any(String.class))).thenReturn(java.time.LocalDateTime.now().plusHours(1));
        when(jwtTokenProvider.getValidityInSeconds()).thenReturn(3600L);
        
        LoginRequest loginRequest = new LoginRequest(username, password);
        AuthTokenDto result = userApplicationService.login(loginRequest);
        
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("mock-jwt-token");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getExpiresIn()).isEqualTo(3600L);
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getUsername()).isEqualTo(username);
    }
    
    @Property(tries = 100)
    void invalidPasswordShouldThrowException(
            @ForAll("validUsername") String username,
            @ForAll("validPassword") String correctPassword,
            @ForAll("validPassword") String wrongPassword) {
        
        Assume.that(!correctPassword.equals(wrongPassword));
        
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        UserEventPublisher userEventPublisher = Mockito.mock(UserEventPublisher.class);
        UserApplicationService userApplicationService = new UserApplicationService(userRepository, jwtTokenProvider, userEventPublisher);
        
        Username usernameObj = Username.of(username);
        Email email = Email.of(username + "@example.com");
        Password passwordObj = Password.fromRawPassword(correctPassword);
        
        User existingUser = User.register(usernameObj, email, passwordObj);
        
        when(userRepository.findByUsername(usernameObj)).thenReturn(Optional.of(existingUser));
        
        LoginRequest loginRequest = new LoginRequest(username, wrongPassword);
        assertThatThrownBy(() -> userApplicationService.login(loginRequest))
                .isInstanceOf(UserDomainException.class)
                .hasMessage("用户名或密码错误");
    }
    
    @Property(tries = 100)
    void nonExistentUserShouldThrowException(
            @ForAll("validUsername") String username,
            @ForAll("validPassword") String password) {
        
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        UserEventPublisher userEventPublisher = Mockito.mock(UserEventPublisher.class);
        UserApplicationService userApplicationService = new UserApplicationService(userRepository, jwtTokenProvider, userEventPublisher);
        
        Username usernameObj = Username.of(username);
        when(userRepository.findByUsername(usernameObj)).thenReturn(Optional.empty());
        
        LoginRequest loginRequest = new LoginRequest(username, password);
        assertThatThrownBy(() -> userApplicationService.login(loginRequest))
                .isInstanceOf(UserDomainException.class)
                .hasMessage("用户名或密码错误");
    }
    
    @Property(tries = 100)
    void bannedUserShouldThrowException(
            @ForAll("validUsername") String username,
            @ForAll("validPassword") String password) {
        
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        UserEventPublisher userEventPublisher = Mockito.mock(UserEventPublisher.class);
        UserApplicationService userApplicationService = new UserApplicationService(userRepository, jwtTokenProvider, userEventPublisher);
        
        Username usernameObj = Username.of(username);
        Email email = Email.of(username + "@example.com");
        Password passwordObj = Password.fromRawPassword(password);
        
        User bannedUser = User.register(usernameObj, email, passwordObj);
        bannedUser.ban();
        
        when(userRepository.findByUsername(usernameObj)).thenReturn(Optional.of(bannedUser));
        
        LoginRequest loginRequest = new LoginRequest(username, password);
        assertThatThrownBy(() -> userApplicationService.login(loginRequest))
                .isInstanceOf(UserDomainException.class)
                .hasMessage("用户账户已被禁用");
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
}
