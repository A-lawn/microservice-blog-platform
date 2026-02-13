-- Comment Service Database Schema Enhancement

-- 添加评论表缺失字段
ALTER TABLE comments
ADD COLUMN ip_address VARCHAR(45) NULL COMMENT '评论IP地址',
ADD COLUMN user_agent VARCHAR(500) NULL COMMENT '用户代理',
ADD COLUMN deleted_at TIMESTAMP NULL COMMENT '软删除时间',
ADD INDEX idx_deleted_at (deleted_at),
ADD INDEX idx_ip_address (ip_address);

-- 修改统计表字段类型
ALTER TABLE comment_statistics
MODIFY COLUMN like_count BIGINT DEFAULT 0 COMMENT '点赞数',
MODIFY COLUMN reply_count BIGINT DEFAULT 0 COMMENT '回复数';

-- 添加评论通知表缺失字段
ALTER TABLE comment_notifications
ADD COLUMN title VARCHAR(200) NULL COMMENT '通知标题',
ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除',
ADD INDEX idx_user_deleted (recipient_id, is_deleted, created_at DESC);
