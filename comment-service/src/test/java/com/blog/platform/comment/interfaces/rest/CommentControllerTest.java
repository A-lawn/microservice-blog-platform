package com.blog.platform.comment.interfaces.rest;

import com.blog.platform.comment.application.dto.CreateCommentRequest;
import com.blog.platform.comment.application.service.CommentApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CommentApplicationService commentApplicationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldReturnBadRequestForInvalidCreateCommentRequest() throws Exception {
        // Given
        CreateCommentRequest request = new CreateCommentRequest();
        request.setArticleId(""); // Invalid - empty
        request.setAuthorId("author-1");
        request.setContent("Test comment");
        
        // When & Then
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnBadRequestForEmptyContent() throws Exception {
        // Given
        CreateCommentRequest request = new CreateCommentRequest();
        request.setArticleId("article-1");
        request.setAuthorId("author-1");
        request.setContent(""); // Invalid - empty
        
        // When & Then
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnOkForHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/comments/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment Service is running"));
    }
}