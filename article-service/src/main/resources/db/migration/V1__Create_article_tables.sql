-- Article Service Database Schema Migration

-- Articles table
CREATE TABLE IF NOT EXISTS articles (
    id VARCHAR(36) PRIMARY KEY,
    author_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    summary TEXT,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    publish_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_author_id (author_id),
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time),
    INDEX idx_status_publish_time (status, publish_time),
    FULLTEXT idx_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Article statistics table
CREATE TABLE IF NOT EXISTS article_statistics (
    article_id VARCHAR(36) PRIMARY KEY,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Article tags table
CREATE TABLE IF NOT EXISTS article_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id),
    INDEX idx_tag_name (tag_name),
    UNIQUE KEY uk_article_tag (article_id, tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Article categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id BIGINT NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Article category mapping
CREATE TABLE IF NOT EXISTS article_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE KEY uk_article_category (article_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Article likes table
CREATE TABLE IF NOT EXISTS article_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    INDEX idx_article_id (article_id),
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_article_user_like (article_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;