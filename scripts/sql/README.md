# 数据库初始化指南

## 一、脚本文件说明

| 序号 | 文件名 | 说明 |
|------|--------|------|
| 01 | `01_create_databases.sql` | 创建数据库和用户 |
| 02 | `02_user_db_tables.sql` | user_db 表结构、索引、视图 |
| 03 | `03_article_db_tables.sql` | article_db 表结构、索引、视图 |
| 04 | `04_comment_db_tables.sql` | comment_db 表结构、索引、视图 |

## 二、Navicat 执行步骤

### 步骤 1：创建数据库

1. 打开 Navicat，连接到 MySQL 服务器
2. 点击菜单 **文件** → **运行 SQL 文件** 或按 `F6` 打开查询窗口
3. 打开 `01_create_databases.sql` 文件
4. 点击 **运行** 按钮执行
5. 刷新连接，确认看到三个数据库：
   - `user_db`
   - `article_db`
   - `comment_db`

### 步骤 2：创建 user_db 表结构

1. 在 Navicat 左侧双击 `user_db` 数据库
2. 点击菜单 **文件** → **运行 SQL 文件**
3. 选择 `02_user_db_tables.sql` 文件
4. 点击 **运行** 按钮执行
5. 刷新数据库，确认看到：
   - 6 个表：users, user_statistics, user_roles, user_sessions, user_follows, notifications, undo_log
   - 2 个视图：v_active_users, v_recently_active_users

### 步骤 3：创建 article_db 表结构

1. 在 Navicat 左侧双击 `article_db` 数据库
2. 点击菜单 **文件** → **运行 SQL 文件**
3. 选择 `03_article_db_tables.sql` 文件
4. 点击 **运行** 按钮执行
5. 刷新数据库，确认看到：
   - 9 个表：articles, article_statistics, categories, article_categories, tags, article_tags, article_likes, article_bookmarks, article_read_history, undo_log
   - 3 个视图：v_published_articles, v_popular_articles, v_article_with_stats

### 步骤 4：创建 comment_db 表结构

1. 在 Navicat 左侧双击 `comment_db` 数据库
2. 点击菜单 **文件** → **运行 SQL 文件**
3. 选择 `04_comment_db_tables.sql` 文件
4. 点击 **运行** 按钮执行
5. 刷新数据库，确认看到：
   - 5 个表：comments, comment_statistics, comment_likes, comment_reports, comment_notifications, undo_log
   - 2 个视图：v_comment_tree, v_recent_comments

## 三、验证安装

### 验证表数量

```sql
-- 在 user_db 中执行
SELECT COUNT(*) as table_count FROM information_schema.tables 
WHERE table_schema = 'user_db';
-- 预期结果：7（6个业务表 + 1个undo_log）

-- 在 article_db 中执行
SELECT COUNT(*) as table_count FROM information_schema.tables 
WHERE table_schema = 'article_db';
-- 预期结果：10（9个业务表 + 1个undo_log）

-- 在 comment_db 中执行
SELECT COUNT(*) as table_count FROM information_schema.tables 
WHERE table_schema = 'comment_db';
-- 预期结果：6（5个业务表 + 1个undo_log）
```

### 验证视图

```sql
-- 在 user_db 中执行
SHOW FULL TABLES WHERE Table_type = 'VIEW';
-- 预期结果：v_active_users, v_recently_active_users

-- 在 article_db 中执行
SHOW FULL TABLES WHERE Table_type = 'VIEW';
-- 预期结果：v_published_articles, v_popular_articles, v_article_with_stats

-- 在 comment_db 中执行
SHOW FULL TABLES WHERE Table_type = 'VIEW';
-- 预期结果：v_comment_tree, v_recent_comments
```

### 验证初始数据

```sql
-- 验证管理员用户
USE user_db;
SELECT * FROM users WHERE username = 'admin';

-- 验证分类数据
USE article_db;
SELECT * FROM categories;

-- 验证标签数据
SELECT * FROM tags;
```

## 四、数据库连接配置

修改各服务的 `application.yml` 中的数据库连接信息：

```yaml
# user-service
spring:
  datasource:
    user:
      jdbc-url: jdbc:mysql://localhost:3306/user_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC
      username: blog_user
      password: your_password

# article-service
spring:
  datasource:
    article:
      jdbc-url: jdbc:mysql://localhost:3306/article_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC
      username: blog_user
      password: your_password

# comment-service
spring:
  datasource:
    comment:
      jdbc-url: jdbc:mysql://localhost:3306/comment_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC
      username: blog_user
      password: your_password
```

## 五、常见问题

### Q1: 外键约束错误

如果遇到外键约束错误，请确保按顺序执行脚本。

### Q2: 全文索引错误

如果 MySQL 版本低于 5.6，全文索引可能不支持。请升级 MySQL 版本。

### Q3: 字符集问题

确保数据库字符集为 `utf8mb4`，以支持 emoji 等特殊字符：

```sql
-- 检查数据库字符集
SHOW CREATE DATABASE user_db;
SHOW CREATE DATABASE article_db;
SHOW CREATE DATABASE comment_db;
```

### Q4: 权限问题

如果遇到权限错误，请使用 root 用户授权：

```sql
GRANT ALL PRIVILEGES ON *.* TO 'blog_user'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

## 六、清理脚本（如需重新初始化）

```sql
-- 警告：以下脚本会删除所有数据，请谨慎使用！

DROP DATABASE IF EXISTS user_db;
DROP DATABASE IF EXISTS article_db;
DROP DATABASE IF EXISTS comment_db;
DROP USER IF EXISTS 'blog_user'@'%';
```
