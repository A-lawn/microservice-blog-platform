package com.blog.platform.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT令牌验证器测试
 */
class JwtTokenValidatorTest {

    private JwtTokenValidator jwtTokenValidator;
    private SecretKey secretKey;
    private String testSecret = "testSecretKey123456789012345678901234567890";

    @BeforeEach
    void setUp() {
        jwtTokenValidator = new JwtTokenValidator(testSecret);
        secretKey = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldValidateValidToken() {
        // 创建有效的JWT令牌
        String token = Jwts.builder()
                .setSubject("user123")
                .claim("username", "testuser")
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1小时后过期
                .signWith(secretKey)
                .compact();

        assertTrue(jwtTokenValidator.validateToken(token));
    }

    @Test
    void shouldRejectExpiredToken() {
        // 创建过期的JWT令牌
        String token = Jwts.builder()
                .setSubject("user123")
                .claim("username", "testuser")
                .claim("role", "USER")
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2小时前
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1小时前过期
                .signWith(secretKey)
                .compact();

        assertFalse(jwtTokenValidator.validateToken(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.jwt.token";
        assertFalse(jwtTokenValidator.validateToken(invalidToken));
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String userId = "user123";
        String token = Jwts.builder()
                .setSubject(userId)
                .claim("username", "testuser")
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();

        assertEquals(userId, jwtTokenValidator.getUserIdFromToken(token));
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String username = "testuser";
        String token = Jwts.builder()
                .setSubject("user123")
                .claim("username", username)
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();

        assertEquals(username, jwtTokenValidator.getUsernameFromToken(token));
    }

    @Test
    void shouldExtractRoleFromToken() {
        String role = "ADMIN";
        String token = Jwts.builder()
                .setSubject("user123")
                .claim("username", "testuser")
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();

        assertEquals(role, jwtTokenValidator.getRoleFromToken(token));
    }
}