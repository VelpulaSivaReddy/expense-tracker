CREATE TABLE expenses (
    expense_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(150)   NOT NULL,
    amount          DECIMAL(12,2)  NOT NULL,
    category_id     BIGINT         NOT NULL,
    payment_method  VARCHAR(30)    NOT NULL,
    description     VARCHAR(500),
    notes           VARCHAR(500),
    expense_date    DATE           NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id         BIGINT         NOT NULL,
    CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_expenses_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_expenses_user ON expenses(user_id);
CREATE INDEX idx_expenses_date ON expenses(expense_date);
CREATE INDEX idx_expenses_category ON expenses(category_id);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date);
