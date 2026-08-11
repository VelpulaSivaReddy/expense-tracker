CREATE TABLE budgets (
    budget_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE,
    daily_budget    DECIMAL(12,2) DEFAULT 0,
    weekly_budget   DECIMAL(12,2) DEFAULT 0,
    monthly_budget  DECIMAL(12,2) DEFAULT 0,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
