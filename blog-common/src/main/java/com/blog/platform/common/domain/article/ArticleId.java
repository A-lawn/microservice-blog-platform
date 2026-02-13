package com.blog.platform.common.domain.article;

import com.blog.platform.common.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * 文章ID值对象
 */
public class ArticleId extends ValueObject {
    
    private final String value;
    
    public ArticleId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        this.value = value.trim();
        validate();
    }
    
    public static ArticleId generate() {
        return new ArticleId(UUID.randomUUID().toString());
    }
    
    public static ArticleId of(String value) {
        return new ArticleId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    protected void validate() {
        if (value.length() > 50) {
            throw new IllegalArgumentException("文章ID长度不能超过50个字符");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ArticleId articleId = (ArticleId) obj;
        return Objects.equals(value, articleId.value);
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