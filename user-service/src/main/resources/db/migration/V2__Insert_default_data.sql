-- Insert default admin user
INSERT IGNORE INTO users (id, username, email, password_hash, nickname, status) 
VALUES ('admin-001', 'admin', 'admin@blog.platform', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Administrator', 'ACTIVE');

INSERT IGNORE INTO user_roles (user_id, role_name) 
VALUES ('admin-001', 'ADMIN');

INSERT IGNORE INTO user_statistics (user_id) 
VALUES ('admin-001');