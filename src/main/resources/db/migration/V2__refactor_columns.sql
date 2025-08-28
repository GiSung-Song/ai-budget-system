ALTER TABLE cards
DROP COLUMN synchronized_at;

ALTER TABLE transactions
DROP COLUMN transaction_type;

ALTER TABLE card_transaction
DROP COLUMN card_transaction_type;