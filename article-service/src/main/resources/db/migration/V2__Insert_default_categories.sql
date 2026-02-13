-- Insert default categories
INSERT IGNORE INTO categories (name, description, sort_order) VALUES
('Technology', 'Technology related articles', 1),
('Programming', 'Programming tutorials and tips', 2),
('Web Development', 'Web development articles', 3),
('Mobile Development', 'Mobile app development', 4),
('DevOps', 'DevOps and infrastructure', 5),
('Database', 'Database design and optimization', 6),
('Architecture', 'Software architecture and design patterns', 7),
('Tutorial', 'Step-by-step tutorials', 8),
('News', 'Technology news and updates', 9),
('Opinion', 'Personal opinions and thoughts', 10);