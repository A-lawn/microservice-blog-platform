package com.blog.platform.common.domain.comment;

public enum CommentStatus {
    
    ACTIVE("活跃"),
    
    HIDDEN("隐藏"),
    
    DELETED("删除"),
    
    PENDING("待审核"),
    
    APPROVED("已通过"),
    
    REJECTED("已拒绝");
    
    private final String description;
    
    CommentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isVisible() {
        return this == ACTIVE || this == APPROVED;
    }
    
    public boolean canEdit() {
        return this == ACTIVE || this == PENDING;
    }
    
    public boolean canDelete() {
        return this == ACTIVE || this == PENDING || this == HIDDEN;
    }
    
    public boolean canReply() {
        return this == ACTIVE || this == APPROVED;
    }
    
    public boolean canApprove() {
        return this == PENDING;
    }
    
    public boolean canHide() {
        return this == ACTIVE || this == PENDING || this == APPROVED;
    }
    
    public boolean canRestore() {
        return this == HIDDEN || this == DELETED || this == REJECTED;
    }
    
    @Override
    public String toString() {
        return description;
    }
}