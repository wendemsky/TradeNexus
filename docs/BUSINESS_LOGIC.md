# TradeNexus — Business Logic Reference

## Purpose

This document is the canonical reference for all financial business logic in TradeNexus. Every service that makes a financial decision must conform to what is written here. This is a living document — if you encounter a rule that seems wrong, incomplete, or inconsistent with how real platforms work, **update it before updating the code**.

## Vigilance Rule

> **When implementing any feature that touches money, prices, orders, or portfolio state: stop and ask whether the logic is financially correct, not just technically correct.**
>
> The original codebase made multiple domain errors (5% price tolerance, 1% fee on buys only, bid-ask spread as a robo advisor score, String comparison with `==` for a boolean field). These errors were invisible at the code level but wrong at the domain level. The goal now is a system that a real user could trust.
>
> If you spot a rule in this document that looks wrong — a formula, a threshold, a fee, a condition — flag it in a comment inside this document (`<!-- REVIEW: ... -->`) and raise it before proceeding. Do not silently implement something that feels off.

---

## 1. Order Types

### 1.1 Supported Order Types

**MARKET order** — execute immediately at the best available price.
- User does not specify a target price (or `targetPrice` is null/omitted).
- BUY fills at current `askPrice` from MDS.
- SELL fills at current `bidPrice` from MDS.
- Will reject if market is closed or price data is stale.
- No price tolerance concept — you get market price.

**LIMIT order** — execute only if the market price meets the user's condition.
- User specifies a `targetPrice` (the limit price).
- BUY limit: execute only if `currentAskPrice ≤ targetPrice` (you're willing to pay up to your limit)
- SELL limit: execute only if `currentBidPrice ≥ targetPrice` (you'll only sell at or above your floor)
- If condition not met: reject with `LIMIT_NOT_MET` — do not queue, do not partially fill.
  - Rationale: We are not building an order book or queuing system. Limits execute immediately or not at all (IOC — Immediate Or Cancel semantics).

### 1.2 Order Type Validation Matrix

| orderType | direction | targetPrice | Result |
|-----------|-----------|-------------|--------|
| MARKET | B | null/omitted | Execute at askPrice |
| MARKET | S | null/omitted | Execute at bidPrice |
| LIMIT | B | user-provided | Execute if askPrice ≤ targetPrice, else reject |
| LIMIT | S | user-provided | Execute if bidPrice ≥ targetPrice, else reject |
| MARKET | B | provided | Ignore targetPrice, execute at askPrice |
| MARKET | S | provided | Ignore targetPrice, execute at bidPrice |

### 1.3 What is NOT supported

- Stop orders (trigger at price, then execute as market): not implemented
- Stop-limit orders: not implemented
- Good-Till-Cancelled (GTC): not implemented — all orders are IOC
- After-hours trading: not implemented
- Partial fills: not implemented — order fills completely or not at all
- Short selling: not implemented — cannot sell an instrument you do not hold
- Margin trading: not implemented

These are documented simplifications, not bugs.

---

## 2. Trade Execution Flow

Order of operations in `TradeService.executeTrade()`:

```
1. Validate order fields (non-null, quantity > 0, instrumentId exists in DB)
2. Check market is open (call MDS /market-status, reject if closed)
3. Fetch current price from MDS (GET /prices/:instrumentId)
4. Verify price freshness (reject if priceTimestamp > 60 seconds old)
5. Apply order type logic:
   MARKET → execPrice = askPrice (BUY) or bidPrice (SELL)
   LIMIT  → check condition; reject if not met
6. For BUY: verify cashBalance ≥ cashValue (after fee)
7. For SELL: verify holdings.quantity ≥ order.quantity
8. Save order to DB (CLIENT_ORDER)
9. Save trade to DB (CLIENT_TRADE)
10. Update portfolio (balance + holdings)
11. Return Trade object
```

Steps 2–5 must happen before any DB writes. If any check fails, nothing is persisted.

---

## 3. Fee Structure

### 3.1 Transaction Fee

**Rate:** 0.1% (10 basis points) on both BUY and SELL.

```
// BUY cashValue (what is deducted from balance)
cashValue = quantity × askPrice × 1.001

// SELL cashValue (what is added to balance)
cashValue = quantity × bidPrice × 0.999
```

### 3.2 Rationale

- 1% (the original rate) is a very high retail fee. Even 2010-era brokers charged < 0.5% for equity trades. Modern platforms (Schwab, Fidelity, Robinhood) charge $0 commission.
- 0.1% is a realistic representation of exchange fees + spread costs without requiring a $0-commission platform model.
- Applying fees on both sides ensures round-trip costs are visible and P&L is honest: a user who buys and immediately sells will show a small loss, which is correct.

### 3.3 Fee in Cost Basis

The buy cashValue (including fee) is used when calculating average cost basis. This is correct — the total cost of acquiring a position includes all transaction costs.

```
// On BUY
totalCostOfNewLot = quantity × askPrice × 1.001

// New average price
newAvgPrice = (oldAvgPrice × oldQuantity + totalCostOfNewLot) / (oldQuantity + quantity)
```

---

## 4. Portfolio and Holdings

### 4.1 Weighted Average Cost Basis

TradeNexus uses **weighted average cost basis** to track the cost of each holding.

```
On BUY:
  if holding exists:
    newAvgPrice = ((oldAvgPrice × oldQty) + (askPrice × 1.001 × newQty)) / (oldQty + newQty)
    newQuantity = oldQty + newQty
  else:
    avgPrice = askPrice × 1.001
    quantity = newQty

On SELL:
  newQuantity = oldQty - soldQty
  avgPrice stays the same (weighted average does not change on sells)
  if newQuantity == 0: delete the holding row
```

FIFO (First In, First Out) is an alternative method that some real platforms use and provides more accurate tax lot tracking. TradeNexus uses weighted average for simplicity. This is a known simplification.

### 4.2 Holdings Invariants

- Holdings quantity is always > 0. A holding at quantity = 0 is deleted, not kept.
- Average price is always ≥ 0.
- A client can only hold instruments that exist in the INSTRUMENT table.
- Short positions (negative quantity) are not possible.

### 4.3 Cash Balance Rules

- Balance is deducted immediately on BUY (T+0 settlement — documented simplification; real market is T+1).
- Balance is credited immediately on SELL.
- Balance can never go below 0 (a BUY that would make balance negative is rejected).
- Initial balance: $10,000 USD for all new clients.

---

## 5. P&L Calculations

### 5.1 Realized P&L

Calculated at the time of a SELL trade.

```
realizedPL per sell = (sellCashValue) - (holding.avgPrice × quantitySold)

// where:
sellCashValue = quantity × bidPrice × 0.999    // after sell fee
holding.avgPrice = weighted average cost (includes buy fees)
```

Accumulated realized P&L per instrument across all sells:
```
totalRealizedPL(instrumentId) = sum(realizedPL) for all SELL trades of that instrument
```

### 5.2 Unrealized P&L

Calculated in real-time using live prices from MDS. Not persisted to DB.

```
unrealizedPL = (currentBidPrice - holding.avgPrice) × holding.quantity
unrealizedPLPercent = (currentBidPrice - holding.avgPrice) / holding.avgPrice × 100
```

Use `bidPrice` (not `lastPrice`) because unrealized P&L represents what you could realize *right now* if you sold.

### 5.3 Total P&L

```
totalPL = realizedPL + unrealizedPL
```

Reported per instrument in the activity report.

### 5.4 Portfolio Valuation

Calculated in real-time. Not persisted to DB.

```
holdingMarketValue(instrument) = currentBidPrice × quantity
totalHoldingsValue = sum(holdingMarketValue) for all holdings
totalNetworth = cashBalance + totalHoldingsValue
```

Portfolio composition (percentage allocation):
```
instrumentAllocation% = holdingMarketValue / totalNetworth × 100
cashAllocation% = cashBalance / totalNetworth × 100
```

---

## 6. Robo Advisor Scoring

### 6.1 Overview

The robo advisor recommends up to 5 instruments to BUY or SELL based on the client's preferences. Scores are computed per instrument and the top 5 (or all available if < 5) are returned.

The scoring uses three factors:
1. **Momentum** (30%) — is the price trending in a favorable direction?
2. **Risk Fit** (40%) — does the instrument's volatility match the client's risk tolerance?
3. **Category Preference** (30%) — does the instrument type align with the client's profile?

### 6.2 Required Data from MDS

Robo advisor requires 30-day price history per instrument:
```
GET /mds/prices/:instrumentId/history?period=30d
Response: { instrumentId, prices: [{ date, close }] }
```

This is computed at scoring time, not cached in the DB.

### 6.3 Momentum Score (30% weight)

Direction: positive momentum favors BUY; negative momentum favors SELL.

```
MA5  = average of last 5 closing prices
MA20 = average of last 20 closing prices
momentum = (MA5 - MA20) / MA20   // positive = uptrend, negative = downtrend

For BUY scoring: higher momentum = better score
For SELL scoring: lower momentum (downtrend) = better score (exit the position)
```

### 6.4 Risk Fit Score (40% weight)

Volatility is the standard deviation of daily returns over 30 days.

```
dailyReturn[i] = (price[i] - price[i-1]) / price[i-1]
volatility = stdDev(dailyReturn[])

// Normalize to [0,1] using expected ranges:
// Low volatility: ~0.3% daily (GOVT bonds)
// High volatility: ~3%+ daily (TSLA, high-beta stocks)

normalizedVol = clamp(volatility / 0.03, 0, 1)    // 3% daily = max
normalizedRisk = (riskTolerance - 1) / 4           // scale 1-5 to 0-1

// Perfect match = vol matches risk tolerance
riskFitScore = 1 - |normalizedVol - normalizedRisk|
```

Category override rules (applied before scoring):
- `riskTolerance ≤ 2` → exclude STOCK/ETF from BUY recommendations (too risky for this client)
- `riskTolerance ≥ 4` → exclude GOVT from BUY recommendations (too conservative for this client)
- `investmentLength = Short` → prefer low-volatility instruments
- `investmentLength = Long` → stocks outperform bonds historically; prefer STOCK/ETF for long horizons

### 6.5 Category Preference Score (30% weight)

```
investmentPurpose + incomeCategory → preferred categories:

Retirement + LIG/MIG → prefer GOVT (capital preservation)
Retirement + HIG/VHIG → mixed (some growth acceptable)
Education → prefer STOCK/ETF (growth needed, medium term)
Major Expense → prefer low-vol instruments (capital preservation)

riskTolerance 1-2 → GOVT weight 0.8, STOCK 0.2
riskTolerance 3   → GOVT 0.4, STOCK/ETF 0.6
riskTolerance 4-5 → GOVT 0.1, STOCK/ETF 0.9
```

Score 1.0 if instrument category matches preferred categories, 0.0 otherwise, interpolated for partial matches.

### 6.6 Composite Score and Ranking

```
buyScore = (0.30 × momentumScore) + (0.40 × riskFitScore) + (0.30 × categoryScore)
```

Sort descending by buyScore. Return top `min(5, available)`.

**For SELL recommendations** (from current holdings only):
- A holding is a sell candidate if ANY of these apply:
  1. `unrealizedPL% < -5%` AND `riskTolerance ≤ 2` (loss threshold for conservative investors)
  2. `unrealizedPL% < -10%` AND `riskTolerance ≤ 3` (broader loss threshold)
  3. Instrument category no longer matches client's risk profile (e.g., riskTolerance dropped below 3 but they hold TSLA)
  4. Single holding > 40% of total portfolio value (concentration risk)
- Sort sell candidates by severity (largest concentration risk or deepest loss first).
- If no holdings meet sell criteria: return empty list, not random picks.

### 6.7 Required Field: `acceptAdvisor` Must Be `true`

If `acceptAdvisor` is false, return 403 immediately. Do not compute scores.

---

## 7. Market Data Rules

### 7.1 Trading Hours

NYSE trading hours: Monday–Friday, 09:30–16:00 Eastern Time (America/New_York).

Outside these hours:
- MARKET orders: rejected with `MARKET_CLOSED` error
- LIMIT orders: rejected with `MARKET_CLOSED` error
- Price feed: MDS continues to serve last-close prices (marked `marketOpen: false`)

### 7.2 Price Staleness

A price is considered stale if `now - priceTimestamp > 60 seconds`.

On a stale price:
- Trade execution: rejected with `PRICE_DATA_STALE` error
- Portfolio display: show prices with a "Delayed" indicator (Angular side)
- Robo advisor: refuse to compute recommendations, return `SERVICE_UNAVAILABLE`

### 7.3 Bid-Ask Spread Model

Since Yahoo Finance does not always return real bid/ask for all instruments, MDS synthesizes them:

**Stocks / ETFs:**
```
spread = lastPrice × 0.0005    // 0.05% — realistic for large-cap liquid stocks
bidPrice = lastPrice - (spread / 2)
askPrice = lastPrice + (spread / 2)
```

**Government Bonds:**
```
// 1/8 point spread (0.125) is the standard for on-the-run Treasuries
bidPrice = bondPrice - 0.0625
askPrice = bondPrice + 0.0625
```

### 7.4 Bond Pricing

Bond price is calculated from FRED daily yield using the standard present value formula.

```
P = (C/y) × (1 - (1+y)^-n) + F × (1+y)^-n

where:
  C = annual coupon payment (couponRate × 1000)
  y = current yield from FRED / 100
  n = years to maturity (from today to maturityDate)
  F = face value (1000)
```

FRED yields are daily — bond prices update once per day in practice, not intraday. This is accurate; Treasury yields do not change second-by-second.

If FRED is unavailable, use last known yield (do not serve stale bond prices older than 24 hours — mark as unavailable instead).

---

## 8. Validation Rules

### 8.1 Order Validation

| Field | Rule |
|-------|------|
| `instrumentId` | Must exist in INSTRUMENT table |
| `quantity` | Integer > 0; must satisfy instrument.minQuantity and instrument.maxQuantity |
| `direction` | Must be `"B"` or `"S"` |
| `orderType` | Must be `"MARKET"` or `"LIMIT"` |
| `targetPrice` | Required if LIMIT; must be > 0; ignored if MARKET |
| `clientId` | Must match JWT sub claim |
| `token` | Must be valid JWT (not expired) |

### 8.2 Client Registration Validation

| Field | Rule |
|-------|------|
| `email` | Valid format; must not already exist in DB |
| `password` | Min 8 chars; at least 1 uppercase, 1 lowercase, 1 digit |
| `country` | Must be `"India"` or `"USA"` |
| `identification.type` | Must match country: India → `Aadhar` or `PAN`; USA → `SSN` |
| `identification.value` | Aadhar: 12 digits; PAN: 5 upper + 4 digits + 1 upper; SSN: 9 digits |
| ID value | Must not already be registered to another account |

---

## 9. Known Simplifications (Intentional — Not Bugs)

These are deliberate deviations from real-world financial systems, accepted for project scope reasons. They are documented here so they are never mistaken for implementation bugs.

| Simplification | Real-world behavior | Reason for simplifying |
|---------------|--------------------|-----------------------|
| T+0 settlement | T+1 (US equities since May 2024) | Settlement cycle adds significant state machine complexity |
| IOC limit orders (no queuing) | GTC orders can sit open for days | No order book or job scheduler implemented |
| No fractional shares | Most modern brokers support fractional | Integer math is simpler; avoids quantity rounding issues |
| No dividends | Held stock pays periodic dividends | Requires corporate actions tracking |
| No corporate actions | Splits, mergers, spin-offs | Requires reference data feed |
| No tax lot tracking | FIFO/HIFO/Specific ID for tax purposes | Weighted average is simpler and still correct |
| No day P&L | Most platforms show today's P&L separately | Requires previous-close snapshot |
| No margin | Most brokers offer 2:1 leverage for equities | Risk management complexity |
| No short selling | Borrowing shares to sell first | Requires borrow market / availability check |
| No stop orders | Trigger orders when price crosses threshold | Requires persistent order monitoring job |
| No partial fills | Large orders sometimes fill in pieces | Requires order book |
| Bond prices are daily | Intraday bond price changes exist but are small | FRED API provides daily yields |
| Fixed spread model | Spreads widen during volatility | Acceptable for demo; could improve with VIX-adjusted spreads |

---

## 10. Error Codes Reference

Standardized error codes for financial domain failures:

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `MARKET_CLOSED` | 400 | Trade submitted outside NYSE hours |
| `PRICE_DATA_STALE` | 503 | Price data > 60s old; cannot execute |
| `LIMIT_NOT_MET` | 409 | Limit price condition not currently satisfiable |
| `INSUFFICIENT_BALANCE` | 402 | Cash balance < cost of BUY order |
| `INSUFFICIENT_HOLDINGS` | 400 | Quantity in holdings < quantity to sell |
| `INSTRUMENT_NOT_HELD` | 400 | Attempting to sell an instrument not in holdings |
| `INSTRUMENT_NOT_FOUND` | 404 | instrumentId does not exist |
| `ADVISOR_NOT_ACCEPTED` | 403 | acceptAdvisor is false; robo advisor not enabled |
| `ORDER_QTY_BELOW_MIN` | 400 | Quantity below instrument.minQuantity |
| `ORDER_QTY_ABOVE_MAX` | 400 | Quantity above instrument.maxQuantity |

---

## 11. How to Flag a Business Logic Issue

If during implementation you find that a rule here is wrong, incomplete, or inconsistent with real financial behavior:

1. Add a `<!-- REVIEW: your concern here -->` comment directly in this document below the affected rule.
2. Raise it in the session before writing code that depends on the rule.
3. Once resolved, update the rule, remove the `<!-- REVIEW -->` comment, and note the change.

Do not silently implement something that contradicts this document. The goal is financial correctness, not just passing tests.
