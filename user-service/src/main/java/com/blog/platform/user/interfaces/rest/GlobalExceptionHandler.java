package com.blog.platform.user.interfaces.rest;

import com.blog.platform.user.domain.exception.UserDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserDomainException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleDomainException(UserDomainException e) {
        Map<String, String> details = new HashMap<>();
        details.put("error", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, e.getMessage(), details));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, "参数验证失败", errors));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误: " + e.getMessage()));
    }
}
