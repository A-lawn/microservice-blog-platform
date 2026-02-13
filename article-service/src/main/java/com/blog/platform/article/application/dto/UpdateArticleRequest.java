package com.blog.platform.article.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新文章请求DTO
 */
public class UpdateArticleRequest {
    
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200个字符")
    private String title;
    
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    @Size(max = 500, message = "文章摘要长度不能超过500个字符")
    private String summary;
    
    // Default constructor
    public UpdateArticleRequest() {}
    
    // Constructor
    public UpdateArticleRequest(String title, String content, String summary) {
        this.title = title;
        this.content = content;
        this.summary = summary;
    }
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
}