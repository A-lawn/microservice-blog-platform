-- Comment Service Database Performance Optimization

-- 优化评论表索引
-- 注意：idx_article_status_created 已在 V1 中创建
ALTER TABLE comments
ADD INDEX idx_author_status_created (author_id, status, created_at DESC),
ADD INDEX idx_parent_created (parent_id, created_at DESC),
ADD INDEX idx_status_created (status, created_at DESC),
ADD INDEX idx_created_at_desc (created_at DESC);

-- 优化评论统计表索引
ALTER TABLE comment_statistics
ADD INDEX idx_like_count_desc (like_count DESC),
ADD INDEX idx_reply_count_desc (reply_count DESC);

-- 优化评论点赞表索引
ALTER TABLE comment_likes
ADD INDEX idx_comment_created (comment_id, created_at DESC),
ADD INDEX idx_user_created (user_id, created_at DESC);

-- 优化评论举报表索引
ALTER TABLE comment_reports
ADD INDEX idx_comment_status (comment_id, status),
ADD INDEX idx_reporter_created (reporter_id, created_at DESC),
ADD INDEX idx_status_created (status, created_at DESC);

-- 优化评论通知表索引
-- 注意：表中字段是 recipient_id 和 type，不是 user_id 和 notification_type
ALTER TABLE comment_notifications
ADD INDEX idx_recipient_read_created (recipient_id, is_read, created_at DESC),
ADD INDEX idx_comment_type (comment_id, type);

-- 创建评论相关视图
CREATE OR REPLACE VIEW v_active_comments AS
SELECT 
    c.id,
    c.article_id,
    c.author_id,
    c.parent_id,
    c.content,
    c.created_at,
    c.updated_at,
    s.like_count,
    s.reply_count
FROM comments c
LEFT JOIN comment_statistics s ON c.id = s.comment_id
WHERE c.status = 'ACTIVE';

-- 创建文章评论树视图（用于展示评论层级）
CREATE OR REPLACE VIEW v_comment_tree AS
SELECT 
    c.id,
    c.article_id,
    c.author_id,
    c.parent_id,
    c.content,
    c.created_at,
    s.like_count,
    s.reply_count,
    CASE 
        WHEN c.parent_id IS NULL THEN 0
        ELSE 1
    END as level
FROM comments c
LEFT JOIN comment_statistics s ON c.id = s.comment_id
WHERE c.status = 'ACTIVE'
ORDER BY 
    CASE WHEN c.parent_id IS NULL THEN c.id ELSE c.parent_id END,
    c.parent_id IS NULL DESC,
    c.created_at ASC;

-- 创建热门评论视图
CREATE OR REPLACE VIEW v_popular_comments AS
SELECT 
    c.id,
    c.article_id,
    c.author_id,
    c.content,
    c.created_at,
    s.like_count,
    s.reply_count,
    -- 计算评论热度分数
    (s.like_count * 0.7 + s.reply_count * 0.3) as popularity_score
FROM comments c
LEFT JOIN comment_statistics s ON c.id = s.comment_id
WHERE c.status = 'ACTIVE'
  AND c.parent_id IS NULL -- 只考虑顶级评论
  AND c.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) -- 最近7天的评论
ORDER BY popularity_score DESC;