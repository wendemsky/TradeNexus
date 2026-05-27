# FIPS Service — DEPRECATED

> **This service has been superseded.** See `docs/services/MDS.md` for the replacement implementation.

---

## What FIPS Was

FIPS (Financial Information Processing Service) was the original market data and authentication service bundled into a single Node.js app. It served:
- Static instrument/price data from JSON files
- Deterministic token generation (not real JWTs)
- Trade execution validation

## Why It Was Replaced

| Problem | Resolution |
|---------|-----------|
| Bundled auth + market data — violated single-responsibility | Split: Spring Boot owns all auth; MDS owns all market data |
| Static JSON prices from 2019 CSVs — never live | Yahoo Finance + FRED API live data |
| Deterministic token hash — trivially forgeable | JWT signed with HS256 secret (Spring Boot issues) |
| Trade execution in a data service — wrong layer | Trade execution moved fully to Spring Boot |
| All bond maturities expired (2021–2024) | New on-the-run benchmarks priced via FRED yields |
| CD instrument (C100) — no public market price | Replaced with SPY ETF |
| URL hardcoded as `localhost:3000` | `MDS_URL` env variable |

## Migration

| Old FIPS directory | New location |
|--------------------|-------------|
| `fips-backend/` | Replaced by `market-data-service/` |
| `fips-backend/data/instruments.json` | `market-data-service/src/data/instruments.ts` |
| `fips-backend/routes/trade.js` | Removed — trade logic is in Spring Boot |
| `fips-backend/routes/auth.js` | Removed — auth is in Spring Boot |
| JWT issuance (`/fips/auth/token`) | `POST /auth/login` on Spring Boot (port 8080) |

The old `fips-backend/` directory should be deleted when the MDS rewrite is complete and merged.
