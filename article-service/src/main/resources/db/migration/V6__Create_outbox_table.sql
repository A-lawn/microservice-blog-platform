-- Outbox pattern table for reliable messaging
CREATE TABLE IF NOT EXISTS outbox_messages (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    target_topic VARCHAR(100) NOT NULL,
    message_key VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retry INT NOT NULL DEFAULT 5,
    next_retry_at DATETIME,
    last_error TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    sent_at DATETIME,
    INDEX idx_outbox_status (status),
    INDEX idx_outbox_retry (status, next_retry_at),
    INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
