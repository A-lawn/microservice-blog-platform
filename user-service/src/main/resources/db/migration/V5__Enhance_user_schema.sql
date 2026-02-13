-- User Service Database Schema Enhancement

-- 添加用户表缺失字段
ALTER TABLE users
ADD COLUMN phone VARCHAR(20) NULL UNIQUE COMMENT '手机号',
ADD COLUMN last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
ADD COLUMN login_count INT DEFAULT 0 COMMENT '登录次数',
ADD COLUMN deleted_at TIMESTAMP NULL COMMENT '软删除时间',
ADD INDEX idx_phone (phone),
ADD INDEX idx_last_login (last_login_at DESC),
ADD INDEX idx_deleted_at (deleted_at);

-- 创建用户关注表
CREATE TABLE IF NOT EXISTS user_follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id VARCHAR(36) NOT NULL COMMENT '关注者ID',
    following_id VARCHAR(36) NOT NULL COMMENT '被关注者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_follow (follower_id, following_id),
    INDEX idx_following (following_id, created_at DESC),
    INDEX idx_follower (follower_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';

-- 创建用户通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL COMMENT '接收通知的用户ID',
    type ENUM('SYSTEM', 'LIKE', 'COMMENT', 'FOLLOW', 'MENTION') NOT NULL COMMENT '通知类型',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    reference_id VARCHAR(36) COMMENT '关联业务ID',
    reference_type VARCHAR(50) COMMENT '关联业务类型',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read, created_at DESC),
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_type_created (type, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知表';
