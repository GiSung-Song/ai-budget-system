CREATE TABLE cards
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_company_type VARCHAR(50) NOT NULL,
    card_number       VARCHAR(20) NOT NULL,
    user_id           BIGINT      NOT NULL,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_card_company_number UNIQUE (card_company_type, card_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;