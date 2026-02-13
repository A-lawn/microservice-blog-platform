package com.blog.platform.user.domain.exception;

import com.blog.platform.common.exception.DomainException;

/**
 * 用户领域异常
 */
public class UserDomainException extends DomainException {
    
    public UserDomainException(String message) {
        super("USER_DOMAIN_ERROR", message);
    }
    
    public UserDomainException(String message, Throwable cause) {
        super("USER_DOMAIN_ERROR", message, cause);
    }
}