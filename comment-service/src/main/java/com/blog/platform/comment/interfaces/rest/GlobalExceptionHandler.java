package com.blog.platform.comment.interfaces.rest;

import com.blog.platform.comment.domain.exception.CommentDomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理评论领域异常
     */
    @ExceptionHandler(CommentDomainException.class)
    public ResponseEntity<ErrorResponse> handleCommentDomainException(CommentDomainException e) {
        logger.warn("Comment domain exception: {}", e.getMessage());
        
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        // 根据具体异常类型设置不同的HTTP状态码
        if (e instanceof CommentDomainException.CommentNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (e instanceof CommentDomainException.UnauthorizedCommentOperationException) {
            status = HttpStatus.FORBIDDEN;
        }
        
        ErrorResponse error = new ErrorResponse(
                status.value(),
                e.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(status).body(error);
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        logger.warn("Validation exception: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "参数验证失败",
                LocalDateTime.now(),
                errors
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        logger.warn("Bind exception: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "参数绑定失败",
                LocalDateTime.now(),
                errors
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Illegal argument exception: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        logger.warn("Illegal state exception: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                e.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        logger.error("Unexpected exception occurred", e);
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "服务器内部错误",
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * 错误响应类
     */
    public static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, String> details;
        
        public ErrorResponse(int status, String message, LocalDateTime timestamp) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public ErrorResponse(int status, String message, LocalDateTime timestamp, Map<String, String> details) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
            this.details = details;
        }
        
        // Getters and setters
        public int getStatus() {
            return status;
        }
        
        public void setStatus(int status) {
            this.status = status;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public Map<String, String> getDetails() {
            return details;
        }
        
        public void setDetails(Map<String, String> details) {
            this.details = details;
        }
    }
}