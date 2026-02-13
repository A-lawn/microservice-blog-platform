package com.blog.platform.common.domain.user;

import com.blog.platform.common.domain.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 用户名值对象
 */
public class Username extends ValueObject {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    private final String value;
    
    public Username(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        this.value = value.trim();
        validate();
    }
    
    public static Username of(String value) {
        return new Username(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    protected void validate() {
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("用户名格式不正确，只能包含字母、数字和下划线，长度为3-20个字符");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Username username = (Username) obj;
        return Objects.equals(value, username.value);
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