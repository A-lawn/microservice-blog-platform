package com.blog.platform.user.application.dto;

import com.blog.platform.common.domain.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 用户信息DTO
 */
public class UserDto {
    
    private String id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String bio;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public UserDto() {}
    
    public UserDto(String id, String username, String email, String nickname, 
                   String avatar, String bio, String status, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.avatar = avatar;
        this.bio = bio;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public static UserDto fromDomain(User user) {
        return new UserDto(
            user.getId().getValue(),
            user.getUsername().getValue(),
            user.getEmail().getValue(),
            user.getProfile().getNickname(),
            user.getProfile().getAvatar(),
            user.getProfile().getBio(),
            user.getStatus().name(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}