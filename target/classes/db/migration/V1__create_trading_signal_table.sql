-- V1__create_trading_signal_table.sql
-- Flyway runs this automatically on first startup.
-- MySQL compatible — uses ENUM inline, DECIMAL for prices.

CREATE TABLE IF NOT EXISTS trading_signal (
    id            CHAR(36)       NOT NULL,
    symbol        VARCHAR(20)    NOT NULL,
    direction     ENUM('BUY','SELL') NOT NULL,
    entry_price   DECIMAL(20,8)  NOT NULL,
    stop_loss     DECIMAL(20,8)  NOT NULL,
    target_price  DECIMAL(20,8)  NOT NULL,
    entry_time    DATETIME(6)    NOT NULL,
    expiry_time   DATETIME(6)    NOT NULL,
    created_at    DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    status        ENUM('OPEN','TARGET_HIT','STOPLOSS_HIT','EXPIRED') NOT NULL DEFAULT 'OPEN',
    realized_roi  DECIMAL(10,2)  NULL,

    PRIMARY KEY (id),

    CONSTRAINT chk_expiry_after_entry CHECK (expiry_time > entry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
