-- Database Performance Optimization Migration

-- 优化文章表索引
-- 添加复合索引用于常见查询模式
ALTER TABLE articles 
ADD INDEX idx_author_status_publish (author_id, status, publish_time DESC),
ADD INDEX idx_status_created (status, created_at DESC),
ADD INDEX idx_publish_time_desc (publish_time DESC),
ADD INDEX idx_updated_at (updated_at DESC);

-- 优化文章统计表索引
-- 添加用于排序的索引
ALTER TABLE article_statistics
ADD INDEX idx_view_count_desc (view_count DESC),
ADD INDEX idx_like_count_desc (like_count DESC),
ADD INDEX idx_comment_count_desc (comment_count DESC);

-- 优化文章标签表索引
-- 添加用于标签查询的复合索引
ALTER TABLE article_tags
ADD INDEX idx_tag_article (tag_name, article_id);

-- 优化文章分类映射表索引
ALTER TABLE article_categories
ADD INDEX idx_category_article (category_id, article_id);

-- 优化文章点赞表索引
-- 添加用于统计的索引
ALTER TABLE article_likes
ADD INDEX idx_article_created (article_id, created_at DESC);

-- 创建分区表（如果数据量大）
-- 注意：这里只是示例，实际使用时需要根据数据量决定是否分区

-- 为文章表添加分区（按年份分区）
-- ALTER TABLE articles PARTITION BY RANGE (YEAR(created_at)) (
--     PARTITION p2023 VALUES LESS THAN (2024),
--     PARTITION p2024 VALUES LESS THAN (2025),
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 创建视图用于常见查询
CREATE OR REPLACE VIEW v_published_articles AS
SELECT 
    a.id,
    a.author_id,
    a.title,
    a.summary,
    a.publish_time,
    a.created_at,
    a.updated_at,
    s.view_count,
    s.like_count,
    s.comment_count,
    s.share_count
FROM articles a
LEFT JOIN article_statistics s ON a.id = s.article_id
WHERE a.status = 'PUBLISHED'
  AND a.publish_time IS NOT NULL
  AND a.publish_time <= NOW();

-- 创建热门文章视图
CREATE OR REPLACE VIEW v_popular_articles AS
SELECT 
    a.id,
    a.author_id,
    a.title,
    a.summary,
    a.publish_time,
    s.view_count,
    s.like_count,
    s.comment_count,
    -- 计算热度分数（可以根据业务需求调整权重）
    (s.view_count * 0.1 + s.like_count * 0.3 + s.comment_count * 0.6) as popularity_score
FROM articles a
LEFT JOIN article_statistics s ON a.id = s.article_id
WHERE a.status = 'PUBLISHED'
  AND a.publish_time IS NOT NULL
  AND a.publish_time <= NOW()
  AND a.publish_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) -- 只考虑最近30天的文章
ORDER BY popularity_score DESC;

-- 添加数据库级别的约束和优化
-- 设置合适的字符集和排序规则
ALTER TABLE articles CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE article_statistics CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE article_tags CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE categories CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE article_categories CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE article_likes CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;