package com.blog.platform.common.domain.comment;

import com.blog.platform.common.domain.ValueObject;

import java.util.Objects;

/**
 * 评论内容值对象
 */
public class CommentContent extends ValueObject {
    
    private final String value;
    
    public CommentContent(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        this.value = value.trim();
        validate();
    }
    
    public static CommentContent of(String value) {
        return new CommentContent(value);
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * 获取评论内容长度
     */
    public int getLength() {
        return value.length();
    }
    
    /**
     * 检查是否为空内容
     */
    public boolean isEmpty() {
        return value.trim().isEmpty();
    }
    
    @Override
    protected void validate() {
        if (value.length() > 2000) {
            throw new IllegalArgumentException("评论内容长度不能超过2000个字符");
        }
        if (value.trim().length() < 1) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CommentContent that = (CommentContent) obj;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.format("CommentContent{length=%d, preview='%s'}", 
                value.length(), 
                value.length() > 50 ? value.substring(0, 50) + "..." : value);
    }
}