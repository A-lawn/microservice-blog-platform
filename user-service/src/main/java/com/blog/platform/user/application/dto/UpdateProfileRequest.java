package com.blog.platform.user.application.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新用户资料请求DTO
 */
public class UpdateProfileRequest {
    
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;
    
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;
    
    public UpdateProfileRequest() {}
    
    public UpdateProfileRequest(String nickname, String avatar, String bio) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.bio = bio;
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
}