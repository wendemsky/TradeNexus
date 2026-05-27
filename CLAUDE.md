# TradeNexus — Claude Code Manual

Trading platform being overhauled from a mock project to a real-data application. Four-service architecture: Market Data Service → PostgreSQL + Spring Boot → Angular 18.

Read this file fully before touching any file. Then read the relevant service spec in `docs/services/`.

---

## CRITICAL: Business Logic Vigilance

> This is a financial application. Every feature touching money, prices, orders, or portfolio state must be verified for **financial correctness**, not just technical correctness.
>
> Before implementing any domain logic: read `docs/BUSINESS_LOGIC.md`.
> If you spot a financially incorrect rule anywhere in the code or docs, flag it immediately rather than implementing around it.
>
> Common traps from the original codebase (already fixed in the new design — do not reintroduce):
> - Fees applied on one side only → fees are 0.1% on both BUY and SELL
> - 5% price tolerance replacing proper order types → MARKET and LIMIT orders
> - Robo advisor scored by bid-ask spread → 3-factor model (momentum + risk fit + category)
> - SELL recommendations from random picks → loss threshold + concentration risk
> - P&L missing unrealized component → realized + unrealized both required

---

## Documentation

Read before starting any phase:

- `docs/ARCHITECTURE.md` — system overview, ports, data flows, critical bug fixes
- `docs/BUSINESS_LOGIC.md` — **financial rules** (fees, P&L, order types, robo advisor scoring)
- `docs/API_CONTRACTS.md` — all request/response shapes across every service boundary
- `docs/IMPLEMENTATION_STATUS.md` — current phase status; update as work completes
- `docs/services/MDS.md` — Phase 1 spec (Market Data Service — pure price data)
- `docs/services/DATABASE.md` — Phase 2 spec
- `docs/services/BACKEND.md` — Phase 3 spec (Spring Boot — owns all auth + business logic)
- `docs/services/FRONTEND.md` — Phase 4 spec

**Deprecated docs (do not implement from these):**
- `docs/services/FIPS.md` — replaced by `MDS.md`
- `docs/services/MIDTIER.md` — mid-tier removed from architecture

---

## Architecture

```
TradeNexus/
  market-data-service/   # TypeScript 5 — live prices (Yahoo Finance + FRED)
  backend-trade-nexus/   # Java 21 + Spring Boot 3.2 — all auth + business logic
  mockdb-trade-nexus/    # PostgreSQL 16 — Flyway migrations (was SQLite)
  frontend-trade-nexus/  # Angular 18 — UI (standalone components + Signals)

  fips-backend/          # DEPRECATED — do not modify
  midtier-trade-nexus/   # REMOVED — do not modify
```

### Request flow (new design)

```
Browser → Angular (4200)
        →[REST] Spring Boot (8080)  ←→  PostgreSQL (5432)
        →[WS]   MDS (3001)          ←→  Yahoo Finance / FRED API

No mid-tier. Angular calls Spring Boot and MDS directly.
```

---

## Tech Stack (New)

| Layer | Language | Key Libraries |
|-------|----------|---------------|
| Frontend | TypeScript 5 | Angular 18, Angular Material 3, AG Grid 32, Chart.js 4, jwt-decode 4, xlsx |
| Backend | Java 21 | Spring Boot 3.2, Spring Data JPA, Spring Security, JJWT 0.12, Bucket4j, Resilience4j |
| MDS | TypeScript 5 | Node.js 20, yahoo-finance2, ws, node-cron, axios, luxon |
| Database | SQL | PostgreSQL 16, Flyway migrations |
| Infra | Docker | Docker Compose (local), Neon/Railway (cloud) |

---

## Commands

### Market Data Service (`market-data-service/`)
```bash
npm run dev           # ts-node-dev — MDS at localhost:3001
npm test              # Jasmine unit tests
npm run build         # tsc compile
```

### Backend (`backend-trade-nexus/`)
```bash
./mvnw spring-boot:run   # Spring Boot at localhost:8080
./mvnw test              # JUnit 5 tests
./mvnw package           # build fat jar
```

### Frontend (`frontend-trade-nexus/`)
```bash
ng serve              # dev server at localhost:4200
ng build              # ng build --configuration=production
npx tsc --noEmit      # type check without build
ng test               # Karma/Jasmine unit tests
```

### Local infrastructure
```bash
docker-compose up     # PostgreSQL (5432) + pgAdmin (5050)
```

Always run lint/type-check/test before declaring work done.

---

## Multi-Agent Git Workflow

Parallel agent model — each agent owns one branch and one scoped task. No agent touches another agent's work in progress.

### Branch naming
```
feature/<layer>/<scope>     # new functionality
fix/<layer>/<scope>         # bug fixes
refactor/<layer>/<scope>    # internal restructuring
test/<layer>/<scope>        # tests only
```

Layer names: `mds`, `db`, `backend`, `frontend`

Examples: `feature/mds/bond-pricer`, `fix/backend/portfolio-avg-cost`, `feature/frontend/trading-form`

### Phase branches (one per phase)
```
feature/mds/rewrite
feature/db/postgres-migration
feature/backend/spring-boot-3
feature/frontend/angular18
```

### Rules

- **One agent = one branch = one scoped task.** No exceptions.
- Never work directly on `master`.
- Never touch files outside your assigned layer unless the task explicitly crosses layers (flag it first).
- Before starting: run `git status` and `git branch` to confirm you are on the right branch.
- After completing work:
  - List every file changed and why
  - Flag potential merge conflicts with shared config or model files
  - Suggest a commit message

### Parallel local development (worktrees)
```bash
git worktree add ../tradenexus-<scope> feature/<layer>/<scope>
```

### Merge conflicts
Do not resolve conflicts in shared files unilaterally. Flag them and stop.

---

## Scope Rules (Critical)

- Work **only** in the subdirectory for your assigned layer.
- **Never** modify without explicit instruction:
  - `mockdb-trade-nexus/migrations/` — schema changes need coordinated migration
  - Any `*.db` file — never commit database files
  - Root `README.md` — doc changes are a separate task
- **Never** refactor outside your scope, even if it looks improvable.
- **Never** create new files or services without confirming first.
- If you notice a bug outside your scope (especially financial logic), document it in your summary — do not fix it silently.

---

## Code Conventions

### All layers
- No comments unless the *why* is non-obvious
- No emojis in code or copy unless explicitly requested
- Prefer editing existing files over creating new ones
- One responsibility per class/module/component

### TypeScript (MDS + frontend)
- Strict mode — no `any`, no suppressed type errors
- Models/interfaces live in `models/` (or `data/`) — not inlined in services
- Services handle HTTP/WS; components handle display logic only

### Java (backend)
- Controller → Service → Repository layering — never skip layers
- DTOs for request/response shapes; JPA entities are internal
- All monetary types: `BigDecimal(19,4)` — never `double` or `float`

### Angular (frontend)
- `ng generate` for new components, services, guards
- Reactive forms over template-driven
- HTTP calls live in `core/services/` only — never in components
- Angular Signals for state (PriceStore, AuthStore, PortfolioStore) — no BehaviorSubject

---

## Commit Rules

- Conventional commits: `type(scope): description`
- Imperative mood, under 72 chars, no trailing period
- Scope matches the layer: `feat(frontend)`, `fix(backend)`, `refactor(mds)`, `chore(db)`
- Types: `feat`, `fix`, `style`, `refactor`, `docs`, `test`, `chore`
- Breaking change: `feat!: description`
- One logical change per commit

---

## Service Ports

| Service | Port | Protocol |
|---------|------|----------|
| Frontend (Angular) | 4200 | HTTP |
| Backend (Spring Boot) | 8080 | HTTP/REST |
| Market Data Service | 3001 | HTTP/REST + WebSocket |
| PostgreSQL | 5432 | TCP |
| pgAdmin | 5050 | HTTP |

---

## Key Domain Concepts

- **Instrument**: a tradeable financial asset. 12 instruments: 6 stocks, 1 ETF, 5 US Treasuries.
- **Order**: a buy/sell request. Types: `MARKET` (execute at current price) or `LIMIT` (IOC — execute at target price or reject).
- **Trade**: an executed order.
- **Holding**: a client's current position; tracks quantity and weighted average cost basis.
- **Robo Advisor**: ranks instruments by 3 factors — momentum (30%), risk fit (40%), category preference (30%).
- **JWT**: issued by Spring Boot (HS256, 30 min). Stored in Angular sessionStorage. MDS verifies for WS auth (read-only `JWT_SECRET`).
- **Activity Report**: Holdings, Trade History, and P&L tabs with Excel export via SheetJS.
- **P&L**: both realized (closed positions) and unrealized (open positions at current bid price).
