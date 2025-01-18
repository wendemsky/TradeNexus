DROP TABLE IF EXISTS HOLDINGS;
DROP TABLE IF EXISTS CLIENT_TRADE;
DROP TABLE IF EXISTS CLIENT_ORDER;
DROP TABLE IF EXISTS CLIENT_PREFERENCES;
DROP TABLE IF EXISTS CLIENT_IDENTIFICATION;
DROP TABLE IF EXISTS CLIENT;

-- Create Table Client
CREATE TABLE CLIENT (
    client_id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    date_of_birth TEXT NOT NULL,
    country TEXT NOT NULL,
    is_admin TEXT CHECK (is_admin IN ('Y', 'N')),
    curr_balance REAL NOT NULL
);

-- Create Table Client Identification
CREATE TABLE CLIENT_IDENTIFICATION (
    client_id TEXT,
    type TEXT,
    value TEXT,
    PRIMARY KEY(client_id, type),
    FOREIGN KEY(client_id) REFERENCES CLIENT(client_id)
);

-- Create table for Client Preferences
CREATE TABLE CLIENT_PREFERENCES (
    client_id TEXT PRIMARY KEY,
    investment_purpose TEXT,
    income_category TEXT,
    length_of_investment TEXT,
    percentage_of_spend TEXT,
    risk_tolerance INTEGER,
    is_advisor_accepted TEXT CHECK (is_advisor_accepted IN ('true', 'false')),
    FOREIGN KEY(client_id) REFERENCES CLIENT(client_id)
);

-- Create HOLDINGS table
CREATE TABLE HOLDINGS (
    client_id TEXT NOT NULL,
    instrument_id TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    avg_price REAL NOT NULL,
    PRIMARY KEY (client_id, instrument_id),
    FOREIGN KEY (client_id) REFERENCES CLIENT(client_id) ON DELETE CASCADE
);

-- Create table ORDER
CREATE TABLE CLIENT_ORDER (
    instrument_id TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    target_price REAL NOT NULL,
    direction TEXT CHECK (direction IN ('S', 'B')),
    client_id TEXT,
    order_id TEXT PRIMARY KEY,
    token INTEGER NOT NULL,
    FOREIGN KEY(client_id) REFERENCES CLIENT(client_id) ON DELETE CASCADE
);

-- Create table TRADE
CREATE TABLE CLIENT_TRADE (
    trade_id TEXT PRIMARY KEY,
    order_id TEXT,
    execution_price REAL NOT NULL,
    cash_value REAL NOT NULL,
    executed_at TEXT NOT NULL,
    FOREIGN KEY(order_id) REFERENCES CLIENT_ORDER(order_id) ON DELETE CASCADE
);
