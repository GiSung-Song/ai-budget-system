CREATE TABLE card_transaction
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id             VARCHAR(255)   NOT NULL,
    original_merchant_id    VARCHAR(255),
    card_number             VARCHAR(255)   NOT NULL,
    amount                  DECIMAL(15, 2) NOT NULL,
    merchant_name           VARCHAR(255)   NOT NULL,
    merchant_address        VARCHAR(255),
    transaction_at          DATETIME       NOT NULL,
    card_transaction_type   ENUM('PAYMENT','WITHDRAW','DEPOSIT','TRANSFER') NOT NULL,
    card_transaction_status ENUM('APPROVED','REFUND','CANCELED')            NOT NULL,
    CONSTRAINT uq_card_merchant UNIQUE (merchant_id, card_number),
    INDEX idx_card_txn_time (card_number, transaction_at),
    INDEX idx_original_merchant (original_merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;