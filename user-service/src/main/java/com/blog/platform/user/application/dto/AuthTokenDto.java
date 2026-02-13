package com.blog.platform.user.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 认证令牌DTO
 */
public class AuthTokenDto {
    
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private UserDto user;
    
    public AuthTokenDto() {}
    
    public AuthTokenDto(String accessToken, String tokenType, long expiresIn, 
                       LocalDateTime expiresAt, UserDto user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.expiresAt = expiresAt;
        this.user = user;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public UserDto getUser() {
        return user;
    }
    
    public void setUser(UserDto user) {
        this.user = user;
    }
}