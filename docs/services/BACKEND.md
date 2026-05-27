# Backend Service — Implementation Spec

**Directory:** `backend-trade-nexus/`
**Port:** 8080
**Stack:** Java 21, Spring Boot 3.2, Spring Data JPA, Spring Security, PostgreSQL
**Phase:** 3 (after DB schema is finalized and MDS is running)
**Branch:** `feature/backend/spring-boot-3`

> **Business Logic Vigilance:** This service owns all financial logic. Every feature touching trade execution, P&L, portfolio state, or robo advisor scoring must be verified against `docs/BUSINESS_LOGIC.md` before coding. See that document for the correct formulas — the original implementation had several financially incorrect rules.

---

## Responsibility

Spring Boot is the single owner of all business logic and authentication.

| Concern | Owner | Notes |
|---------|-------|-------|
| JWT issuance | Spring Boot | HS256, 30-min expiry, signed with `JWT_SECRET` |
| BCrypt password hashing | Spring Boot | Cost factor 12; passwords never leave the backend |
| Trade validation + execution | Spring Boot | Fetches fresh price from MDS per trade |
| Portfolio state | Spring Boot | Balance, holdings, weighted avg cost basis |
| Realized + unrealized P&L | Spring Boot | Unrealized uses live MDS bid price |
| Activity reports | Spring Boot | Holdings, trade history, P&L |
| Robo advisor scoring | Spring Boot | 3-factor: momentum + risk fit + category preference |
| Client preferences | Spring Boot | |
| CORS | Spring Boot | Allows `localhost:4200` (Angular) |
| Rate limiting | Spring Boot | Bucket4j per-IP limits on trade endpoints |
| Instrument list | Spring Boot | Reads from DB (seeded from MDS instruments) |

---

## Key Changes from Old Design

| Old | New |
|-----|-----|
| Spring Boot 2.6 | Spring Boot 3.2 (`javax.*` → `jakarta.*`) |
| Java 17 | Java 21 |
| MyBatis + mapper XML | Spring Data JPA + Hibernate |
| SQLite + JDBC | PostgreSQL + Spring Data JPA |
| Plain-text passwords | BCrypt hashing (cost factor 12) |
| `GET /client?password=x` | `POST /auth/login` with JSON body |
| FIPS issued JWT; backend validated it | Spring Boot issues **and** validates JWT end-to-end |
| FIPS URL hardcoded `localhost:3000` | `${MDS_URL}` env var |
| `priceList` loaded once at startup | Fresh MDS REST call per trade execution |
| `token INTEGER` in Order | `token String` in Order |
| `is_advisor_accepted String` with `== "false"` bug | `acceptAdvisor boolean` |
| 5% price tolerance (arbitrary) | MARKET and LIMIT order types |
| 1% fee on BUY only | 0.1% fee on both BUY and SELL |
| Robo advisor scores by bid-ask spread | 3-factor: momentum + risk fit + category preference |
| `subList(0, 5)` throws if < 5 results | `subList(0, Math.min(5, list.size()))` |
| No trading hours enforcement | Reject MARKET orders when NYSE is closed |

---

## File Structure

```
backend-trade-nexus/
  src/
    main/
      java/com/tradenexus/
        config/
          WebConfig.java              # CORS — allow localhost:4200
          SecurityConfig.java         # Spring Security: JWT filter, public vs protected routes
          BucketConfig.java           # Bucket4j rate limit config
        model/
          Client.java                 # @Entity
          ClientIdentification.java   # @Entity
          ClientPreferences.java      # @Entity
          Holding.java                # @Entity
          Instrument.java             # @Entity (read-only; managed by Flyway seed)
          ClientOrder.java            # @Entity
          ClientTrade.java            # @Entity
          Price.java                  # POJO — from MDS REST
          Trade.java                  # POJO — returned after execution
          TradePL.java                # POJO — P&L report row
          ClientProfile.java          # POJO response DTO (login/register response)
        repository/
          ClientRepository.java
          InstrumentRepository.java
          HoldingRepository.java
          ClientOrderRepository.java
          ClientTradeRepository.java
          ClientPreferencesRepository.java
        service/
          AuthService.java            # Login, register, token refresh
          TradeService.java           # Trade execution, robo advisor
          PortfolioService.java       # Portfolio CRUD, P&L
          ClientPreferencesService.java
          ActivityReportService.java
          MdsClient.java              # RestTemplate wrapper for MDS REST calls
        controller/
          AuthController.java         # /auth/login, /auth/register, /auth/refresh
          ClientController.java       # /client/verify-email, /client/:id
          TradeController.java
          PortfolioController.java
          ClientPreferencesController.java
          ActivityReportController.java
          InstrumentController.java
        exception/
          GlobalExceptionHandler.java
          InsufficientBalanceException.java
          MarketClosedException.java
          LimitOrderNotMetException.java
          PriceDataStaleException.java
        security/
          JwtUtil.java                # Issue + validate JWT (Spring Boot owns both)
          JwtAuthFilter.java          # OncePerRequestFilter — validates on protected routes
      resources/
        application.properties
        application-dev.properties
  pom.xml
  Dockerfile
```

---

## Authentication Design

Spring Boot is the **sole owner** of JWT issuance. There is no external auth service.

### JWT Flow

```
1. POST /auth/login
   → ClientRepository.findByEmail()
   → BCrypt.matches(plainPassword, storedHash)
   → JwtUtil.issueToken(clientId, email, isAdmin)  ← Spring Boot issues here
   → Return ClientProfile { client, token }

2. POST /auth/register
   → Validate email uniqueness
   → Validate ID uniqueness
   → BCrypt.encode(plainPassword)
   → Save Client with $10k initial balance
   → JwtUtil.issueToken(...)
   → Return ClientProfile

3. POST /auth/refresh
   → JwtUtil.validate(existingToken)  ← verify not expired yet (within refresh window)
   → JwtUtil.issueToken(...)          ← issue new token
   → Return { token, expiresAt }

4. Protected endpoint
   → JwtAuthFilter extracts Bearer token
   → JwtUtil.validate(token)          ← throws if expired or invalid signature
   → Set SecurityContext with clientId, email, isAdmin
```

### JwtUtil

```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    public String issueToken(String clientId, String email, boolean isAdmin) {
        return Jwts.builder()
            .setSubject(clientId)
            .claim("email", email)
            .claim("isAdmin", isAdmin)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
            .compact();
    }

    public Claims validate(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
```

MDS receives `JWT_SECRET` as a read-only env var to validate WebSocket connections. It never issues tokens.

---

## JPA Entities

### Client (`@Entity @Table(name = "client")`)
```java
@Entity
@Table(name = "client")
public class Client {
    @Id
    private String clientId;                  // UUID, generated by backend on register
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;                  // BCrypt hash — NEVER return in API response
    private String name;
    private LocalDate dateOfBirth;
    private String country;
    private boolean isAdmin;
    @Column(precision = 19, scale = 4)
    private BigDecimal currBalance;
    @CreationTimestamp
    private OffsetDateTime createdAt;
}
```

### Holding
```java
@Entity
@Table(name = "holdings")
@IdClass(HoldingId.class)
public class Holding {
    @Id private String clientId;
    @Id private String instrumentId;
    private int quantity;
    @Column(precision = 19, scale = 4)
    private BigDecimal avgPrice;             // Weighted average cost basis

    @Transient private String instrumentDescription;
    @Transient private String categoryId;
}
```

Populate `@Transient` fields in `PortfolioService.getClientPortfolio()` via `InstrumentRepository`.

### ClientOrder
```java
@Entity
@Table(name = "client_order")
public class ClientOrder {
    @Id private String orderId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;
    private int quantity;
    @Column(precision = 19, scale = 4)
    private BigDecimal targetPrice;
    @Enumerated(EnumType.STRING)
    private TradeDirection direction;         // B or S
    @Enumerated(EnumType.STRING)
    private OrderType orderType;             // MARKET or LIMIT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;
    private String token;                    // JWT string (kept for audit trail)
    @CreationTimestamp
    private OffsetDateTime createdAt;
}
```

---

## Trade Execution (MARKET and LIMIT Orders)

See `docs/BUSINESS_LOGIC.md` for the complete financial rules. Summary:

```java
public Trade executeTrade(OrderRequest order) {
    // 1. Fetch fresh price from MDS (never use cached startup prices)
    Price price = mdsClient.getPrice(order.instrumentId());
    if (price == null) throw new IllegalArgumentException("INSTRUMENT_NOT_FOUND");

    // 2. Staleness check (reject if price is > 60s old)
    if (priceIsStale(price)) throw new PriceDataStaleException("PRICE_DATA_STALE");

    BigDecimal executionPrice;

    if (order.orderType() == OrderType.MARKET) {
        // 3a. MARKET: check NYSE open; reject if closed
        if (!price.isMarketOpen()) throw new MarketClosedException("MARKET_CLOSED");
        // Execution at current market price
        executionPrice = "B".equals(order.direction()) ? price.askPrice() : price.bidPrice();

    } else {
        // 3b. LIMIT: IOC semantics — execute now if limit is met, else reject
        executionPrice = "B".equals(order.direction()) ? price.askPrice() : price.bidPrice();
        if ("B".equals(order.direction()) && executionPrice.compareTo(order.targetPrice()) > 0)
            throw new LimitOrderNotMetException("LIMIT_NOT_MET");
        if ("S".equals(order.direction()) && executionPrice.compareTo(order.targetPrice()) < 0)
            throw new LimitOrderNotMetException("LIMIT_NOT_MET");
    }

    // 4. Fee calculation (0.1% both sides)
    // BUY:  cashValue = quantity × askPrice × 1.001
    // SELL: cashValue = quantity × bidPrice × 0.999
    BigDecimal qty = BigDecimal.valueOf(order.quantity());
    BigDecimal cashValue;
    if ("B".equals(order.direction())) {
        cashValue = qty.multiply(executionPrice).multiply(new BigDecimal("1.001"));
        // 5. Balance check
        Client client = clientRepository.findById(order.clientId()).orElseThrow();
        if (client.getCurrBalance().compareTo(cashValue) < 0)
            throw new InsufficientBalanceException("INSUFFICIENT_BALANCE");
    } else {
        // 5. Holdings check
        Holding holding = holdingRepository
            .findByClientIdAndInstrumentId(order.clientId(), order.instrumentId())
            .orElseThrow(() -> new IllegalArgumentException("INSUFFICIENT_HOLDINGS"));
        if (holding.getQuantity() < order.quantity())
            throw new IllegalArgumentException("INSUFFICIENT_HOLDINGS");
        cashValue = qty.multiply(executionPrice).multiply(new BigDecimal("0.999"));
    }

    // 6. Save order + trade records
    // 7. Update portfolio (applyBuy or applySell)
    // 8. Return Trade
}

private boolean priceIsStale(Price price) {
    Instant priceTime = Instant.parse(price.priceTimestamp());
    return Duration.between(priceTime, Instant.now()).getSeconds() > 60;
}
```

---

## Portfolio Management (Weighted Average Cost Basis)

```java
// BUY path
void applyBuy(Trade trade) {
    Optional<Holding> existing = holdingRepository
        .findByClientIdAndInstrumentId(trade.clientId(), trade.instrumentId());

    if (existing.isPresent()) {
        Holding h = existing.get();
        // Weighted average: (oldAvg × oldQty + cashValue) / (oldQty + newQty)
        // Note: cashValue already includes fee, so avg cost basis includes fee
        BigDecimal totalCost = h.getAvgPrice()
            .multiply(BigDecimal.valueOf(h.getQuantity()))
            .add(trade.getCashValue());
        int newQty = h.getQuantity() + trade.getQuantity();
        h.setAvgPrice(totalCost.divide(BigDecimal.valueOf(newQty), 4, RoundingMode.HALF_UP));
        h.setQuantity(newQty);
    } else {
        Holding h = new Holding(trade.clientId(), trade.instrumentId(),
            trade.getQuantity(),
            trade.getCashValue().divide(BigDecimal.valueOf(trade.getQuantity()), 4, RoundingMode.HALF_UP));
        holdingRepository.save(h);
    }
    // Deduct cash
    Client client = clientRepository.findById(trade.clientId()).orElseThrow();
    client.setCurrBalance(client.getCurrBalance().subtract(trade.getCashValue()));
    clientRepository.save(client);
}

// SELL path
void applySell(Trade trade) {
    Holding h = holdingRepository
        .findByClientIdAndInstrumentId(trade.clientId(), trade.instrumentId()).orElseThrow();
    // avgPrice unchanged on sell
    h.setQuantity(h.getQuantity() - trade.getQuantity());
    if (h.getQuantity() == 0) holdingRepository.delete(h);
    else holdingRepository.save(h);
    // Credit net proceeds
    Client client = clientRepository.findById(trade.clientId()).orElseThrow();
    client.setCurrBalance(client.getCurrBalance().add(trade.getCashValue()));
    clientRepository.save(client);
}
```

---

## Robo Advisor (3-Factor Scoring)

See `docs/BUSINESS_LOGIC.md` for full formula details. Implementation outline:

```java
public List<Price> suggestBuy(ClientPreferences prefs) {
    if (!prefs.isAcceptAdvisor())
        throw new AccessDeniedException("ADVISOR_DISABLED");

    List<Price> allPrices = mdsClient.getAllPrices();
    List<Price> eligible = filterByPreferredCategories(allPrices, prefs);

    // Score each instrument: momentum (30%) + riskFit (40%) + categoryPref (30%)
    return eligible.stream()
        .map(p -> new ScoredPrice(p, score(p, prefs)))
        .sorted(Comparator.comparingDouble(ScoredPrice::score).reversed())
        .limit(Math.min(5, eligible.size()))  // Fix: was subList(0,5) throwing on < 5
        .map(ScoredPrice::price)
        .collect(Collectors.toList());
}

private double score(Price price, ClientPreferences prefs) {
    List<Price> history = mdsClient.getPriceHistory(price.instrumentId());  // 30-day
    double momentum      = calcMomentum(history);        // MA5/MA20 ratio − 1
    double riskFit       = calcRiskFit(price, prefs);    // based on volatility vs riskTolerance
    double categoryPref  = calcCategoryPref(price, prefs);

    return 0.30 * normalize(momentum)
         + 0.40 * riskFit
         + 0.30 * categoryPref;
}
```

SELL recommendations are based on holdings that are in loss or concentrated (not random picks):

```java
public List<Holding> suggestSell(String clientId, ClientPreferences prefs) {
    if (!prefs.isAcceptAdvisor())
        throw new AccessDeniedException("ADVISOR_DISABLED");

    List<Holding> holdings = holdingRepository.findByClientId(clientId);
    List<Price> prices = mdsClient.getAllPrices();

    return holdings.stream()
        .map(h -> enrich(h, prices))
        .filter(h -> isSellCandidate(h))  // loss > 5% OR concentration > 25% of portfolio
        .sorted(Comparator.comparingDouble(HoldingWithPL::unrealizedPLPercent))
        .limit(Math.min(5, holdings.size()))
        .collect(Collectors.toList());
}
```

---

## MDS Client (`service/MdsClient.java`)

```java
@Service
public class MdsClient {
    @Value("${mds.url:http://localhost:3001}")
    private String mdsUrl;

    private final RestTemplate restTemplate;

    public List<Price> getAllPrices() {
        return Arrays.asList(restTemplate.getForObject(mdsUrl + "/prices", Price[].class));
    }

    public Price getPrice(String instrumentId) {
        return restTemplate.getForObject(mdsUrl + "/prices/" + instrumentId, Price.class);
    }

    public List<Price> getPriceHistory(String instrumentId) {
        return Arrays.asList(restTemplate.getForObject(
            mdsUrl + "/prices/" + instrumentId + "/history", Price[].class));
    }

    public MarketStatus getMarketStatus() {
        return restTemplate.getForObject(mdsUrl + "/market-status", MarketStatus.class);
    }
}
```

---

## application.properties

```properties
# JWT — Spring Boot issues and validates
jwt.secret=${JWT_SECRET}

# MDS (Market Data Service)
mds.url=${MDS_URL:http://localhost:3001}

# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${DB_USER:tn_user}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# App
app.initial-balance=${INITIAL_BALANCE:10000}
server.port=8080

# CORS — allow Angular dev server only
allowed.origins=http://localhost:4200
```

---

## pom.xml Key Dependencies

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.3</version>
</parent>
<properties><java.version>21</java.version></properties>

<dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
    <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
    <!-- JWT (JJWT 0.12.x) -->
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.12.3</version></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.12.3</version><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.12.3</version><scope>runtime</scope></dependency>
    <!-- Rate limiting -->
    <dependency><groupId>com.github.bucket4j</groupId><artifactId>bucket4j-core</artifactId><version>8.7.0</version></dependency>
    <!-- Resilience4j for MDS client -->
    <dependency><groupId>io.github.resilience4j</groupId><artifactId>resilience4j-spring-boot3</artifactId><version>2.1.0</version></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
</dependencies>
```

---

## Error Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(error(400, e.getMessage()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleBalance(InsufficientBalanceException e) {
        return ResponseEntity.status(402).body(error(402, e.getMessage()));
    }

    @ExceptionHandler({MarketClosedException.class, LimitOrderNotMetException.class,
                        PriceDataStaleException.class})
    public ResponseEntity<ErrorResponse> handleTrade(RuntimeException e) {
        return ResponseEntity.status(409).body(error(409, e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException e) {
        return ResponseEntity.status(403).body(error(403, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        return ResponseEntity.status(500).body(error(500, "Unexpected error"));
    }
}
```

Error codes returned in `message` field match the canonical list in `docs/BUSINESS_LOGIC.md`:
`MARKET_CLOSED`, `PRICE_DATA_STALE`, `LIMIT_NOT_MET`, `INSUFFICIENT_BALANCE`, `INSUFFICIENT_HOLDINGS`, `ADVISOR_DISABLED`, `EMAIL_TAKEN`, `ID_TAKEN`, `INSTRUMENT_NOT_FOUND`

---

## Verification Checklist

```bash
# 1. Start (requires MDS + PostgreSQL running)
./mvnw spring-boot:run

# 2. Register new client
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test1234","name":"Test User","dateOfBirth":"1990-01-01","country":"USA","identification":[{"type":"SSN","value":"123-45-6789"}]}'
# Expect: ClientProfile { client: {...}, token: "<JWT string>" }

# 3. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test1234"}'
# Expect: ClientProfile with fresh JWT

# 4. Token refresh
curl -X POST http://localhost:8080/auth/refresh \
  -H "Authorization: Bearer <token>"
# Expect: { token: "<new JWT>", expiresAt: "..." }

# 5. Instruments
curl -H "Authorization: Bearer <token>" http://localhost:8080/instrument
# Expect: 12 instruments from DB

# 6. MARKET BUY trade
curl -X POST http://localhost:8080/trade/execute-trade \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"orderId":"<uuid>","instrumentId":"AAPL","quantity":1,"direction":"B","orderType":"MARKET","clientId":"<id>"}'
# Expect: Trade { executionPrice: <real price>, cashValue: qty × askPrice × 1.001 }

# 7. LIMIT order rejection
# Submit LIMIT BUY with targetPrice well below current ask
# Expect: 409 LIMIT_NOT_MET

# 8. P&L report
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/activity-report/pl/<clientId>"
# Expect: TradePL[] with realizedPL and unrealizedPL

# 9. Robo advisor (requires preferences with acceptAdvisor=true)
curl -X POST http://localhost:8080/trade/suggest-buy \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"clientId":"<id>","acceptAdvisor":true,...}'
# Expect: up to 5 Price[] — not 500 error from subList bug
```
