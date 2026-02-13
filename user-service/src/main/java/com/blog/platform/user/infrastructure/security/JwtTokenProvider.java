package com.blog.platform.user.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT令牌提供者
 * Updated for JJWT 0.12.x API
 */
@Component
public class JwtTokenProvider {
    
    private final SecretKey secretKey;
    private final long validityInMilliseconds;
    
    public JwtTokenProvider(@Value("${jwt.secret:mySecretKey}") String secret,
                           @Value("${jwt.validity:3600000}") long validityInMilliseconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }
    
    /**
     * 创建JWT令牌
     */
    public String createToken(String userId, String username, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        
        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * 从令牌中获取用户ID
     */
    public String getUserId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    
    /**
     * 从令牌中获取用户名
     */
    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("username", String.class);
    }
    
    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 从令牌中获取用户角色
     */
    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
    
    /**
     * 获取令牌过期时间
     */
    public LocalDateTime getExpirationTime(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        
        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    
    /**
     * 获取令牌有效期（秒）
     */
    public long getValidityInSeconds() {
        return validityInMilliseconds / 1000;
    }
}
