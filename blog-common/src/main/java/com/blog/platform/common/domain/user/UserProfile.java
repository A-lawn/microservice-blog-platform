package com.blog.platform.common.domain.user;

import com.blog.platform.common.domain.ValueObject;

import java.util.Objects;

/**
 * 用户资料值对象
 */
public class UserProfile extends ValueObject {
    
    private final String nickname;
    private final String avatar;
    private final String bio;
    
    public UserProfile(String nickname, String avatar, String bio) {
        this.nickname = nickname != null ? nickname.trim() : "";
        this.avatar = avatar != null ? avatar.trim() : "";
        this.bio = bio != null ? bio.trim() : "";
        validate();
    }
    
    public static UserProfile of(String nickname, String avatar, String bio) {
        return new UserProfile(nickname, avatar, bio);
    }
    
    public static UserProfile empty() {
        return new UserProfile("", "", "");
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public String getBio() {
        return bio;
    }
    
    public UserProfile withNickname(String newNickname) {
        return new UserProfile(newNickname, avatar, bio);
    }
    
    public UserProfile withAvatar(String newAvatar) {
        return new UserProfile(nickname, newAvatar, bio);
    }
    
    public UserProfile withBio(String newBio) {
        return new UserProfile(nickname, avatar, newBio);
    }
    
    @Override
    protected void validate() {
        if (nickname.length() > 50) {
            throw new IllegalArgumentException("昵称长度不能超过50个字符");
        }
        
        if (avatar.length() > 255) {
            throw new IllegalArgumentException("头像URL长度不能超过255个字符");
        }
        
        if (bio.length() > 500) {
            throw new IllegalArgumentException("个人简介长度不能超过500个字符");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserProfile that = (UserProfile) obj;
        return Objects.equals(nickname, that.nickname) &&
               Objects.equals(avatar, that.avatar) &&
               Objects.equals(bio, that.bio);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nickname, avatar, bio);
    }
    
    @Override
    public String toString() {
        return String.format("UserProfile{nickname='%s', avatar='%s', bio='%s'}", 
                nickname, avatar, bio);
    }
}