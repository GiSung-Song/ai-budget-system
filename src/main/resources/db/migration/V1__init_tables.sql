-- ===============================================
-- V1_init_tables.sql
-- 초기 테이블 생성 및 초기 데이터 삽입
-- ===============================================

-- 회원 테이블
CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(10)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 카테고리 테이블
CREATE TABLE categories
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(50)  NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_categories_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 기본 카테고리 데이터 삽입
INSERT INTO categories (code, display_name)
VALUES ('TRANSPORTATION', '교통'),
       ('FOOD', '음식점'),
       ('CONVENIENCE_STORE', '편의점'),
       ('CAFE', '카페'),
       ('LIVING', '생활'),
       ('CULTURE', '문화'),
       ('MART', '마트'),
       ('ETC', '기타');

-- 카드 테이블
CREATE TABLE cards
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_company_type VARCHAR(50) NOT NULL,
    card_number       VARCHAR(20) NOT NULL,
    user_id           BIGINT      NOT NULL,
    synchronized_at   DATETIME    NULL,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cards_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_card_company_number UNIQUE (card_company_type, card_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 카드 거래내역 테이블
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
    card_transaction_type   VARCHAR(20)    NOT NULL,
    card_transaction_status VARCHAR(20)    NOT NULL,
    CONSTRAINT uq_card_merchant UNIQUE (merchant_id, card_number),
    INDEX idx_card_txn_time (card_number, transaction_at),
    INDEX idx_original_merchant (original_merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 매장-카테고리 매핑 테이블
CREATE TABLE merchant_categories
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id   BIGINT       NOT NULL,
    merchant_name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_merchant_categories_categories FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT uk_merchant_name UNIQUE (merchant_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 매장-카테고리 매핑 데이터 삽입
INSERT INTO merchant_categories (merchant_name, category_id)
SELECT '스타벅스', id FROM categories WHERE code = 'CAFE'
UNION ALL
SELECT '맥도날드', id FROM categories WHERE code = 'FOOD'
UNION ALL
SELECT '버스', id FROM categories WHERE code = 'TRANSPORTATION'
UNION ALL
SELECT 'GS25', id FROM categories WHERE code = 'CONVENIENCE_STORE'
UNION ALL
SELECT '메가박스', id FROM categories WHERE code = 'CULTURE'
UNION ALL
SELECT '홈플러스', id FROM categories WHERE code = 'MART'
;

-- 거래내역 테이블
CREATE TABLE transactions
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT         NOT NULL,
    card_id              BIGINT         NOT NULL,
    category_id          BIGINT         NOT NULL,
    merchant_id          VARCHAR(255)   NOT NULL,
    original_merchant_id VARCHAR(255),
    amount               DECIMAL(15, 2) NOT NULL,
    merchant_name        VARCHAR(255)   NOT NULL,
    merchant_address     VARCHAR(255),
    transaction_at       DATETIME       NOT NULL,
    transaction_type     VARCHAR(20)    NOT NULL,
    transaction_status   VARCHAR(20)    NOT NULL,
    created_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_users      FOREIGN KEY (user_id)     REFERENCES users(id),
    CONSTRAINT fk_transactions_cards      FOREIGN KEY (card_id)     REFERENCES cards(id),
    CONSTRAINT fk_transactions_categories FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT uq_txn UNIQUE (card_id, merchant_id, transaction_at),
    INDEX idx_txn_time          (card_id, merchant_id, transaction_at),
    INDEX idx_original_merchant (original_merchant_id)
)