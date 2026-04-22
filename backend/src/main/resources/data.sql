-- 直接插入admin用户（使用BCrypt加密的密码 "admin123"）
-- BCrypt.encode("admin123") 的结果
INSERT INTO admin_user (username, password, role, created_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', NOW())
ON DUPLICATE KEY UPDATE password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH';
