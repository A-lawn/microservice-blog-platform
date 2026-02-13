-- ============================================================
-- 第二部分：user_db 数据库表结构
-- 在 Navicat 中选择 user_db 数据库后执行
-- ============================================================

USE user_db;

-- ------------------------------------------------------------
-- 用户主表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    bio TEXT COMMENT '个人简介',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE' COMMENT '状态',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    login_count INT DEFAULT 0 COMMENT '登录次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '软删除时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_email_status (email, status),
    INDEX idx_username_status (username, status),
    INDEX idx_status_created (status, created_at DESC),
    INDEX idx_created_at_desc (created_at DESC),
    INDEX idx_phone (phone),
    INDEX idx_last_login (last_login_at DESC),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主表';

-- ------------------------------------------------------------
-- 用户统计表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_statistics (
    user_id VARCHAR(36) PRIMARY KEY COMMENT '用户ID',
    article_count BIGINT DEFAULT 0 COMMENT '文章数',
    comment_count BIGINT DEFAULT 0 COMMENT '评论数',
    like_count BIGINT DEFAULT 0 COMMENT '获赞数',
    follower_count BIGINT DEFAULT 0 COMMENT '粉丝数',
    following_count BIGINT DEFAULT 0 COMMENT '关注数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_article_count_desc (article_count DESC),
    INDEX idx_comment_count_desc (comment_count DESC),
    INDEX idx_follower_count_desc (follower_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';

-- ------------------------------------------------------------
-- 用户角色表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_name),
    INDEX idx_user_role (user_id, role_name),
    INDEX idx_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色表';

-- ------------------------------------------------------------
-- 用户会话表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    token_hash VARCHAR(255) NOT NULL COMMENT 'Token哈希',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_token_hash (token_hash),
    INDEX idx_expires_at (expires_at),
    INDEX idx_user_expires (user_id, expires_at),
    INDEX idx_token_expires (token_hash, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- ------------------------------------------------------------
-- 用户关注表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id VARCHAR(36) NOT NULL COMMENT '关注者ID',
    following_id VARCHAR(36) NOT NULL COMMENT '被关注者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_follow (follower_id, following_id),
    INDEX idx_following (following_id, created_at DESC),
    INDEX idx_follower (follower_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';

-- ------------------------------------------------------------
-- 用户通知表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL COMMENT '接收通知的用户ID',
    type ENUM('SYSTEM', 'LIKE', 'COMMENT', 'FOLLOW', 'MENTION') NOT NULL COMMENT '通知类型',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    reference_id VARCHAR(36) COMMENT '关联业务ID',
    reference_type VARCHAR(50) COMMENT '关联业务类型',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_read (user_id, is_read, created_at DESC),
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_type_created (type, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知表';

-- ------------------------------------------------------------
-- Seata 分布式事务日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    xid VARCHAR(100) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME NOT NULL,
    log_modified DATETIME NOT NULL,
    UNIQUE KEY uk_xid_branch (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata分布式事务日志表';

-- ------------------------------------------------------------
-- 视图：活跃用户视图
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_active_users AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.nickname,
    u.avatar_url,
    u.bio,
    u.created_at,
    s.article_count,
    s.comment_count,
    s.follower_count,
    s.following_count
FROM users u
LEFT JOIN user_statistics s ON u.id = s.user_id
WHERE u.status = 'ACTIVE' AND u.deleted_at IS NULL;

-- ------------------------------------------------------------
-- 视图：最近活跃用户视图
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_recently_active_users AS
SELECT 
    u.id,
    u.username,
    u.nickname,
    u.avatar_url,
    u.created_at,
    s.article_count,
    s.comment_count,
    MAX(sess.created_at) as last_active_time
FROM users u
LEFT JOIN user_statistics s ON u.id = s.user_id
LEFT JOIN user_sessions sess ON u.id = sess.user_id
WHERE u.status = 'ACTIVE' AND u.deleted_at IS NULL
GROUP BY u.id, u.username, u.nickname, u.avatar_url, u.created_at, s.article_count, s.comment_count
HAVING last_active_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY last_active_time DESC;

-- ------------------------------------------------------------
-- 初始数据：管理员用户
-- ------------------------------------------------------------
INSERT INTO users (id, username, email, password_hash, nickname, status) VALUES
('admin-0001-0001-0001-000000000001', 'admin', 'admin@blog.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'ACTIVE');

INSERT INTO user_statistics (user_id) VALUES
('admin-0001-0001-0001-000000000001');

INSERT INTO user_roles (user_id, role_name) VALUES
('admin-0001-0001-0001-000000000001', 'ADMIN'),
('admin-0001-0001-0001-000000000001', 'USER');
