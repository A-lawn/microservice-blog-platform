-- Article Service Database Schema Enhancement

-- 添加文章表缺失字段
ALTER TABLE articles
ADD COLUMN cover_image VARCHAR(255) NULL COMMENT '封面图片URL',
ADD COLUMN slug VARCHAR(250) NULL UNIQUE COMMENT 'URL友好标识',
ADD COLUMN deleted_at TIMESTAMP NULL COMMENT '软删除时间',
ADD INDEX idx_slug (slug),
ADD INDEX idx_deleted_at (deleted_at);

-- 修改统计表字段类型并添加收藏数字段
ALTER TABLE article_statistics
MODIFY COLUMN view_count BIGINT DEFAULT 0 COMMENT '浏览数',
MODIFY COLUMN like_count BIGINT DEFAULT 0 COMMENT '点赞数',
MODIFY COLUMN comment_count BIGINT DEFAULT 0 COMMENT '评论数',
MODIFY COLUMN share_count BIGINT DEFAULT 0 COMMENT '分享数',
ADD COLUMN bookmark_count BIGINT DEFAULT 0 COMMENT '收藏数' AFTER share_count,
ADD INDEX idx_bookmark_count_desc (bookmark_count DESC);

-- 创建标签主表
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    slug VARCHAR(60) NULL UNIQUE COMMENT 'URL友好标识',
    description TEXT COMMENT '标签描述',
    article_count INT DEFAULT 0 COMMENT '文章数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_article_count (article_count DESC),
    INDEX idx_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签主表';

-- 修改文章标签表关联标签主表
ALTER TABLE article_tags
ADD COLUMN tag_id BIGINT NULL COMMENT '标签ID',
ADD INDEX idx_tag_id (tag_id);

-- 创建文章收藏表
CREATE TABLE IF NOT EXISTS article_bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL COMMENT '文章ID',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_bookmark (article_id, user_id),
    INDEX idx_user_bookmarks (user_id, created_at DESC),
    INDEX idx_article_bookmarks (article_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章收藏表';

-- 创建文章阅读历史表
CREATE TABLE IF NOT EXISTS article_read_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL COMMENT '文章ID',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    read_duration INT DEFAULT 0 COMMENT '阅读时长(秒)',
    read_progress DECIMAL(5,2) DEFAULT 0 COMMENT '阅读进度(百分比)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_history (user_id, created_at DESC),
    INDEX idx_article_history (article_id, created_at DESC),
    UNIQUE KEY uk_user_article_recent (user_id, article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章阅读历史表';
