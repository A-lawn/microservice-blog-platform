-- ============================================================
-- 第四部分：comment_db 数据库表结构
-- 在 Navicat 中选择 comment_db 数据库后执行
-- ============================================================

USE comment_db;

-- ------------------------------------------------------------
-- 评论主表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comments (
    id VARCHAR(36) PRIMARY KEY COMMENT '评论ID',
    article_id VARCHAR(36) NOT NULL COMMENT '文章ID',
    author_id VARCHAR(36) NOT NULL COMMENT '作者ID',
    parent_id VARCHAR(36) NULL COMMENT '父评论ID',
    content TEXT NOT NULL COMMENT '评论内容',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    status ENUM('ACTIVE', 'HIDDEN', 'DELETED') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '软删除时间',
    INDEX idx_article_id (article_id),
    INDEX idx_author_id (author_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_article_status_created (article_id, status, created_at),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_ip_address (ip_address),
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论主表';

-- ------------------------------------------------------------
-- 评论统计表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comment_statistics (
    comment_id VARCHAR(36) PRIMARY KEY COMMENT '评论ID',
    like_count BIGINT DEFAULT 0 COMMENT '点赞数',
    reply_count BIGINT DEFAULT 0 COMMENT '回复数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论统计表';

-- ------------------------------------------------------------
-- 评论点赞表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id VARCHAR(36) NOT NULL COMMENT '评论ID',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_comment_id (comment_id),
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_comment_user_like (comment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- ------------------------------------------------------------
-- 评论举报表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comment_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id VARCHAR(36) NOT NULL COMMENT '评论ID',
    reporter_id VARCHAR(36) NOT NULL COMMENT '举报者ID',
    reason ENUM('SPAM', 'INAPPROPRIATE', 'HARASSMENT', 'OTHER') NOT NULL COMMENT '举报原因',
    description TEXT COMMENT '详细描述',
    status ENUM('PENDING', 'REVIEWED', 'RESOLVED') DEFAULT 'PENDING' COMMENT '处理状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_comment_id (comment_id),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论举报表';

-- ------------------------------------------------------------
-- 评论通知表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comment_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id VARCHAR(36) NOT NULL COMMENT '评论ID',
    recipient_id VARCHAR(36) NOT NULL COMMENT '接收者ID',
    type ENUM('REPLY', 'MENTION', 'LIKE') NOT NULL COMMENT '通知类型',
    title VARCHAR(200) NULL COMMENT '通知标题',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_comment_id (comment_id),
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_recipient_read (recipient_id, is_read),
    INDEX idx_user_deleted (recipient_id, is_deleted, created_at DESC),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论通知表';

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
-- 视图：评论树视图
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_comment_tree AS
WITH RECURSIVE comment_tree AS (
    SELECT 
        c.id,
        c.article_id,
        c.author_id,
        c.parent_id,
        c.content,
        c.status,
        c.created_at,
        c.updated_at,
        0 as level,
        CAST(c.id AS CHAR(1000)) as path
    FROM comments c
    WHERE c.parent_id IS NULL AND c.deleted_at IS NULL
    
    UNION ALL
    
    SELECT 
        c.id,
        c.article_id,
        c.author_id,
        c.parent_id,
        c.content,
        c.status,
        c.created_at,
        c.updated_at,
        ct.level + 1,
        CONCAT(ct.path, '/', c.id)
    FROM comments c
    INNER JOIN comment_tree ct ON c.parent_id = ct.id
    WHERE c.deleted_at IS NULL
)
SELECT 
    ct.*,
    COALESCE(s.like_count, 0) as like_count,
    COALESCE(s.reply_count, 0) as reply_count
FROM comment_tree ct
LEFT JOIN comment_statistics s ON ct.id = s.comment_id
WHERE ct.status = 'ACTIVE'
ORDER BY ct.path;

-- ------------------------------------------------------------
-- 视图：最近评论视图
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_recent_comments AS
SELECT 
    c.id,
    c.article_id,
    c.author_id,
    c.parent_id,
    c.content,
    c.created_at,
    COALESCE(s.like_count, 0) as like_count,
    COALESCE(s.reply_count, 0) as reply_count
FROM comments c
LEFT JOIN comment_statistics s ON c.id = s.comment_id
WHERE c.status = 'ACTIVE' AND c.deleted_at IS NULL
ORDER BY c.created_at DESC
LIMIT 100;
