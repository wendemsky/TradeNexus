# Database — Implementation Spec

**Directory:** `mockdb-trade-nexus/`
**Engine:** PostgreSQL 16
**Phase:** 2 (after FIPS — FIPS defines instruments; DB schema must reference them)
**Branch:** `feature/db/postgres-migration`

---

## Responsibility

- PostgreSQL schema with proper types, constraints, and FKs
- Flyway migrations (run automatically by Spring Boot on startup)
- Docker Compose for local development (Postgres + pgAdmin)
- Seed data for development/testing
- Configuration templates for Neon/Railway cloud deployment

---

## File Structure

```
mockdb-trade-nexus/
  migrations/
    V1__initial_schema.sql        # Full schema — all tables, types, constraints
    V2__seed_instruments.sql      # 12 instruments from FIPS master list
    V3__seed_dev_clients.sql      # 6 dev clients (bcrypt-hashed passwords)
  docker-compose.yml              # Postgres 16 + pgAdmin
  .env.example                    # Template for DATABASE_URL etc.
  README.md                       # How to run locally and connect to Neon
```

---

## Full PostgreSQL Schema (`V1__initial_schema.sql`)

```sql
-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ENUMS
CREATE TYPE trade_direction AS ENUM ('B', 'S');
CREATE TYPE id_type        AS ENUM ('Aadhar', 'PAN', 'SSN');
CREATE TYPE category_id    AS ENUM ('STOCK', 'GOVT', 'ETF');
CREATE TYPE income_cat     AS ENUM ('LIG', 'MIG', 'HIG', 'VHIG');
CREATE TYPE investment_purpose AS ENUM ('Education', 'Major Expense', 'Retirement');
CREATE TYPE invest_length  AS ENUM ('Short', 'Medium', 'Long');
CREATE TYPE spend_tier     AS ENUM ('Tier1', 'Tier2', 'Tier3', 'Tier4');

-- INSTRUMENT (master list — seeded from FIPS instruments.ts)
CREATE TABLE instrument (
    instrument_id      VARCHAR(20)     PRIMARY KEY,
    ticker             VARCHAR(20)     NOT NULL,
    external_id_type   VARCHAR(10)     NOT NULL,                -- 'TICKER' | 'FRED' | 'CUSIP' | 'ISIN'
    external_id        VARCHAR(50)     NOT NULL,
    category_id        category_id     NOT NULL,
    description        VARCHAR(200)    NOT NULL,
    max_quantity       INT             NOT NULL DEFAULT 1000,
    min_quantity       INT             NOT NULL DEFAULT 1,
    coupon_rate        NUMERIC(6,4)    NULL,                    -- GOVT only: e.g., 0.0425
    maturity_date      DATE            NULL                     -- GOVT only
);

-- CLIENT
CREATE TABLE client (
    client_id      VARCHAR(50)     PRIMARY KEY,                 -- FIPS-generated numeric string
    email          VARCHAR(255)    UNIQUE NOT NULL,
    password       VARCHAR(60)     NOT NULL,                    -- BCrypt hash (always 60 chars)
    name           VARCHAR(100)    NOT NULL,
    date_of_birth  DATE            NOT NULL,
    country        VARCHAR(50)     NOT NULL CHECK (country IN ('India', 'USA')),
    is_admin       BOOLEAN         NOT NULL DEFAULT FALSE,
    curr_balance   NUMERIC(19,4)   NOT NULL,
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- CLIENT_IDENTIFICATION
CREATE TABLE client_identification (
    client_id  VARCHAR(50)  NOT NULL REFERENCES client(client_id) ON DELETE CASCADE,
    type       id_type      NOT NULL,
    value      VARCHAR(50)  NOT NULL,
    PRIMARY KEY (client_id, type)
);

-- CLIENT_PREFERENCES
CREATE TABLE client_preferences (
    client_id          VARCHAR(50)         PRIMARY KEY REFERENCES client(client_id) ON DELETE CASCADE,
    investment_purpose investment_purpose  NOT NULL,
    income_category    income_cat          NOT NULL,
    length_of_investment invest_length     NOT NULL,
    percentage_of_spend spend_tier         NOT NULL,
    risk_tolerance     SMALLINT            NOT NULL CHECK (risk_tolerance BETWEEN 1 AND 5),
    accept_advisor     BOOLEAN             NOT NULL DEFAULT FALSE   -- was 'true'/'false' string
);

-- HOLDINGS
CREATE TABLE holdings (
    client_id      VARCHAR(50)   NOT NULL REFERENCES client(client_id) ON DELETE CASCADE,
    instrument_id  VARCHAR(20)   NOT NULL REFERENCES instrument(instrument_id),
    quantity       INT           NOT NULL CHECK (quantity > 0),
    avg_price      NUMERIC(19,4) NOT NULL CHECK (avg_price >= 0),
    PRIMARY KEY (client_id, instrument_id)
);

-- CLIENT_ORDER
CREATE TABLE client_order (
    order_id       VARCHAR(36)     PRIMARY KEY,                 -- UUID v4 from frontend
    instrument_id  VARCHAR(20)     NOT NULL REFERENCES instrument(instrument_id),
    quantity       INT             NOT NULL CHECK (quantity > 0),
    target_price   NUMERIC(19,4)   NOT NULL CHECK (target_price > 0),
    direction      trade_direction NOT NULL,
    client_id      VARCHAR(50)     NOT NULL REFERENCES client(client_id) ON DELETE CASCADE,
    token          TEXT            NOT NULL,                    -- JWT string (was INTEGER)
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- CLIENT_TRADE
CREATE TABLE client_trade (
    trade_id        VARCHAR(36)     PRIMARY KEY,                -- UUID v4 from FIPS
    order_id        VARCHAR(36)     NOT NULL REFERENCES client_order(order_id) ON DELETE CASCADE,
    execution_price NUMERIC(19,4)   NOT NULL CHECK (execution_price > 0),
    cash_value      NUMERIC(19,4)   NOT NULL CHECK (cash_value >= 0),
    executed_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Indexes for common query patterns
CREATE INDEX idx_client_trade_order    ON client_trade(order_id);
CREATE INDEX idx_client_order_client   ON client_order(client_id);
CREATE INDEX idx_holdings_client       ON holdings(client_id);
CREATE INDEX idx_client_order_created  ON client_order(created_at DESC);
CREATE INDEX idx_client_trade_executed ON client_trade(executed_at DESC);
```

---

## Seed Instruments (`V2__seed_instruments.sql`)

```sql
INSERT INTO instrument (instrument_id, ticker, external_id_type, external_id, category_id, description, max_quantity, min_quantity, coupon_rate, maturity_date) VALUES
('GOOGL',  'GOOGL',  'TICKER', 'GOOGL',  'STOCK', 'Alphabet Inc. Class A',        1000, 1,    NULL,   NULL),
('TSLA',   'TSLA',   'TICKER', 'TSLA',   'STOCK', 'Tesla Inc.',                   1000, 1,    NULL,   NULL),
('JPM',    'JPM',    'TICKER', 'JPM',    'STOCK', 'JPMorgan Chase & Co.',          1000, 1,    NULL,   NULL),
('BRK-B',  'BRK-B',  'TICKER', 'BRK-B',  'STOCK', 'Berkshire Hathaway Class B',   100,  1,    NULL,   NULL),
('AAPL',   'AAPL',   'TICKER', 'AAPL',   'STOCK', 'Apple Inc.',                   1000, 1,    NULL,   NULL),
('MSFT',   'MSFT',   'TICKER', 'MSFT',   'STOCK', 'Microsoft Corp.',              1000, 1,    NULL,   NULL),
('SPY',    'SPY',    'TICKER', 'SPY',    'ETF',   'SPDR S&P 500 ETF Trust',       1000, 1,    NULL,   NULL),
('US2Y',   'DGS2',   'FRED',   'DGS2',   'GOVT',  'US Treasury 2-Year Note',      10000, 100, 0.0425, '2027-05-15'),
('US5Y',   'DGS5',   'FRED',   'DGS5',   'GOVT',  'US Treasury 5-Year Note',      10000, 100, 0.0400, '2029-05-15'),
('US10Y',  'DGS10',  'FRED',   'DGS10',  'GOVT',  'US Treasury 10-Year Note',     10000, 100, 0.0425, '2034-05-15'),
('US20Y',  'DGS20',  'FRED',   'DGS20',  'GOVT',  'US Treasury 20-Year Bond',     10000, 100, 0.0438, '2044-05-15'),
('US30Y',  'DGS30',  'FRED',   'DGS30',  'GOVT',  'US Treasury 30-Year Bond',     10000, 100, 0.0450, '2054-05-15');
```

---

## Seed Dev Clients (`V3__seed_dev_clients.sql`)

Passwords are BCrypt hashes of `'Marsh2024'` (cost factor 12). Pre-compute with `bcrypt.hash('Marsh2024', 12)` before inserting — do NOT store plain text.

```sql
-- Passwords: BCrypt hash of 'Marsh2024'
-- IMPORTANT: Replace <bcrypt_hash> with actual BCrypt hash before running
-- Generate with: node -e "const b=require('bcrypt'); b.hash('Marsh2024',12).then(console.log)"

INSERT INTO client (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) VALUES
('1654658069', 'sowmya@gmail.com',    '<bcrypt_hash>', 'Sowmya',   '2002-11-12', 'India', TRUE,  10000.0000),
('541107416',  'himanshu@gmail.com',  '<bcrypt_hash>', 'Himanshu', '2002-12-08', 'India', TRUE,  10000.0000),
('1425922638', 'rishiyanth@gmail.com','<bcrypt_hash>', 'Rishiyanth','2002-11-08','India', TRUE,  10000.0000),
('1644724236', 'aditi@gmail.com',     '<bcrypt_hash>', 'Aditi',    '2002-09-28', 'India', TRUE,  10000.0000),
('1236679496', 'mohith@gmail.com',    '<bcrypt_hash>', 'Mohith',   '2002-06-25', 'India', TRUE,  10000.0000),
('739982664',  'john.doe@gmail.com',  '<bcrypt_hash>', 'John Doe', '1998-03-15', 'USA',   FALSE, 10000.0000);

INSERT INTO client_identification (client_id, type, value) VALUES
('1654658069', 'Aadhar', '123456789102'),
('541107416',  'Aadhar', '154365349453'),
('1425922638', 'Aadhar', '784304248242'),
('1644724236', 'Aadhar', '759304541346'),
('1236679496', 'Aadhar', '674902441257'),
('739982664',  'SSN',    '123457353');

INSERT INTO client_preferences (client_id, investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, accept_advisor) VALUES
('1654658069', 'Education',     'HIG',  'Short',  'Tier4', 2, FALSE),
('541107416',  'Major Expense', 'LIG',  'Medium', 'Tier2', 1, TRUE),
('1425922638', 'Education',     'MIG',  'Short',  'Tier1', 5, TRUE),
('1644724236', 'Retirement',    'VHIG', 'Medium', 'Tier4', 3, FALSE),
('1236679496', 'Retirement',    'MIG',  'Long',   'Tier3', 3, TRUE),
('739982664',  'Retirement',    'LIG',  'Short',  'Tier4', 1, TRUE);
```

---

## Docker Compose (`docker-compose.yml` in `mockdb-trade-nexus/`)

```yaml
version: '3.9'
services:
  postgres:
    image: postgres:16
    container_name: tradenexus-postgres
    environment:
      POSTGRES_DB: tradenexus
      POSTGRES_USER: tn_user
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tn_user -d tradenexus"]
      interval: 5s
      timeout: 5s
      retries: 10

  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: tradenexus-pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@tradenexus.dev
      PGADMIN_DEFAULT_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5050:80"
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  pgdata:
```

---

## Root `docker-compose.yml` (all services — at repo root)

```yaml
version: '3.9'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: tradenexus
      POSTGRES_USER: tn_user
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tn_user -d tradenexus"]
      interval: 5s
      timeout: 5s
      retries: 10

  fips:
    build: ./fips-backend
    ports:
      - "3001:3001"
    env_file: ./fips-backend/.env
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3001/fips/instruments"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend-trade-nexus
    ports:
      - "8080:8080"
    env_file: ./backend-trade-nexus/.env
    depends_on:
      postgres:
        condition: service_healthy
      fips:
        condition: service_healthy
    environment:
      DATABASE_URL: postgresql://tn_user:${POSTGRES_PASSWORD}@postgres:5432/tradenexus

  midtier:
    build: ./midtier-trade-nexus
    ports:
      - "4000:4000"
    env_file: ./midtier-trade-nexus/.env
    depends_on:
      backend:
        condition: service_started
      fips:
        condition: service_healthy

volumes:
  pgdata:
```

---

## Cloud Deployment (Neon / Railway)

### Neon (PostgreSQL)
1. Create project at neon.tech
2. Copy connection string format: `postgresql://user:password@ep-xxx.region.neon.tech/tradenexus?sslmode=require`
3. Set as `DATABASE_URL` in backend `.env`
4. Flyway runs on Spring Boot startup — migrations applied automatically

### Railway (PostgreSQL)
1. Add PostgreSQL plugin to Railway project
2. Copy `DATABASE_URL` from Railway environment variables panel
3. Same `?sslmode=require` suffix

### Environment File Template

```bash
# mockdb-trade-nexus/.env.example
POSTGRES_PASSWORD=changeme_local_only

# For backend
DATABASE_URL=postgresql://tn_user:changeme_local_only@localhost:5432/tradenexus
```

---

## Key Schema Design Decisions

1. **`client_id` stays VARCHAR(50)** — FIPS generates numeric string IDs using the email hash formula. Keeping string type maintains compatibility with the JWT `sub` claim.

2. **`token TEXT` in `client_order`** — The JWT is a ~200-character string. Old schema had `INTEGER`. This is a breaking change from the old design.

3. **`accept_advisor BOOLEAN`** — Old schema was `TEXT CHECK IN ('true','false')`. Java code had a `==` comparison bug that never worked. New schema uses proper boolean.

4. **`coupon_rate` and `maturity_date` on `instrument`** — Required for bond price calculation in FIPS. NULL for STOCK/ETF.

5. **Flyway, not Hibernate DDL** — `spring.jpa.hibernate.ddl-auto=validate`. Hibernate validates against existing schema but never creates/alters tables. Flyway owns schema lifecycle.

6. **No PRICE_SNAPSHOT table (Phase 2)** — Prices are served live from FIPS. A historical price table can be added in a future phase for charting. Adds storage complexity not needed now.

7. **`holdings.quantity` must be > 0** — Zero-quantity holdings are deleted by the portfolio service (not left as 0-row).

---

## Verification Checklist

```bash
# 1. Start local PostgreSQL
cd mockdb-trade-nexus
docker-compose up -d

# 2. Confirm migrations ran (via Spring Boot startup log or manually)
psql postgresql://tn_user:changeme@localhost:5432/tradenexus \
  -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank"
# Expect: 3 rows (V1, V2, V3)

# 3. Verify instrument seed
psql ... -c "SELECT instrument_id, category_id FROM instrument ORDER BY category_id"
# Expect: 12 rows (7 STOCK/ETF + 5 GOVT)

# 4. Verify no plain text passwords
psql ... -c "SELECT email, LEFT(password, 7) FROM client"
# Expect: all passwords start with '$2b$12$' (BCrypt prefix)

# 5. Test against Neon
DATABASE_URL="postgresql://user:pass@ep-xxx.neon.tech/tradenexus?sslmode=require"
psql "$DATABASE_URL" -c "SELECT COUNT(*) FROM instrument"
# Expect: 12
```
