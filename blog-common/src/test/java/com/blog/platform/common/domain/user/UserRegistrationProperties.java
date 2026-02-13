package com.blog.platform.common.domain.user;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * **Feature: microservice-blog-platform, Property 1: 用户注册信息验证**
 * **验证需求: Requirements 1.1**
 * 
 * 属性测试：验证用户注册信息的正确性
 */
class UserRegistrationProperties {
    
    @Property(tries = 100)
    @Label("对于任何有效的用户注册信息，系统应当成功创建用户账户并返回用户ID")
    void validUserRegistrationCreatesUser(
            @ForAll("validUsernames") String username,
            @ForAll("validEmails") String email,
            @ForAll("validPasswords") String password) {
        
        // Given: 有效的用户注册信息
        Username usernameVO = Username.of(username);
        Email emailVO = Email.of(email);
        Password passwordVO = Password.fromRawPassword(password);
        
        // When: 注册用户
        User user = User.register(usernameVO, emailVO, passwordVO);
        
        // Then: 用户应该被成功创建
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getId().getValue()).isNotEmpty();
        assertThat(user.getUsername()).isEqualTo(usernameVO);
        assertThat(user.getEmail()).isEqualTo(emailVO);
        assertThat(user.getPassword()).isEqualTo(passwordVO);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.canLogin()).isTrue();
        
        // 验证领域事件被发布
        assertThat(user.hasDomainEvents()).isTrue();
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserRegisteredEvent.class);
        
        UserRegisteredEvent event = (UserRegisteredEvent) user.getDomainEvents().get(0);
        assertThat(event.getAggregateId()).isEqualTo(user.getId().getValue());
        assertThat(event.getUsername()).isEqualTo(username);
        assertThat(event.getEmail()).isEqualTo(email);
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的用户名，注册应当失败")
    void invalidUsernameRegistrationFails(@ForAll("invalidUsernames") String invalidUsername) {
        // Given: 无效的用户名
        Email validEmail = Email.of("test@example.com");
        Password validPassword = Password.fromRawPassword("password123");
        
        // When & Then: 注册应该失败
        assertThatThrownBy(() -> {
            Username username = Username.of(invalidUsername);
            User.register(username, validEmail, validPassword);
        }).isInstanceOf(IllegalArgumentException.class);
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的邮箱，注册应当失败")
    void invalidEmailRegistrationFails(@ForAll("invalidEmails") String invalidEmail) {
        // Given: 无效的邮箱
        Username validUsername = Username.of("testuser");
        Password validPassword = Password.fromRawPassword("password123");
        
        // When & Then: 注册应该失败
        assertThatThrownBy(() -> {
            Email email = Email.of(invalidEmail);
            User.register(validUsername, email, validPassword);
        }).isInstanceOf(IllegalArgumentException.class);
    }
    
    @Property(tries = 100)
    @Label("对于任何无效的密码，注册应当失败")
    void invalidPasswordRegistrationFails(@ForAll("invalidPasswords") String invalidPassword) {
        // Given: 无效的密码
        Username validUsername = Username.of("testuser");
        Email validEmail = Email.of("test@example.com");
        
        // When & Then: 注册应该失败
        assertThatThrownBy(() -> {
            Password password = Password.fromRawPassword(invalidPassword);
            User.register(validUsername, validEmail, password);
        }).isInstanceOf(IllegalArgumentException.class);
    }
    
    @Property(tries = 100)
    @Label("对于任何用户，密码验证应当正确工作")
    void passwordVerificationWorks(
            @ForAll("validUsernames") String username,
            @ForAll("validEmails") String email,
            @ForAll("validPasswords") String password) {
        
        // Given: 注册的用户
        User user = User.register(
            Username.of(username),
            Email.of(email),
            Password.fromRawPassword(password)
        );
        
        // When & Then: 密码验证应该正确
        assertThat(user.verifyPassword(password)).isTrue();
        assertThat(user.verifyPassword("wrongpassword")).isFalse();
        assertThat(user.verifyPassword(null)).isFalse();
    }
    
    // 生成器方法
    @Provide
    Arbitrary<String> validUsernames() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(3)
            .ofMaxLength(15)  // 减少长度，为数字留出空间
            .map(s -> s + Arbitraries.integers().between(1, 99).sample());  // 减少数字范围
    }
    
    @Provide
    Arbitrary<String> validEmails() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(3)
            .ofMaxLength(10)
            .map(localPart -> localPart + "@example.com");
    }
    
    @Provide
    Arbitrary<String> validPasswords() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(6)
            .ofMaxLength(20)
            .map(s -> s + "123");
    }
    
    @Provide
    Arbitrary<String> invalidUsernames() {
        return Arbitraries.oneOf(
            // 太短
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(2),
            // 太长
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(21).ofMaxLength(30),
            // 包含无效字符
            Arbitraries.strings().withChars("!@#$%^&*()").ofMinLength(3).ofMaxLength(10),
            // 空字符串
            Arbitraries.just(""),
            // 只有空格
            Arbitraries.just("   ")
        );
    }
    
    @Provide
    Arbitrary<String> invalidEmails() {
        return Arbitraries.oneOf(
            // 缺少@符号
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(10),
            // 缺少域名
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(3).ofMaxLength(8).map(s -> s + "@"),
            // 无效格式
            Arbitraries.just("invalid.email"),
            Arbitraries.just("@example.com"),
            Arbitraries.just("test@"),
            // 空字符串
            Arbitraries.just(""),
            // 只有空格
            Arbitraries.just("   ")
        );
    }
    
    @Provide
    Arbitrary<String> invalidPasswords() {
        return Arbitraries.oneOf(
            // 太短
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(5),
            // 太长
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(51).ofMaxLength(60),
            // 空字符串
            Arbitraries.just(""),
            // 只有空格
            Arbitraries.just("   ")
        );
    }
}