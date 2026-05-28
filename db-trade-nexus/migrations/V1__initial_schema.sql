-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ENUMS
CREATE TYPE trade_direction    AS ENUM ('B', 'S');
CREATE TYPE id_type            AS ENUM ('Aadhar', 'PAN', 'SSN');
CREATE TYPE category_id        AS ENUM ('STOCK', 'GOVT', 'ETF');
CREATE TYPE income_cat         AS ENUM ('LIG', 'MIG', 'HIG', 'VHIG');
CREATE TYPE investment_purpose AS ENUM ('Education', 'Major Expense', 'Retirement');
CREATE TYPE invest_length      AS ENUM ('Short', 'Medium', 'Long');
CREATE TYPE spend_tier         AS ENUM ('Tier1', 'Tier2', 'Tier3', 'Tier4');

-- INSTRUMENT
CREATE TABLE instrument (
    instrument_id    VARCHAR(20)   PRIMARY KEY,
    ticker           VARCHAR(20)   NOT NULL,
    external_id_type VARCHAR(10)   NOT NULL,
    external_id      VARCHAR(50)   NOT NULL,
    category_id      category_id   NOT NULL,
    description      VARCHAR(200)  NOT NULL,
    max_quantity     INT           NOT NULL DEFAULT 1000,
    min_quantity     INT           NOT NULL DEFAULT 1,
    coupon_rate      NUMERIC(6,4)  NULL,
    maturity_date    DATE          NULL
);

-- CLIENT
CREATE TABLE client (
    client_id     VARCHAR(50)   PRIMARY KEY,
    email         VARCHAR(255)  UNIQUE NOT NULL,
    password      VARCHAR(60)   NOT NULL,
    name          VARCHAR(100)  NOT NULL,
    date_of_birth DATE          NOT NULL,
    country       VARCHAR(50)   NOT NULL CHECK (country IN ('India', 'USA')),
    is_admin      BOOLEAN       NOT NULL DEFAULT FALSE,
    curr_balance  NUMERIC(19,4) NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- CLIENT_IDENTIFICATION
CREATE TABLE client_identification (
    client_id VARCHAR(50) NOT NULL REFERENCES client(client_id) ON DELETE CASCADE,
    type      id_type     NOT NULL,
    value     VARCHAR(50) NOT NULL,
    PRIMARY KEY (client_id, type)
);

-- CLIENT_PREFERENCES
CREATE TABLE client_preferences (
    client_id            VARCHAR(50)         PRIMARY KEY REFERENCES client(client_id) ON DELETE CASCADE,
    investment_purpose   investment_purpose  NOT NULL,
    income_category      income_cat          NOT NULL,
    length_of_investment invest_length       NOT NULL,
    percentage_of_spend  spend_tier          NOT NULL,
    risk_tolerance       SMALLINT            NOT NULL CHECK (risk_tolerance BETWEEN 1 AND 5),
    accept_advisor       BOOLEAN             NOT NULL DEFAULT FALSE
);

-- HOLDINGS
CREATE TABLE holdings (
    client_id     VARCHAR(50)   NOT NULL REFERENCES client(client_id) ON DELETE CASCADE,
    instrument_id VARCHAR(20)   NOT NULL REFERENCES instrument(instrument_id),
    quantity      INT           NOT NULL CHECK (quantity > 0),
    avg_price     NUMERIC(19,4) NOT NULL CHECK (avg_price >= 0),
    PRIMARY KEY (client_id, instrument_id)
);

-- CLIENT_ORDER
-- target_price is NULL for MARKET orders; backend enforces NOT NULL for LIMIT orders
CREATE TABLE client_order (
    order_id      VARCHAR(36)     PRIMARY KEY,
    instrument_id VARCHAR(20)     NOT NULL REFERENCES instrument(instrument_id),
    quantity      INT             NOT NULL CHECK (quantity > 0),
    target_price  NUMERIC(19,4)   NULL CHECK (target_price IS NULL OR target_price > 0),
    direction     trade_direction NOT NULL,
    order_type    VARCHAR(10)     NOT NULL CHECK (order_type IN ('MARKET', 'LIMIT')),
    client_id     VARCHAR(50)     NOT NULL REFERENCES client(client_id) ON DELETE CASCADE,
    token         TEXT            NOT NULL,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- CLIENT_TRADE
CREATE TABLE client_trade (
    trade_id        VARCHAR(36)   PRIMARY KEY,
    order_id        VARCHAR(36)   NOT NULL REFERENCES client_order(order_id) ON DELETE CASCADE,
    execution_price NUMERIC(19,4) NOT NULL CHECK (execution_price > 0),
    cash_value      NUMERIC(19,4) NOT NULL CHECK (cash_value >= 0),
    executed_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- Indexes for common query patterns
CREATE INDEX idx_client_trade_order    ON client_trade(order_id);
CREATE INDEX idx_client_order_client   ON client_order(client_id);
CREATE INDEX idx_holdings_client       ON holdings(client_id);
CREATE INDEX idx_client_order_created  ON client_order(created_at DESC);
CREATE INDEX idx_client_trade_executed ON client_trade(executed_at DESC);
