-- ============================================================
--  Personal Finance Management System — Database Setup
-- ============================================================

CREATE DATABASE IF NOT EXISTS pfms_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pfms_db;

-- ------------------------------------------------------------
-- users
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name             VARCHAR(150)   NOT NULL,
    email                 VARCHAR(255)   NOT NULL UNIQUE,
    password              VARCHAR(255)   NOT NULL,
    profile_picture       VARCHAR(500),
    currency              VARCHAR(10)    NOT NULL DEFAULT 'INR',
    is_active             BOOLEAN        NOT NULL DEFAULT TRUE,
    financial_health_score DOUBLE        DEFAULT 0.0,
    created_at            DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                         ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_users_email (email)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- transactions
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255)   NOT NULL,
    description       TEXT,
    amount            DECIMAL(12,2)  NOT NULL,
    type              ENUM('INCOME','EXPENSE') NOT NULL,
    category          ENUM(
                          'FOOD','TRANSPORT','SHOPPING','BILLS','HEALTHCARE',
                          'ENTERTAINMENT','EDUCATION','TRAVEL','SALARY','BUSINESS',
                          'INVESTMENT','SAVINGS','RENT','INSURANCE','SUBSCRIPTIONS',
                          'GIFTS','PERSONAL_CARE','SPORTS','TECHNOLOGY','OTHER'
                      ) NOT NULL,
    transaction_date  DATE           NOT NULL,
    payment_method    VARCHAR(100),
    reference_number  VARCHAR(100),
    is_recurring      BOOLEAN        NOT NULL DEFAULT FALSE,
    tags              VARCHAR(500),
    user_id           BIGINT         NOT NULL,
    created_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                     ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_txn_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_txn_user       (user_id),
    INDEX idx_txn_date       (transaction_date),
    INDEX idx_txn_type       (type),
    INDEX idx_txn_category   (category)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- budgets
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS budgets (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    category         ENUM(
                         'FOOD','TRANSPORT','SHOPPING','BILLS','HEALTHCARE',
                         'ENTERTAINMENT','EDUCATION','TRAVEL','SALARY','BUSINESS',
                         'INVESTMENT','SAVINGS','RENT','INSURANCE','SUBSCRIPTIONS',
                         'GIFTS','PERSONAL_CARE','SPORTS','TECHNOLOGY','OTHER'
                     ) NOT NULL,
    budget_limit     DECIMAL(12,2)  NOT NULL,
    spent_amount     DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    month            INT            NOT NULL,
    year             INT            NOT NULL,
    alert_threshold  INT            NOT NULL DEFAULT 80,
    is_alert_sent    BOOLEAN        NOT NULL DEFAULT FALSE,
    user_id          BIGINT         NOT NULL,
    created_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                    ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_budget (user_id, category, month, year),
    INDEX idx_budget_user_month (user_id, month, year)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- savings_goals
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS savings_goals (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255)   NOT NULL,
    description     TEXT,
    target_amount   DECIMAL(12,2)  NOT NULL,
    current_amount  DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    target_date     DATE,
    status          ENUM('IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'IN_PROGRESS',
    icon            VARCHAR(50),
    color           VARCHAR(20),
    user_id         BIGINT         NOT NULL,
    created_at      DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                   ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_goal_user (user_id)
) ENGINE=InnoDB;

-- ============================================================
--  Sample Data  (password = "password123" BCrypt-hashed)
-- ============================================================
INSERT IGNORE INTO users (full_name, email, password, currency) VALUES
('Demo User', 'demo@pfms.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lN8i',
 'INR');

-- sample transactions for demo user (id=1)
INSERT IGNORE INTO transactions
    (title, description, amount, type, category, transaction_date, payment_method, user_id)
VALUES
('Monthly Salary',  'March salary',        85000.00, 'INCOME',  'SALARY',        '2026-03-01', 'Bank Transfer', 1),
('Freelance Work',  'React project',        15000.00, 'INCOME',  'BUSINESS',      '2026-03-05', 'UPI',           1),
('Rent',            'March rent',           18000.00, 'EXPENSE', 'RENT',          '2026-03-02', 'Bank Transfer', 1),
('Groceries',       'Weekly groceries',      3500.00, 'EXPENSE', 'FOOD',          '2026-03-06', 'UPI',           1),
('Electricity Bill','March electricity',     1200.00, 'EXPENSE', 'BILLS',         '2026-03-07', 'Online',        1),
('Amazon Shopping', 'Electronics',           5800.00, 'EXPENSE', 'SHOPPING',      '2026-03-10', 'Credit Card',   1),
('Netflix',         'Monthly subscription',   649.00, 'EXPENSE', 'SUBSCRIPTIONS', '2026-03-10', 'Credit Card',   1),
('Petrol',          'Bike fuel',              800.00, 'EXPENSE', 'TRANSPORT',     '2026-03-12', 'Cash',          1),
('Dining Out',      'Dinner with family',    2200.00, 'EXPENSE', 'FOOD',          '2026-03-15', 'UPI',           1),
('Gym',             'Monthly membership',    1500.00, 'EXPENSE', 'SPORTS',        '2026-03-16', 'UPI',           1),
('Investment',      'Mutual Fund SIP',      10000.00, 'EXPENSE', 'INVESTMENT',    '2026-03-20', 'Bank Transfer', 1),
('Medicines',       'Pharmacy',              1100.00, 'EXPENSE', 'HEALTHCARE',    '2026-03-22', 'Cash',          1);

-- sample budgets for demo user
INSERT IGNORE INTO budgets (category, budget_limit, spent_amount, month, year, alert_threshold, user_id)
VALUES
('FOOD',          8000.00,  5700.00, 3, 2026, 80, 1),
('SHOPPING',      6000.00,  5800.00, 3, 2026, 80, 1),
('TRANSPORT',     2000.00,   800.00, 3, 2026, 75, 1),
('ENTERTAINMENT', 2000.00,   649.00, 3, 2026, 80, 1),
('RENT',         20000.00, 18000.00, 3, 2026, 90, 1);

-- sample savings goals
INSERT IGNORE INTO savings_goals
    (title, description, target_amount, current_amount, target_date, icon, color, user_id)
VALUES
('Emergency Fund',  '6 months expenses',  150000.00,  45000.00, '2026-12-31', '🛡️', '#4CAF50', 1),
('Vacation Trip',   'Trip to Europe',     200000.00,  30000.00, '2026-10-01', '✈️', '#2196F3', 1),
('New Laptop',      'MacBook Pro',        120000.00,  80000.00, '2026-05-30', '💻', '#FF9800', 1);
