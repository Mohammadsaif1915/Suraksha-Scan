-- SurakshaScan Database Schema

CREATE DATABASE IF NOT EXISTS surakshascan;
USE surakshascan;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('normal', 'guardian') NOT NULL DEFAULT 'normal',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Scam Patterns table
CREATE TABLE IF NOT EXISTS scam_patterns (
    pattern_id INT AUTO_INCREMENT PRIMARY KEY,
    category ENUM('sms', 'upi', 'link', 'call') NOT NULL,
    pattern_text VARCHAR(255) NOT NULL,
    pattern_type ENUM('keyword', 'regex') NOT NULL,
    risk_weight INT NOT NULL CHECK(risk_weight BETWEEN 1 AND 10),
    description TEXT
);

-- User Reports table
CREATE TABLE IF NOT EXISTS user_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    input_type ENUM('sms', 'upi', 'link', 'call') NOT NULL,
    raw_input TEXT NOT NULL,
    risk_score INT,
    verdict ENUM('SAFE', 'SUSPICIOUS', 'HIGH RISK'),
    matched_rules TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Community Flags table
CREATE TABLE IF NOT EXISTS community_flags (
    flag_id INT AUTO_INCREMENT PRIMARY KEY,
    flagged_value VARCHAR(255) NOT NULL UNIQUE,
    report_count INT DEFAULT 1,
    first_reported TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_reported TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Family Links table
CREATE TABLE IF NOT EXISTS family_links (
    link_id INT AUTO_INCREMENT PRIMARY KEY,
    guardian_id INT NOT NULL,
    dependent_id INT NOT NULL,
    status ENUM('pending', 'active', 'inactive') DEFAULT 'pending',
    FOREIGN KEY (guardian_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (dependent_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_link (guardian_id, dependent_id)
);

-- Indexes for frequently searched fields
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_patterns_category ON scam_patterns(category);
CREATE INDEX idx_community_flags_value ON community_flags(flagged_value);

-- Insert sample development scam pattern (for testing)
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description) 
VALUES ('sms', '(?i)your account has been blocked.*click here', 'regex', 9, '[DEV ONLY] Mock phishing SMS pattern');


-- Seed data for Step 4
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description) VALUES 
('sms', '(?i)account.*blocked.*kyc', 'regex', 40, 'Urgent KYC threat with account block'),
('sms', '(?i)won.*lottery.*click', 'regex', 35, 'Fake lottery/prize scam'),
('sms', '(?i)electricity.*disconnect', 'regex', 35, 'Fake utility disconnection threat'),
('sms', '(?i)download.*apk', 'regex', 40, 'Malicious APK download request'),
('upi', '(?i)refund.*support', 'regex', 30, 'Suspicious refund support pattern in UPI ID'),
('upi', '(?i)prize.*claim', 'regex', 30, 'Fake prize claim via UPI'),
('link', '(?i)update-kyc.*\\.com', 'regex', 30, 'Suspicious KYC domain pattern');

-- Step 5 Indexes
CREATE INDEX IF NOT EXISTS idx_user_reports_user_id ON user_reports(user_id);
CREATE INDEX IF NOT EXISTS idx_user_reports_user_id_created_at ON user_reports(user_id, created_at DESC);

-- Step 6 Community Reports
CREATE TABLE IF NOT EXISTS community_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    flagged_value VARCHAR(255) NOT NULL,
    input_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE INDEX idx_community_reports_user_flag (user_id, flagged_value, input_type)
);

-- Step 8 Schema: Guardian Alerts
CREATE TABLE IF NOT EXISTS guardian_alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    link_id INT NOT NULL,
    report_id INT NOT NULL,
    guardian_id INT NOT NULL,
    dependent_id INT NOT NULL,
    alert_type VARCHAR(50) NOT NULL DEFAULT 'HIGH_RISK_SCAN',
    title VARCHAR(255) NOT NULL,
    message TEXT,
    risk_score INT DEFAULT 0,
    verdict VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (link_id) REFERENCES family_links(link_id) ON DELETE CASCADE,
    FOREIGN KEY (report_id) REFERENCES user_reports(report_id) ON DELETE CASCADE,
    FOREIGN KEY (guardian_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (dependent_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_alert (guardian_id, report_id, alert_type)
);

-- Step 8 Indexes
CREATE INDEX IF NOT EXISTS idx_family_links_guardian ON family_links(guardian_id);
CREATE INDEX IF NOT EXISTS idx_family_links_dependent ON family_links(dependent_id);
CREATE INDEX IF NOT EXISTS idx_guardian_alerts_guardian ON guardian_alerts(guardian_id);

-- Step 9: Add admin role to users (ALTER ENUM)
ALTER TABLE users MODIFY COLUMN role ENUM('normal', 'guardian', 'admin') NOT NULL DEFAULT 'normal';

-- Step 9: Add is_active to scam_patterns
ALTER TABLE scam_patterns ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
CREATE INDEX IF NOT EXISTS idx_scam_patterns_active ON scam_patterns(is_active);

-- Step 9: Add status to community_flags
ALTER TABLE community_flags ADD COLUMN IF NOT EXISTS status ENUM('active','removed') NOT NULL DEFAULT 'active';
CREATE INDEX IF NOT EXISTS idx_community_flags_status ON community_flags(status);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_reports_verdict ON user_reports(verdict);
CREATE INDEX IF NOT EXISTS idx_reports_created ON user_reports(created_at);

-- Step 9: Admin Audit Log
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INT,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_audit_admin (admin_id),
    INDEX idx_audit_created (created_at)
);
