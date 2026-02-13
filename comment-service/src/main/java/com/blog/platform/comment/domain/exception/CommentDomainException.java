package com.blog.platform.comment.domain.exception;

import com.blog.platform.common.exception.DomainException;

/**
 * 评论领域异常
 */
public class CommentDomainException extends DomainException {
    
    public CommentDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public CommentDomainException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    // 具体的业务异常
    public static class CommentNotFoundException extends CommentDomainException {
        public CommentNotFoundException(String commentId) {
            super("COMMENT_NOT_FOUND", "评论不存在: " + commentId);
        }
    }
    
    public static class CommentNotEditableException extends CommentDomainException {
        public CommentNotEditableException(String commentId) {
            super("COMMENT_NOT_EDITABLE", "评论不可编辑: " + commentId);
        }
    }
    
    public static class CommentNotDeletableException extends CommentDomainException {
        public CommentNotDeletableException(String commentId) {
            super("COMMENT_NOT_DELETABLE", "评论不可删除: " + commentId);
        }
    }
    
    public static class CommentNotReplyableException extends CommentDomainException {
        public CommentNotReplyableException(String commentId) {
            super("COMMENT_NOT_REPLYABLE", "评论不可回复: " + commentId);
        }
    }
    
    public static class InvalidCommentContentException extends CommentDomainException {
        public InvalidCommentContentException(String message) {
            super("INVALID_COMMENT_CONTENT", "评论内容无效: " + message);
        }
    }
    
    public static class UnauthorizedCommentOperationException extends CommentDomainException {
        public UnauthorizedCommentOperationException(String operation) {
            super("UNAUTHORIZED_COMMENT_OPERATION", "无权限执行评论操作: " + operation);
        }
    }
}