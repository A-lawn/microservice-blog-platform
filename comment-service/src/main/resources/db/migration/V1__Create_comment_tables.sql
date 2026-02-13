-- Comment Service Database Schema Migration

-- Comments table
CREATE TABLE IF NOT EXISTS comments (
    id VARCHAR(36) PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL,
    author_id VARCHAR(36) NOT NULL,
    parent_id VARCHAR(36) NULL,
    content TEXT NOT NULL,
    status ENUM('ACTIVE', 'HIDDEN', 'DELETED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_article_id (article_id),
    INDEX idx_author_id (author_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_article_status_created (article_id, status, created_at),
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comment statistics table
CREATE TABLE IF NOT EXISTS comment_statistics (
    comment_id VARCHAR(36) PRIMARY KEY,
    like_count INT DEFAULT 0,
    reply_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comment likes table
CREATE TABLE IF NOT EXISTS comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_comment_id (comment_id),
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_comment_user_like (comment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comment reports table for moderation
CREATE TABLE IF NOT EXISTS comment_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id VARCHAR(36) NOT NULL,
    reporter_id VARCHAR(36) NOT NULL,
    reason ENUM('SPAM', 'INAPPROPRIATE', 'HARASSMENT', 'OTHER') NOT NULL,
    description TEXT,
    status ENUM('PENDING', 'REVIEWED', 'RESOLVED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_comment_id (comment_id),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comment notifications table
CREATE TABLE IF NOT EXISTS comment_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id VARCHAR(36) NOT NULL,
    recipient_id VARCHAR(36) NOT NULL,
    type ENUM('REPLY', 'MENTION', 'LIKE') NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_comment_id (comment_id),
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_recipient_read (recipient_id, is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;