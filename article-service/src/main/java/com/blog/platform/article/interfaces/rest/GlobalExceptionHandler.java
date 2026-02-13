package com.blog.platform.article.interfaces.rest;

import com.blog.platform.article.domain.exception.ArticleDomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理领域异常
     */
    @ExceptionHandler(ArticleDomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(ArticleDomainException e) {
        logger.warn("Domain exception occurred: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse(e.getErrorCode(), e.getMessage());
        
        // Map specific error codes to HTTP status codes
        HttpStatus status = switch (e.getErrorCode()) {
            case "ARTICLE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "UNAUTHORIZED" -> HttpStatus.FORBIDDEN;
            case "INVALID_ARTICLE_DATA", "INVALID_AUTHOR" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
        
        return ResponseEntity.status(status).body(error);
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        logger.warn("Validation exception occurred: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", "请求参数验证失败", errors);
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        logger.warn("Bind exception occurred: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = new ErrorResponse("BIND_ERROR", "请求参数绑定失败", errors);
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Illegal argument exception occurred: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse("INVALID_ARGUMENT", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        logger.warn("Illegal state exception occurred: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse("INVALID_STATE", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        logger.error("Unexpected error occurred", e);
        
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "服务器内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * 错误响应DTO
     */
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private Map<String, String> details;
        
        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }
        
        public ErrorResponse(String errorCode, String message, Map<String, String> details) {
            this.errorCode = errorCode;
            this.message = message;
            this.details = details;
        }
        
        // Getters and setters
        public String getErrorCode() {
            return errorCode;
        }
        
        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public Map<String, String> getDetails() {
            return details;
        }
        
        public void setDetails(Map<String, String> details) {
            this.details = details;
        }
    }
}