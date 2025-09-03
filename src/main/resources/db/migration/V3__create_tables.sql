CREATE TABLE reports
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT  NOT NULL,
    report_month         DATE    NOT NULL,
    report_message       TEXT    NOT NULL,
    notification_message TEXT    NOT NULL,
    is_read              BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_reports_user_month UNIQUE (user_id, report_month)
)