package com.blog.platform.common.domain.user;

import com.blog.platform.common.domain.ValueObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Objects;

public class Password extends ValueObject {
    
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    
    private final String hashedValue;
    
    private Password(String hashedValue) {
        this.hashedValue = hashedValue;
    }
    
    public static Password fromRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        if (rawPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6个字符");
        }
        
        if (rawPassword.length() > 100) {
            throw new IllegalArgumentException("密码长度不能超过100个字符");
        }
        
        String hashed = ENCODER.encode(rawPassword);
        return new Password(hashed);
    }
    
    public static Password fromHashedPassword(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("哈希密码不能为空");
        }
        return new Password(hashedPassword);
    }
    
    public String getHashedValue() {
        return hashedValue;
    }
    
    public boolean matches(String rawPassword) {
        if (rawPassword == null) {
            return false;
        }
        return ENCODER.matches(rawPassword, hashedValue);
    }
    
    @Override
    protected void validate() {
        if (hashedValue == null || hashedValue.trim().isEmpty()) {
            throw new IllegalArgumentException("哈希密码不能为空");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Password password = (Password) obj;
        return Objects.equals(hashedValue, password.hashedValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }
    
    @Override
    public String toString() {
        return "Password{hashed}";
    }
}
