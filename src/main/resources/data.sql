INSERT INTO "users" ("full_name", "email", "password", "currency", "is_active", "financial_health_score", "created_at", "updated_at") VALUES ('Demo User', 'demo@pfms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lN8i', 'INR', TRUE, 0.0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO "transactions" ("title", "description", "amount", "type", "category", "transaction_date", "payment_method", "is_recurring", "user_id", "created_at", "updated_at") VALUES
('Monthly Salary', 'March salary', 85000.00, 'INCOME', 'SALARY', '2026-03-01', 'Bank Transfer', FALSE, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Rent', 'March rent', 18000.00, 'EXPENSE', 'RENT', '2026-03-02', 'Bank Transfer', FALSE, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO "budgets" ("category", "budget_limit", "spent_amount", "month", "year", "alert_threshold", "is_alert_sent", "user_id", "created_at", "updated_at") VALUES
('FOOD', 8000.00, 5700.00, 3, 2026, 80, FALSE, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO "savings_goals" ("title", "description", "target_amount", "current_amount", "target_date", "status", "icon", "color", "user_id", "created_at", "updated_at") VALUES
('Emergency Fund', '6 months expenses', 150000.00, 45000.00, '2026-12-31', 'IN_PROGRESS', '🛡️', '#4CAF50', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
