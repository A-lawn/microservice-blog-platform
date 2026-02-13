package com.blog.platform.common.domain.article;

import com.blog.platform.common.domain.ValueObject;

import java.util.Objects;

/**
 * 文章内容值对象
 */
public class ArticleContent extends ValueObject {
    
    private final String value;
    private final String summary;
    
    public ArticleContent(String value, String summary) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        this.value = value.trim();
        this.summary = summary != null ? summary.trim() : generateSummary(value);
        validate();
    }
    
    public ArticleContent(String value) {
        this(value, null);
    }
    
    public static ArticleContent of(String value) {
        return new ArticleContent(value);
    }
    
    public static ArticleContent of(String value, String summary) {
        return new ArticleContent(value, summary);
    }
    
    public String getValue() {
        return value;
    }
    
    public String getSummary() {
        return summary;
    }
    
    /**
     * 生成文章摘要
     */
    private String generateSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // 简单的摘要生成逻辑：取前200个字符
        String cleanContent = content.replaceAll("<[^>]*>", "").trim(); // 移除HTML标签
        if (cleanContent.length() <= 200) {
            return cleanContent;
        }
        
        return cleanContent.substring(0, 200) + "...";
    }
    
    @Override
    protected void validate() {
        if (value.length() > 100000) {
            throw new IllegalArgumentException("文章内容长度不能超过100000个字符");
        }
        if (summary != null && summary.length() > 500) {
            throw new IllegalArgumentException("文章摘要长度不能超过500个字符");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ArticleContent that = (ArticleContent) obj;
        return Objects.equals(value, that.value) && Objects.equals(summary, that.summary);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, summary);
    }
    
    @Override
    public String toString() {
        return String.format("ArticleContent{length=%d, summary='%s'}", 
                value.length(), summary != null ? summary.substring(0, Math.min(50, summary.length())) : "");
    }
}