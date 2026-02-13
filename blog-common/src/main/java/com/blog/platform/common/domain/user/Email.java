package com.blog.platform.common.domain.user;

import com.blog.platform.common.domain.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 邮箱值对象
 */
public class Email extends ValueObject {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private final String value;
    
    public Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        this.value = value.trim().toLowerCase();
        validate();
    }
    
    public static Email of(String value) {
        return new Email(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    protected void validate() {
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        
        if (value.length() > 100) {
            throw new IllegalArgumentException("邮箱长度不能超过100个字符");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Email email = (Email) obj;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}