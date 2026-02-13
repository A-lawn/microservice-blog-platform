package com.blog.platform.common.domain.comment;

import com.blog.platform.common.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * 评论ID值对象
 */
public class CommentId extends ValueObject {
    
    private final String value;
    
    public CommentId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("评论ID不能为空");
        }
        this.value = value.trim();
        validate();
    }
    
    public static CommentId generate() {
        return new CommentId(UUID.randomUUID().toString());
    }
    
    public static CommentId of(String value) {
        return new CommentId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    protected void validate() {
        if (value.length() > 50) {
            throw new IllegalArgumentException("评论ID长度不能超过50个字符");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CommentId commentId = (CommentId) obj;
        return Objects.equals(value, commentId.value);
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