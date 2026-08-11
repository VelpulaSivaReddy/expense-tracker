CREATE TABLE password_reset_tokens (
    token_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    token               VARCHAR(255) NOT NULL UNIQUE,
    user_id             BIGINT NOT NULL,
    expires_at          TIMESTAMP NOT NULL,
    used                BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_token ON password_reset_tokens(token);
CREATE INDEX idx_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_expires_at ON password_reset_tokens(expires_at);
