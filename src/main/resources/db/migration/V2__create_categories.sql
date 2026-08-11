CREATE TABLE categories (
    category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name   VARCHAR(100) NOT NULL,
    icon            VARCHAR(50)  DEFAULT 'tag',
    color           VARCHAR(20)  DEFAULT '#22C55E',
    is_default      BOOLEAN      DEFAULT FALSE,
    user_id         BIGINT       NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_category_name_per_user UNIQUE (category_name, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_categories_user ON categories(user_id);

-- Global default categories (user_id = NULL). Every user sees these plus their own custom ones.
INSERT INTO categories (category_name, icon, color, is_default, user_id) VALUES
('Food',            'utensils',    '#22C55E', TRUE, NULL),
('Entertainment',   'film',        '#22C55E', TRUE, NULL),
('Shopping',        'shopping-bag','#22C55E', TRUE, NULL),
('Grocery',         'shopping-cart','#22C55E', TRUE, NULL),
('Fuel',            'fuel',        '#22C55E', TRUE, NULL),
('Medical',         'heart-pulse', '#22C55E', TRUE, NULL),
('Transport',       'bus',         '#22C55E', TRUE, NULL),
('Bills',           'receipt',     '#22C55E', TRUE, NULL),
('Education',       'book',        '#22C55E', TRUE, NULL),
('Rent',            'home',        '#22C55E', TRUE, NULL),
('Investments',     'trending-up', '#22C55E', TRUE, NULL),
('Travel',          'plane',       '#22C55E', TRUE, NULL),
('Mobile Recharge', 'smartphone',  '#22C55E', TRUE, NULL),
('Utilities',       'bolt',        '#22C55E', TRUE, NULL),
('Other',           'tag',         '#22C55E', TRUE, NULL);
