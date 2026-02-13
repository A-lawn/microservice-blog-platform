-- User Service Database Performance Optimization

-- 优化用户表索引
ALTER TABLE users
ADD INDEX idx_email_status (email, status),
ADD INDEX idx_username_status (username, status),
ADD INDEX idx_status_created (status, created_at DESC),
ADD INDEX idx_created_at_desc (created_at DESC);

-- 优化用户统计表索引
ALTER TABLE user_statistics
ADD INDEX idx_article_count_desc (article_count DESC),
ADD INDEX idx_comment_count_desc (comment_count DESC),
ADD INDEX idx_follower_count_desc (follower_count DESC);

-- 优化用户会话表索引
-- 注意：idx_expires_at 已在 V1 中创建，这里只添加复合索引
ALTER TABLE user_sessions
ADD INDEX idx_user_expires (user_id, expires_at),
ADD INDEX idx_token_expires (token_hash, expires_at);

-- 优化用户角色表索引
ALTER TABLE user_roles
ADD INDEX idx_user_role (user_id, role_name),
ADD INDEX idx_role_name (role_name);

-- 创建用户相关视图
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
WHERE u.status = 'ACTIVE';

-- 创建活跃用户视图（基于最近活动）
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
WHERE u.status = 'ACTIVE'
GROUP BY u.id, u.username, u.nickname, u.avatar_url, u.created_at, s.article_count, s.comment_count
HAVING last_active_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY last_active_time DESC;
