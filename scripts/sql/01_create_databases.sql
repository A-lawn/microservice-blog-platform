-- ============================================================
-- 博客平台数据库初始化脚本
-- 使用方式：在 Navicat 中依次执行各部分脚本
-- ============================================================

-- ============================================================
-- 第一部分：创建数据库和用户
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS user_db 
    DEFAULT CHARSET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS article_db 
    DEFAULT CHARSET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS comment_db 
    DEFAULT CHARSET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权（请修改密码）
CREATE USER IF NOT EXISTS 'blog_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON user_db.* TO 'blog_user'@'%';
GRANT ALL PRIVILEGES ON article_db.* TO 'blog_user'@'%';
GRANT ALL PRIVILEGES ON comment_db.* TO 'blog_user'@'%';
FLUSH PRIVILEGES;
