package com.blog.platform.comment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建评论请求
 */
public class CreateCommentRequest {
    
    @NotBlank(message = "文章ID不能为空")
    private String articleId;
    
    @NotBlank(message = "作者ID不能为空")
    private String authorId;
    
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 5000, message = "评论内容长度必须在1-5000字符之间")
    private String content;
    
    public CreateCommentRequest() {}
    
    public CreateCommentRequest(String articleId, String authorId, String content) {
        this.articleId = articleId;
        this.authorId = authorId;
        this.content = content;
    }
    
    public String getArticleId() {
        return articleId;
    }
    
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}
