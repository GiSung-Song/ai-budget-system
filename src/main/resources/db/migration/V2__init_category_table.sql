CREATE TABLE category
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(50)  NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 기본 카테고리 데이터 삽입
INSERT INTO category (code, display_name) VALUES
('TRANSPORTATION', '교통'),
('FOOD', '음식점'),
('CONVENIENCE_STORE', '편의점'),
('CAFE', '카페')
('LIVING', '생활'),
('CULTURE', '문화'),
('MART', '마트'),
('ETC', '기타');