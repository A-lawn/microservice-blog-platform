package com.blog.platform.article.domain.exception;

import com.blog.platform.common.exception.DomainException;

/**
 * 文章领域异常
 */
public class ArticleDomainException extends DomainException {
    
    public ArticleDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public ArticleDomainException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}