# Market Data Service (MDS) — Implementation Spec

**Directory:** `market-data-service/`
**Port:** 3001
**Stack:** TypeScript 5, Node.js 20, `yahoo-finance2`, `ws`, `node-cron`, `axios`
**Phase:** 1 (built first — defines instrument universe and Price shape for all other services)
**Branch:** `feature/mds/rewrite`

> **Business Logic Vigilance:** Price data is the foundation of all financial calculations in this system. Any change to how bid/ask/last prices are calculated or how bond pricing works must be reviewed against `docs/BUSINESS_LOGIC.md` before coding. Stale or incorrect prices propagate to every trade, every P&L calculation, and every robo advisor recommendation.

---

## Responsibility

MDS is a **pure market data service**. It does one thing: fetch, price, cache, and broadcast financial instrument prices.

| In scope | Out of scope |
|----------|-------------|
| Fetching stock/ETF prices from Yahoo Finance | JWT issuance |
| Fetching Treasury yields from FRED | Trade execution |
| Bond PV pricing from FRED yields | Client authentication |
| In-memory price cache | Portfolio management |
| WebSocket price broadcasting | Any database writes |
| Market hours detection | Business logic |
| Price history (30-day rolling) | |
| REST price snapshot endpoints | |

Spring Boot owns all business logic. MDS only receives the shared `JWT_SECRET` read-only, to authenticate WebSocket connections once per connection.

---

## Instrument Universe

Defined in `src/data/instruments.ts` — typed TypeScript constant, not JSON. This file is the authoritative master list.

```typescript
export const INSTRUMENTS: Instrument[] = [
  // Stocks
  { instrumentId: 'GOOGL',  ticker: 'GOOGL',  categoryId: 'STOCK', instrumentDescription: 'Alphabet Inc. Class A',          minQuantity: 1,  maxQuantity: 1000 },
  { instrumentId: 'TSLA',   ticker: 'TSLA',   categoryId: 'STOCK', instrumentDescription: 'Tesla Inc.',                     minQuantity: 1,  maxQuantity: 1000 },
  { instrumentId: 'JPM',    ticker: 'JPM',    categoryId: 'STOCK', instrumentDescription: 'JPMorgan Chase & Co.',            minQuantity: 1,  maxQuantity: 1000 },
  { instrumentId: 'BRK-B',  ticker: 'BRK-B',  categoryId: 'STOCK', instrumentDescription: 'Berkshire Hathaway Class B',      minQuantity: 1,  maxQuantity: 1000 },
  { instrumentId: 'AAPL',   ticker: 'AAPL',   categoryId: 'STOCK', instrumentDescription: 'Apple Inc.',                     minQuantity: 1,  maxQuantity: 1000 },
  { instrumentId: 'MSFT',   ticker: 'MSFT',   categoryId: 'STOCK', instrumentDescription: 'Microsoft Corp.',                minQuantity: 1,  maxQuantity: 1000 },
  // ETF
  { instrumentId: 'SPY',    ticker: 'SPY',    categoryId: 'ETF',   instrumentDescription: 'SPDR S&P 500 ETF Trust',          minQuantity: 1,  maxQuantity: 1000 },
  // US Treasuries — priced via FRED yield + bond PV formula
  { instrumentId: 'US2Y',   ticker: 'DGS2',   categoryId: 'GOVT',  instrumentDescription: 'US Treasury 2-Year Note',  couponRate: 0.0475, maturityDate: '2027-03-31', minQuantity: 1, maxQuantity: 500 },
  { instrumentId: 'US5Y',   ticker: 'DGS5',   categoryId: 'GOVT',  instrumentDescription: 'US Treasury 5-Year Note',  couponRate: 0.0425, maturityDate: '2030-03-31', minQuantity: 1, maxQuantity: 500 },
  { instrumentId: 'US10Y',  ticker: 'DGS10',  categoryId: 'GOVT',  instrumentDescription: 'US Treasury 10-Year Note', couponRate: 0.0400, maturityDate: '2035-03-31', minQuantity: 1, maxQuantity: 500 },
  { instrumentId: 'US20Y',  ticker: 'DGS20',  categoryId: 'GOVT',  instrumentDescription: 'US Treasury 20-Year Bond', couponRate: 0.0425, maturityDate: '2045-03-31', minQuantity: 1, maxQuantity: 500 },
  { instrumentId: 'US30Y',  ticker: 'DGS30',  categoryId: 'GOVT',  instrumentDescription: 'US Treasury 30-Year Bond', couponRate: 0.0450, maturityDate: '2055-03-31', minQuantity: 1, maxQuantity: 500 },
]
```

Coupon rates and maturity dates are approximate benchmarks for on-the-run Treasuries. They are used only in the bond PV pricing formula — they do not change frequently and do not need to be dynamic.

---

## File Structure

```
market-data-service/
  src/
    data/
      instruments.ts          # Typed instrument master list (authoritative)
    lib/
      bondPricer.ts           # Bond PV formula
      marketHours.ts          # NYSE open/closed detection (ET timezone)
      jwtHelper.ts            # verifyToken() only — no issuance
    cache/
      PriceCache.ts           # In-memory Map<instrumentId, Price> + event emitter
    jobs/
      priceFetcher.ts         # node-cron: Yahoo Finance + FRED fetch + bond pricing
    routes/
      instruments.ts          # GET /instruments
      prices.ts               # GET /prices, GET /prices/:id, GET /prices/:id/history
      health.ts               # GET /health
      marketStatus.ts         # GET /market-status
    ws/
      priceSocket.ts          # WebSocket server + AUTH frame handling
    app.ts                    # Express setup + routes
    server.ts                 # HTTP server bootstrap + WS init + cron start
  spec/
    bondPricer.spec.ts
    marketHours.spec.ts
    priceFetcher.spec.ts
  .env
  .env.example
  tsconfig.json
  package.json
```

---

## Bond Pricing (`src/lib/bondPricer.ts`)

Standard present-value formula. Called with FRED daily yield for each Treasury.

```typescript
/**
 * Standard bond PV formula.
 * @param faceValue  Par value (always 100 for US Treasuries, price per $100 face)
 * @param couponRate Annual coupon rate as decimal (e.g., 0.04 for 4%)
 * @param yieldRate  Current market yield from FRED as decimal (e.g., 0.045 for 4.5%)
 * @param yearsToMaturity Remaining years to maturity
 * @param paymentsPerYear Coupon frequency (2 = semi-annual for US Treasuries)
 */
export function bondPrice(
  faceValue: number,
  couponRate: number,
  yieldRate: number,
  yearsToMaturity: number,
  paymentsPerYear = 2
): number {
  const c = (couponRate * faceValue) / paymentsPerYear   // coupon per period
  const y = yieldRate / paymentsPerYear                   // yield per period
  const n = Math.round(yearsToMaturity * paymentsPerYear) // total periods

  if (n <= 0) return faceValue  // at or past maturity: par

  // PV of coupon stream + PV of par
  const pvCoupons = c * (1 - Math.pow(1 + y, -n)) / y
  const pvPar = faceValue / Math.pow(1 + y, n)
  return Math.round((pvCoupons + pvPar) * 10000) / 10000
}
```

**Bid/ask spread for bonds:** `±0.125` (standard 1/8 point). Bonds move daily, not intraday.

---

## Market Hours (`src/lib/marketHours.ts`)

```typescript
import { DateTime } from 'luxon'

export function isNYSEOpen(): boolean {
  const now = DateTime.now().setZone('America/New_York')
  const day = now.weekday  // 1=Mon ... 7=Sun
  if (day >= 6) return false  // weekend
  const hour = now.hour, minute = now.minute
  const afterOpen  = hour > 9  || (hour === 9  && minute >= 30)
  const beforeClose = hour < 16
  return afterOpen && beforeClose
}

export function secondsUntilNextOpen(): number { ... }
```

---

## Price Fetcher (`src/jobs/priceFetcher.ts`)

```typescript
import yahooFinance from 'yahoo-finance2'
import axios from 'axios'
import { CronJob } from 'node-cron'
import { bondPrice } from '../lib/bondPricer'
import { isNYSEOpen } from '../lib/marketHours'
import { priceCache } from '../cache/PriceCache'

const STOCK_TICKERS = ['GOOGL', 'TSLA', 'JPM', 'BRK-B', 'AAPL', 'MSFT', 'SPY']
const FRED_SERIES   = ['DGS2', 'DGS5', 'DGS10', 'DGS20', 'DGS30']
const FRED_BASE_URL = 'https://fred.stlouisfed.org/graph/fredgraph.csv'

async function fetchStockPrices(): Promise<void> {
  const quotes = await yahooFinance.quote(STOCK_TICKERS)
  for (const q of quotes) {
    const last  = q.regularMarketPrice ?? 0
    const spread = last * 0.0005   // 0.05% realistic large-cap spread
    priceCache.set(q.symbol, {
      instrumentId: q.symbol,
      ticker: q.symbol,
      lastPrice: last,
      bidPrice:  Math.round((last - spread / 2) * 10000) / 10000,
      askPrice:  Math.round((last + spread / 2) * 10000) / 10000,
      priceTimestamp: new Date().toISOString(),
      marketOpen: isNYSEOpen(),
    })
  }
}

async function fetchBondPrices(): Promise<void> {
  for (const series of FRED_SERIES) {
    const resp = await axios.get(`${FRED_BASE_URL}?id=${series}&api_key=${process.env.FRED_API_KEY}`)
    const lines = (resp.data as string).trim().split('\n')
    const latest = lines[lines.length - 1].split(',')
    const yieldPct = parseFloat(latest[1]) / 100  // FRED returns e.g. "4.50"

    const instrumentId = fredSeriestoInstrumentId(series)
    const instrument = INSTRUMENTS.find(i => i.instrumentId === instrumentId)!
    const yearsLeft = yearsToMaturity(instrument.maturityDate!)
    const midPrice = bondPrice(100, instrument.couponRate!, yieldPct, yearsLeft)
    const spread = 0.125

    priceCache.set(instrumentId, {
      instrumentId,
      ticker: series,
      lastPrice: midPrice,
      bidPrice:  Math.round((midPrice - spread / 2) * 10000) / 10000,
      askPrice:  Math.round((midPrice + spread / 2) * 10000) / 10000,
      priceTimestamp: new Date().toISOString(),
      marketOpen: true,  // bonds trade outside NYSE hours
    })
  }
}

// Stock refresh: every 15s during NYSE hours, every 60s off-hours
export function startPriceFetcher(): void {
  const stockInterval = parseInt(process.env.PRICE_REFRESH_SECONDS ?? '15')

  new CronJob(`*/${stockInterval} * * * * *`, fetchStockPrices, null, true)
  new CronJob('*/60 * * * * *', fetchBondPrices, null, true)

  // Initial fetch on startup (don't wait for first cron tick)
  fetchStockPrices().catch(console.error)
  fetchBondPrices().catch(console.error)
}
```

---

## Price Cache (`src/cache/PriceCache.ts`)

```typescript
import { EventEmitter } from 'events'

export interface Price {
  instrumentId: string
  ticker: string
  lastPrice: number
  bidPrice: number
  askPrice: number
  priceTimestamp: string
  marketOpen: boolean
}

class PriceCache extends EventEmitter {
  private cache = new Map<string, Price>()
  private history = new Map<string, Price[]>()  // rolling 30-day, one entry per fetch

  set(instrumentId: string, price: Price): void {
    this.cache.set(instrumentId, price)
    this.appendHistory(instrumentId, price)
    this.emit('update', instrumentId, price)
  }

  getAll(): Price[] { return Array.from(this.cache.values()) }
  get(instrumentId: string): Price | undefined { return this.cache.get(instrumentId) }

  getHistory(instrumentId: string, limit = 30): Price[] {
    return (this.history.get(instrumentId) ?? []).slice(-limit)
  }

  private appendHistory(id: string, price: Price): void {
    const arr = this.history.get(id) ?? []
    arr.push(price)
    // Trim to ~30 days of entries (1 per minute worst case = 43200 per day — use daily snapshots)
    if (arr.length > 30) arr.shift()
    this.history.set(id, arr)
  }
}

export const priceCache = new PriceCache()
```

---

## WebSocket Server (`src/ws/priceSocket.ts`)

Browser connects directly: `ws://localhost:3001/ws/prices`

```typescript
import WebSocket, { WebSocketServer } from 'ws'
import { Server } from 'http'
import { priceCache } from '../cache/PriceCache'
import { verifyToken } from '../lib/jwtHelper'

export function initPriceSocket(httpServer: Server): void {
  const wss = new WebSocketServer({ server: httpServer, path: '/ws/prices' })

  wss.on('connection', (ws) => {
    let authenticated = false

    ws.on('message', (data) => {
      try {
        const frame = JSON.parse(data.toString())

        if (frame.type === 'AUTH') {
          try {
            verifyToken(frame.token)  // throws if invalid/expired
            authenticated = true
            // Send full snapshot immediately after auth
            ws.send(JSON.stringify({
              type: 'PRICE_SNAPSHOT',
              prices: priceCache.getAll(),
              timestamp: new Date().toISOString()
            }))
          } catch {
            ws.send(JSON.stringify({ type: 'AUTH_ERROR', message: 'Invalid token' }))
            ws.close()
          }
          return
        }

        if (frame.type === 'PONG') return  // heartbeat response
      } catch {
        // ignore malformed frames
      }
    })

    // Heartbeat every 30s
    const pingInterval = setInterval(() => {
      if (ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify({ type: 'PING' }))
    }, 30_000)

    ws.on('close', () => clearInterval(pingInterval))
  })

  // Broadcast price updates to all authenticated clients
  priceCache.on('update', () => {
    const frame = JSON.stringify({
      type: 'PRICE_UPDATE',
      prices: priceCache.getAll(),
      timestamp: new Date().toISOString()
    })
    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) client.send(frame)
    })
  })
}
```

Auth is checked once per connection (on the AUTH frame). Subsequent price updates do not require re-auth.

---

## JWT Helper (`src/lib/jwtHelper.ts`)

MDS only **verifies** tokens — it never issues them. Spring Boot issues all tokens.

```typescript
import jwt from 'jsonwebtoken'

const JWT_SECRET = process.env.JWT_SECRET
if (!JWT_SECRET) throw new Error('JWT_SECRET env var required')

export function verifyToken(token: string): { sub: string; email: string; isAdmin: boolean } {
  return jwt.verify(token, JWT_SECRET) as any
}
```

---

## REST Endpoints

### `GET /instruments`
- Auth: None
- Response 200: `Instrument[]` (full list from `instruments.ts`)

### `GET /prices`
- Auth: None (prices are public market data)
- Query: `?category=STOCK|GOVT|ETF` (optional filter)
- Response 200: `Price[]` (current cache snapshot)

### `GET /prices/:instrumentId`
- Auth: None
- Response 200: `Price`
- Response 404: `{ message: "Instrument not found" }`

### `GET /prices/:instrumentId/history`
- Auth: None
- Query: `?period=30d` (default; only 30d supported in Phase 1)
- Response 200: `Price[]` (up to 30 historical entries — used by robo advisor momentum calc)
- Response 404: `{ message: "Instrument not found" }`

### `GET /market-status`
- Auth: None
- Response 200:
  ```json
  {
    "marketOpen": true,
    "timezone": "America/New_York",
    "currentTime": "2025-10-15T14:30:00Z",
    "nextOpenAt": null,
    "nextCloseAt": "2025-10-15T20:00:00Z"
  }
  ```

### `GET /health`
- Auth: None
- Response 200: `{ status: "ok", priceCount: 12, lastUpdate: "<ISO>" }`

---

## WebSocket Auth Flow (Angular side)

```typescript
// In PriceStore.connect():
this.ws = new WebSocket('ws://localhost:3001/ws/prices')

this.ws.onopen = () => {
  // Send auth frame immediately after connection
  const token = authStore.token()
  if (token) this.ws!.send(JSON.stringify({ type: 'AUTH', token }))
}

this.ws.onmessage = (event) => {
  const frame = JSON.parse(event.data)
  if (frame.type === 'PRICE_SNAPSHOT' || frame.type === 'PRICE_UPDATE') {
    this._prices.update(map => {
      const next = new Map(map)
      frame.prices.forEach((p: Price) => next.set(p.instrumentId, p))
      return next
    })
  }
  if (frame.type === 'PING') this.ws!.send(JSON.stringify({ type: 'PONG' }))
  if (frame.type === 'AUTH_ERROR') { /* disconnect, redirect to login */ }
}
```

---

## Environment Variables

```bash
# market-data-service/.env.example
PORT=3001
JWT_SECRET=change_me_must_match_spring_boot
FRED_API_KEY=your_fred_api_key_from_fred_stlouisfed_org
PRICE_REFRESH_SECONDS=15
NODE_ENV=development
```

**JWT_SECRET must match Spring Boot's `jwt.secret` exactly.** MDS uses it only to verify, never to sign.

---

## Dependencies (`package.json`)

```json
{
  "dependencies": {
    "express": "^4.18.0",
    "ws": "^8.16.0",
    "yahoo-finance2": "^2.11.0",
    "axios": "^1.6.0",
    "node-cron": "^3.0.0",
    "jsonwebtoken": "^9.0.0",
    "luxon": "^3.4.0",
    "dotenv": "^16.0.0"
  },
  "devDependencies": {
    "@types/express": "^4.17.0",
    "@types/ws": "^8.5.0",
    "@types/jsonwebtoken": "^9.0.0",
    "@types/luxon": "^3.3.0",
    "@types/node": "^20.0.0",
    "typescript": "^5.3.0",
    "jasmine": "^5.1.0",
    "ts-node": "^10.9.0",
    "ts-node-dev": "^2.0.0"
  }
}
```

---

## Verification Checklist

```bash
# 1. Start MDS
cd market-data-service && npm run dev

# 2. Health check
curl http://localhost:3001/health
# Expect: { status: "ok", priceCount: 12, lastUpdate: "..." }

# 3. Instruments
curl http://localhost:3001/instruments
# Expect: array of 12 instruments

# 4. Price snapshot
curl http://localhost:3001/prices
# Expect: 12 prices with real market values and timestamps within last 15s

# 5. Single price
curl http://localhost:3001/prices/AAPL
# Expect: Price with real bidPrice, askPrice, lastPrice

# 6. Bond price check
curl http://localhost:3001/prices/US10Y
# Expect: Price around 90-110 range depending on current yield; NOT 0

# 7. Market status
curl http://localhost:3001/market-status
# Expect: { marketOpen: true/false, ... }

# 8. Price history
curl http://localhost:3001/prices/AAPL/history
# Expect: array of Price objects (grows over time as fetcher runs)

# 9. WebSocket — open connection, send AUTH, receive PRICE_SNAPSHOT
wscat -c ws://localhost:3001/ws/prices
# After connecting, send: {"type":"AUTH","token":"<valid-jwt>"}
# Expect: {"type":"PRICE_SNAPSHOT","prices":[...]} immediately
# Then: {"type":"PRICE_UPDATE",...} every 15s

# 10. Unit tests
npm test
# All bondPricer, marketHours, priceFetcher tests pass
```
