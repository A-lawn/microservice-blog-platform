package com.blog.platform.common.domain.article;

import com.blog.platform.common.domain.ValueObject;

import java.util.Objects;

/**
 * 文章标题值对象
 */
public class ArticleTitle extends ValueObject {
    
    private final String value;
    
    public ArticleTitle(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        this.value = value.trim();
        validate();
    }
    
    public static ArticleTitle of(String value) {
        return new ArticleTitle(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    protected void validate() {
        if (value.length() > 200) {
            throw new IllegalArgumentException("文章标题长度不能超过200个字符");
        }
        if (value.length() < 1) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ArticleTitle that = (ArticleTitle) obj;
        return Objects.equals(value, that.value);
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