# Mid-Tier Service — REMOVED

> **This service has been removed from the architecture.** The directory `midtier-trade-nexus/` is retained for reference but is not part of the active implementation.

---

## Decision

The mid-tier was a pure passthrough proxy — it forwarded HTTP requests from Angular to Spring Boot and forwarded WebSocket frames from FIPS to browsers. It added:
- One extra network hop on every API call
- An additional failure point (if mid-tier is down, the entire frontend breaks)
- Complexity without proportional value at this scale

### What replaced it

| Mid-tier responsibility | New owner |
|------------------------|-----------|
| REST proxy to backend | Angular calls Spring Boot directly (port 8080) |
| WebSocket price broker | Angular connects directly to MDS (port 3001) |
| Rate limiting | Spring Boot — Bucket4j handles per-IP rate limiting natively |
| CORS | Spring Boot — `@CrossOrigin` / `WebMvcConfigurer` allows `localhost:4200` |
| Circuit breakers | Spring Boot — Resilience4j for MDS client calls |
| JWT passthrough | Angular AuthInterceptor attaches `Authorization: Bearer <token>` directly |

### Real trading platforms

Direct browser-to-data-service WebSocket connections are the standard pattern. Coinbase, Robinhood, and Bloomberg Terminal all push price data directly from the price server to the browser. Adding a broker hop between them degrades latency without meaningful security benefit (the MDS WebSocket requires JWT auth on every connection).

---

## If Mid-Tier is Ever Reconsidered

The only scenario where a mid-tier becomes worthwhile is if the application needs to:
- Fan out to many upstream data sources and aggregate them (e.g., combining multiple exchanges)
- Hide backend topology from clients in a multi-tenant deployment
- Apply complex per-session rate limiting that a single Spring Boot instance cannot handle

None of these apply at current scale. Revisit if the architecture grows to multiple backend instances behind a load balancer.
