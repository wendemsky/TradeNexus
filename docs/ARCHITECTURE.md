# TradeNexus — System Architecture

## Purpose

This document is the canonical architecture reference for the TradeNexus overhaul. Every AI instance and developer working on this project should read this before touching any file.

> **Business Logic Vigilance:** This is a financial application. Every feature that touches money, prices, orders, or portfolio state must be verified for financial correctness, not just technical correctness. See `docs/BUSINESS_LOGIC.md` before implementing any domain logic.

---

## Service Map

```
┌─────────────────────────────────────────────────────────────────────┐
│  Browser (Angular 18)  port 4200                                    │
│  • REST calls → Spring Boot (auth, trades, portfolio, reports)      │
│  • WebSocket → Market Data Service (live price stream)              │
└──────────────────┬──────────────────────────┬───────────────────────┘
                   │ REST                      │ WebSocket
                   ▼                           ▼
   ┌───────────────────────────┐   ┌──────────────────────────────────┐
   │  Spring Boot 3.2          │   │  Market Data Service (MDS)       │
   │  Java 21  port 8080       │   │  TypeScript 5  port 3001         │
   │                           │   │                                  │
   │  • JWT issuance + BCrypt  │   │  • Yahoo Finance (stocks/ETFs)   │
   │  • MARKET + LIMIT orders  │   │  • FRED API (Treasury yields)    │
   │  • Portfolio management   │   │  • Bond price calculation        │
   │  • P&L (realized +        │◄──│  • In-memory PriceCache          │
   │    unrealized)            │ ↑ │  • Market status + hours         │
   │  • Activity reports       │ │ │  • Price history (30-day)        │
   │  • Robo advisor scoring   │ │ │  • WebSocket price broadcasting  │
   │  • Client preferences     │ │ └──────────────┬───────────────────┘
   └──────────────┬────────────┘ │                │
                  │ JDBC         │ REST            │ Yahoo Finance
                  ▼             └─(price fetch)   │ FRED API
   ┌──────────────────────┐                       ▼
   │  PostgreSQL  port 5432│              External APIs (free)
   │  (Neon / Railway /   │
   │   Docker locally)    │
   └──────────────────────┘
```

**No mid-tier.** Angular connects directly to Spring Boot (REST) and MDS (WebSocket). The Express proxy layer was removed — it added a network hop and failure point without providing meaningful value at this scale. Spring Boot handles CORS, rate limiting (Bucket4j), and security natively.

---

## Real-Time Price Flow

```
Yahoo Finance (stocks/ETFs)        FRED API (bond yields)
         │                                │
         └──────────────┬─────────────────┘
                        │  PriceFetcher job
                        │  every 15s (market hours)
                        │  every 60s (off-hours / bonds)
                        ▼
              MDS PriceCache
              (in-memory Map<instrumentId, Price>)
                        │
                        │  broadcast on every refresh cycle
                        ▼
              MDS WebSocket Server
              ws://localhost:3001/ws/prices
                        │
                        │  direct browser connection
                        ▼
              Angular PriceStore (signal-based)
                        │
         ┌──────────────┼──────────────┬──────────────┐
         ▼              ▼              ▼              ▼
   Instrument     Portfolio      Robo Advisor    Unrealized
   Grid (live)   (unreal. P&L)  (live scoring)   P&L %
```

---

## Port Assignments

| Service | Port | Protocol |
|---------|------|----------|
| Frontend (Angular dev server) | 4200 | HTTP |
| Backend (Spring Boot) | 8080 | HTTP/REST |
| Market Data Service | 3001 | HTTP/REST + WebSocket |
| PostgreSQL | 5432 | TCP |
| pgAdmin | 5050 | HTTP |

---

## Technology Stack

| Layer | Technology | Key Libraries |
|-------|-----------|---------------|
| Frontend | Angular 18, TypeScript 5 | `@angular/material` 3, `ag-grid-angular` 32, `lightweight-charts` 4, `chart.js` 4, `jwt-decode`, `xlsx` |
| Backend | Java 21, Spring Boot 3.2 | `spring-data-jpa`, `postgresql`, `spring-security`, `jjwt`, `bucket4j`, `resilience4j` |
| Market Data Service | TypeScript 5, Node.js 20 | `yahoo-finance2`, `ws`, `node-cron`, `axios` |
| Database | PostgreSQL 16 | Flyway migrations |
| Infra | Docker | Docker Compose (local), Neon/Railway (cloud) |

---

## Authentication Design

Spring Boot is the sole owner of authentication. MDS has no auth responsibility.

### JWT Token Flow

```
1. POST /auth/login → Spring Boot
2. Spring Boot: look up client by email; BCrypt.verify(plainPassword, storedHash)
3. Spring Boot: issue JWT
   - Signed with JWT_SECRET (HS256)
   - Payload: { sub: clientId, email, isAdmin, iat, exp }
   - Expiry: 30 minutes
4. JWT returned to Angular
5. Angular: store in sessionStorage; AuthInterceptor adds Bearer header to all HTTP requests
6. Spring Boot: SecurityFilter validates JWT on every protected endpoint
7. MDS WebSocket: on first connection message { type: "AUTH", token }, decode + verify JWT
   using shared JWT_SECRET env var. Auth once per connection; not per message.
```

### JWT Secret Ownership

`JWT_SECRET` is owned by Spring Boot. MDS receives a read-only copy to validate WebSocket connections. No other service uses it.

| Service | JWT Role |
|---------|----------|
| Spring Boot | Issues tokens, validates on every REST request |
| MDS | Validates once on WS connection (read-only use of JWT_SECRET) |
| Angular | Stores token, attaches to requests, decodes locally for expiry check |

### Token Refresh

Angular checks `exp` claim every 60s. If within 5 minutes of expiry, calls `POST /auth/refresh` → Spring Boot issues a new JWT.

---

## Service Responsibilities (Clean Boundaries)

| Concern | Owner | Notes |
|---------|-------|-------|
| JWT issuance | Spring Boot | Not MDS, not a separate auth service |
| BCrypt password verification | Spring Boot | Passwords never leave the backend |
| Instrument master list | MDS (source) → seeded to DB | MDS constants file is the authoritative list |
| Live prices | MDS exclusively | No other service fetches market data |
| WebSocket broadcasting | MDS | Browser connects directly; no broker/proxy |
| Trade execution logic | Spring Boot | Fetches price from MDS REST, applies all business rules |
| Portfolio management | Spring Boot | Balance, holdings, avg cost basis |
| Realized + unrealized P&L | Spring Boot | Unrealized fetches live price from MDS |
| Activity reports | Spring Boot | All data from DB + MDS price enrichment |
| Robo advisor scoring | Spring Boot | Momentum + risk fit + category (uses MDS price history) |
| User preferences | Spring Boot | |
| Database schema | DB (Flyway) | Hibernate validates, never creates/alters |

**Data flow direction:** Spring Boot → MDS (for prices). Never MDS → Spring Boot.

---

## Critical Fixes vs Old Design

| # | Original Problem | New Design |
|---|-----------------|------------|
| 1 | Passwords stored in plain text | BCrypt hash in backend before storing |
| 2 | Login via `GET /client?password=x` (credentials in URL/logs) | `POST /auth/login` with JSON body |
| 3 | Token = deterministic hash of email (trivially forgeable) | JWT signed with secret + expiry |
| 4 | FIPS issued JWT while backend validated it — split ownership | Spring Boot owns auth end-to-end |
| 5 | 5% price tolerance mapped to no real financial concept | MARKET + LIMIT order types |
| 6 | 1% fee on BUY only — makes P&L calculations wrong | 0.1% on both BUY and SELL |
| 7 | `priceList` cached at TradeService startup — never refreshes | Fresh MDS price fetch per trade |
| 8 | FIPS URL hardcoded as `localhost:3000` | `MDS_URL` environment variable |
| 9 | `acceptAdvisor == "false"` — Java `==` on String, always false | Boolean field, proper comparison |
| 10 | `subList(0,5)` throws if < 5 results | `subList(0, Math.min(5, list.size()))` |
| 11 | Robo advisor scores by bid-ask spread — not a financial concept | Momentum + volatility + category preference |
| 12 | `CLIENT_ORDER.token INTEGER` — can't store JWT string | `token TEXT NOT NULL` |
| 13 | Express mid-tier as pure passthrough — complexity without value | Removed; Angular talks directly to backend + MDS |

---

## Instrument Universe

| instrumentId | ticker | type | description | MDS source |
|---|---|---|---|---|
| GOOGL | GOOGL | STOCK | Alphabet Inc. Class A | yahoo-finance2 |
| TSLA | TSLA | STOCK | Tesla Inc. | yahoo-finance2 |
| JPM | JPM | STOCK | JPMorgan Chase | yahoo-finance2 |
| BRK-B | BRK-B | STOCK | Berkshire Hathaway Class B | yahoo-finance2 |
| AAPL | AAPL | STOCK | Apple Inc. | yahoo-finance2 |
| MSFT | MSFT | STOCK | Microsoft Corp. | yahoo-finance2 |
| SPY | SPY | ETF | SPDR S&P 500 ETF Trust | yahoo-finance2 |
| US2Y | DGS2 | GOVT | US Treasury 2-Year Note | FRED + bond pricer |
| US5Y | DGS5 | GOVT | US Treasury 5-Year Note | FRED + bond pricer |
| US10Y | DGS10 | GOVT | US Treasury 10-Year Note | FRED + bond pricer |
| US20Y | DGS20 | GOVT | US Treasury 20-Year Bond | FRED + bond pricer |
| US30Y | DGS30 | GOVT | US Treasury 30-Year Bond | FRED + bond pricer |

---

## Environment Variables

| Variable | Service(s) | Description |
|---|---|---|
| `JWT_SECRET` | Spring Boot, MDS (read-only) | JWT signing secret — HS256 |
| `FRED_API_KEY` | MDS | FRED API key (free at fred.stlouisfed.org) |
| `PRICE_REFRESH_SECONDS` | MDS | Stock refresh interval (default: 15) |
| `PORT` | MDS | HTTP port (default: 3001) |
| `DATABASE_URL` | Spring Boot | PostgreSQL connection string |
| `MDS_URL` | Spring Boot | MDS base URL (default: http://localhost:3001) |
| `INITIAL_BALANCE` | Spring Boot | New client starting balance USD (default: 10000) |
| `POSTGRES_PASSWORD` | Docker Compose | Local PostgreSQL password |

---

## Branch Strategy

Four phases. Merge each before starting the next.

```
master
  └── feature/mds/rewrite              Phase 1: Market Data Service
  └── feature/db/postgres-migration    Phase 2: Database (after Phase 1 merged)
  └── feature/backend/spring-boot-3    Phase 3: Backend (after Phase 2 merged)
  └── feature/frontend/angular18       Phase 4: Frontend (after Phase 3 merged)
```

---

## Cloud Deployment

### Local Development
```bash
docker-compose up          # PostgreSQL + pgAdmin
npm run dev                # from market-data-service/
./mvnw spring-boot:run     # from backend-trade-nexus/
ng serve                   # from frontend-trade-nexus/
```

### Cloud Targets
- **PostgreSQL**: Neon free tier — `postgresql://...?sslmode=require`
- **Backend**: Railway or Render (Java/Docker image)
- **MDS**: Railway or Render (Node.js)
- **Frontend**: Vercel or Netlify (`ng build` → static)
