-- ============================================================
-- 第三部分：article_db 数据库表结构
-- 在 Navicat 中选择 article_db 数据库后执行
-- ============================================================

USE article_db;

-- ------------------------------------------------------------
-- 文章主表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS articles (
    id VARCHAR(36) PRIMARY KEY COMMENT '文章ID',
    author_id VARCHAR(36) NOT NULL COMMENT '作者ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    slug VARCHAR(250) UNIQUE COMMENT 'URL友好标识',
    content LONGTEXT NOT NULL COMMENT '内容',
    summary TEXT COMMENT '摘要',
    cover_image VARCHAR(255) COMMENT '封面图片URL',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT' COMMENT '状态',
    publish_time TIMESTAMP NULL COMMENT '发布时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '软删除时间',
    INDEX idx_author_id (author_id),
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time),
    INDEX idx_status_publish_time (status, publish_time),
    INDEX idx_author_status_publish (author_id, status, publish_time DESC),
    INDEX idx_status_created (status, created_at DESC),
    INDEX idx_slug (slug),
    INDEX idx_deleted_at (deleted_at),
    FULLTEXT idx_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章主表';

-- ------------------------------------------------------------
-- 文章统计表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS article_statistics (
    article_id VARCHAR(36) PRIMARY KEY COMMENT '文章ID',
    view_count BIGINT DEFAULT 0 COMMENT '浏览数',
    like_count BIGINT DEFAULT 0 COMMENT '点赞数',
    comment_count BIGINT DEFAULT 0 COMMENT '评论数',
    share_count BIGINT DEFAULT 0 COMMENT '分享数',
    bookmark_count BIGINT DEFAULT 0 COMMENT '收藏数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    INDEX idx_view_count_desc (view_count DESC),
    INDEX idx_like_count_desc (like_count DESC),
    INDEX idx_bookmark_count_desc (bookmark_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章统计表';

-- ------------------------------------------------------------
-- 分类表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    parent_id BIGINT NULL COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ------------------------------------------------------------
-- 文章分类关联表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS article_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL COMMENT '文章ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE KEY uk_article_category (article_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类关联表';

-- ------------------------------------------------------------
-- 标签主表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    slug VARCHAR(60) UNIQUE COMMENT 'URL友好标识',
    description TEXT COMMENT '标签描述',
    article_count INT DEFAULT 0 COMMENT '文章数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_article_count (article_count DESC),
    INDEX idx_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签主表';

-- ------------------------------------------------------------
-- 文章标签关联表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS article_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL COMMENT '文章ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_id BIGINT NULL COMMENT '标签ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id),
    INDEX idx_tag_name (tag_name),
    INDEX idx_tag_id (tag_id),
    UNIQUE KEY uk_article_tag (article_id, tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- ------------------------------------------------------------
-- 文章点赞表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS article_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL COMMENT '文章ID',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id),
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_article_user_like (article_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';

-- ------------------------------------------------------------
-- 文章收藏表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS article_bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL COMMENT '文章ID',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_bookmark (article_id, user_id),
    INDEX idx_user_bookmarks (user_id, created_at DESC),
    INDEX idx_article_bookmarks (article_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章收藏表';

-- ------------------------------------------------------------
-- 文章阅读历史表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS article_read_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL COMMENT '文章ID',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    read_duration INT DEFAULT 0 COMMENT '阅读时长(秒)',
    read_progress DECIMAL(5,2) DEFAULT 0 COMMENT '阅读进度(百分比)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_history (user_id, created_at DESC),
    INDEX idx_article_history (article_id, created_at DESC),
    UNIQUE KEY uk_user_article_recent (user_id, article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章阅读历史表';

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
-- 视图：已发布文章视图
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_published_articles AS
SELECT 
    a.id,
    a.author_id,
    a.title,
    a.slug,
    a.summary,
    a.cover_image,
    a.publish_time,
    a.created_at,
    a.updated_at,
    s.view_count,
    s.like_count,
    s.comment_count,
    s.share_count,
    s.bookmark_count
FROM articles a
LEFT JOIN article_statistics s ON a.id = s.article_id
WHERE a.status = 'PUBLISHED' AND a.deleted_at IS NULL;

-- ------------------------------------------------------------
-- 视图：热门文章视图
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_popular_articles AS
SELECT 
    a.id,
    a.author_id,
    a.title,
    a.summary,
    a.cover_image,
    a.publish_time,
    s.view_count,
    s.like_count,
    s.comment_count
FROM articles a
LEFT JOIN article_statistics s ON a.id = s.article_id
WHERE a.status = 'PUBLISHED' AND a.deleted_at IS NULL
ORDER BY s.view_count DESC, s.like_count DESC
LIMIT 100;

-- ------------------------------------------------------------
-- 视图：文章统计视图
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_article_with_stats AS
SELECT 
    a.*,
    COALESCE(s.view_count, 0) as view_count,
    COALESCE(s.like_count, 0) as like_count,
    COALESCE(s.comment_count, 0) as comment_count,
    COALESCE(s.share_count, 0) as share_count,
    COALESCE(s.bookmark_count, 0) as bookmark_count
FROM articles a
LEFT JOIN article_statistics s ON a.id = s.article_id
WHERE a.deleted_at IS NULL;

-- ------------------------------------------------------------
-- 初始数据：默认分类
-- ------------------------------------------------------------
INSERT INTO categories (name, description, sort_order) VALUES
('技术', '技术相关文章', 1),
('生活', '生活随笔', 2),
('随笔', '个人随笔', 3),
('教程', '教程指南', 4),
('资讯', '行业资讯', 5);

-- ------------------------------------------------------------
-- 初始数据：默认标签
-- ------------------------------------------------------------
INSERT INTO tags (name, slug, description) VALUES
('Java', 'java', 'Java编程语言'),
('Spring', 'spring', 'Spring框架'),
('MySQL', 'mysql', 'MySQL数据库'),
('微服务', 'microservice', '微服务架构'),
('Docker', 'docker', 'Docker容器技术');
