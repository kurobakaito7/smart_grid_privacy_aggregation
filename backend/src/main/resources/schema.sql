CREATE DATABASE IF NOT EXISTS smartgrid_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smartgrid_db;

CREATE TABLE IF NOT EXISTS device (
    device_id VARCHAR(32) PRIMARY KEY,
    public_key TEXT NOT NULL,
    status VARCHAR(16) DEFAULT 'active',
    last_report_time DATETIME,
    last_seq INT DEFAULT 0,
    register_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS report_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(32) NOT NULL,
    timestamp DATETIME(3) NOT NULL,
    seq INT NOT NULL,
    ciphertext TEXT NOT NULL,
    ciphertext_sq TEXT NOT NULL,
    signature VARCHAR(128) NOT NULL,
    verify_result BOOLEAN NOT NULL,
    reject_reason VARCHAR(256),
    INDEX idx_device_time (device_id, timestamp),
    FOREIGN KEY (device_id) REFERENCES device (device_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS aggregation (
    agg_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    window_start DATETIME(3) NOT NULL,
    window_end DATETIME(3) NOT NULL,
    participant_count INT NOT NULL,
    participant_list JSON,
    sum_power DECIMAL(12, 2) NOT NULL,
    sum_voltage DECIMAL(10, 2) NOT NULL,
    sum_current DECIMAL(10, 2) NOT NULL,
    fog_signature VARCHAR(128) NOT NULL,
    verify_result BOOLEAN NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_window (window_start, window_end)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS audit_log (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_time DATETIME(3) NOT NULL,
    level VARCHAR(16) NOT NULL,
    module VARCHAR(32) NOT NULL,
    device_id VARCHAR(32),
    event_type VARCHAR(64) NOT NULL,
    message VARCHAR(512) NOT NULL,
    detail JSON,
    INDEX idx_event_time (event_time),
    INDEX idx_level (level)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'ADMIN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT IGNORE INTO
    admin_user (username, password, role)
VALUES (
        'admin',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        'ADMIN'
    );