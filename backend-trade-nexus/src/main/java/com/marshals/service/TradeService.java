package com.marshals.service;

import com.marshals.client.MdsClient;
import com.marshals.dto.OrderRequest;
import com.marshals.dto.Price;
import com.marshals.dto.TradeHistoryResponse;
import com.marshals.dto.TradeResponse;
import com.marshals.exception.InsufficientBalanceException;
import com.marshals.exception.LimitOrderNotMetException;
import com.marshals.exception.MarketClosedException;
import com.marshals.exception.PriceDataStaleException;
import com.marshals.model.Client;
import com.marshals.model.ClientOrder;
import com.marshals.model.ClientTrade;
import com.marshals.model.Holding;
import com.marshals.model.Instrument;
import com.marshals.repository.ClientOrderRepository;
import com.marshals.repository.ClientRepository;
import com.marshals.repository.ClientTradeRepository;
import com.marshals.repository.HoldingRepository;
import com.marshals.repository.InstrumentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TradeService {

    private static final BigDecimal BUY_FEE_FACTOR  = new BigDecimal("1.001");
    private static final BigDecimal SELL_FEE_FACTOR = new BigDecimal("0.999");
    private static final int STALENESS_SECONDS = 60;

    private final ClientRepository clientRepository;
    private final InstrumentRepository instrumentRepository;
    private final ClientOrderRepository orderRepository;
    private final ClientTradeRepository tradeRepository;
    private final HoldingRepository holdingRepository;
    private final MdsClient mdsClient;
    private final PortfolioService portfolioService;

    public TradeService(ClientRepository clientRepository,
                        InstrumentRepository instrumentRepository,
                        ClientOrderRepository orderRepository,
                        ClientTradeRepository tradeRepository,
                        HoldingRepository holdingRepository,
                        MdsClient mdsClient,
                        PortfolioService portfolioService) {
        this.clientRepository = clientRepository;
        this.instrumentRepository = instrumentRepository;
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.holdingRepository = holdingRepository;
        this.mdsClient = mdsClient;
        this.portfolioService = portfolioService;
    }

    // ─── Trade Execution ─────────────────────────────────────────────────────

    @Transactional
    public TradeResponse executeTrade(OrderRequest req) {
        // 1. Validate basic fields
        validateOrderRequest(req);

        // 2. Validate instrument exists in DB
        Instrument instrument = instrumentRepository.findById(req.getInstrumentId())
                .orElseThrow(() -> new NoSuchElementException("INSTRUMENT_NOT_FOUND"));

        // 3. Check market is open (both MARKET and LIMIT orders require open market)
        var marketStatus = mdsClient.getMarketStatus();
        if (marketStatus == null || !marketStatus.isMarketOpen()) {
            throw new MarketClosedException("MARKET_CLOSED");
        }

        // 4. Fetch fresh price from MDS
        Price price = mdsClient.getPrice(req.getInstrumentId());
        if (price == null) {
            throw new NoSuchElementException("INSTRUMENT_NOT_FOUND");
        }

        // 5. Staleness check — reject if price is > 60 seconds old
        if (isStale(price.getPriceTimestamp())) {
            throw new PriceDataStaleException("PRICE_DATA_STALE");
        }

        // 6. Apply order type logic to determine execution price
        BigDecimal executionPrice = resolveExecutionPrice(req, price);

        // 7. Quantity constraint check
        if (req.getQuantity() < instrument.getMinQuantity()) {
            throw new IllegalArgumentException("ORDER_QTY_BELOW_MIN");
        }
        if (req.getQuantity() > instrument.getMaxQuantity()) {
            throw new IllegalArgumentException("ORDER_QTY_ABOVE_MAX");
        }

        // 8. Compute cash value with fee
        BigDecimal qty = BigDecimal.valueOf(req.getQuantity());
        BigDecimal cashValue;
        Client client = clientRepository.findById(req.getClientId())
                .orElseThrow(() -> new NoSuchElementException("CLIENT_NOT_FOUND"));

        if ("B".equals(req.getDirection())) {
            cashValue = qty.multiply(executionPrice).multiply(BUY_FEE_FACTOR)
                    .setScale(4, RoundingMode.HALF_UP);
            if (client.getCurrBalance().compareTo(cashValue) < 0) {
                throw new InsufficientBalanceException("INSUFFICIENT_BALANCE");
            }
        } else {
            Holding holding = holdingRepository
                    .findByIdClientIdAndIdInstrumentId(req.getClientId(), req.getInstrumentId())
                    .orElseThrow(() -> new IllegalArgumentException("INSUFFICIENT_HOLDINGS"));
            if (holding.getQuantity() < req.getQuantity()) {
                throw new IllegalArgumentException("INSUFFICIENT_HOLDINGS");
            }
            cashValue = qty.multiply(executionPrice).multiply(SELL_FEE_FACTOR)
                    .setScale(4, RoundingMode.HALF_UP);
        }

        // 9. Persist order
        ClientOrder order = new ClientOrder();
        order.setOrderId(req.getOrderId() != null ? req.getOrderId() : UUID.randomUUID().toString());
        order.setInstrument(instrument);
        order.setQuantity(req.getQuantity());
        order.setTargetPrice(req.getTargetPrice());
        order.setDirection(req.getDirection());
        order.setOrderType(req.getOrderType());
        order.setClient(client);
        order.setToken(req.getToken() != null ? req.getToken() : "");
        orderRepository.save(order);

        // 10. Persist trade
        ClientTrade trade = new ClientTrade();
        trade.setTradeId(UUID.randomUUID().toString());
        trade.setOrder(order);
        trade.setExecutionPrice(executionPrice.setScale(4, RoundingMode.HALF_UP));
        trade.setCashValue(cashValue);
        tradeRepository.save(trade);

        // 11. Update portfolio (balance + holdings)
        if ("B".equals(req.getDirection())) {
            portfolioService.applyBuy(req.getClientId(), req.getInstrumentId(), req.getQuantity(), cashValue);
        } else {
            portfolioService.applySell(req.getClientId(), req.getInstrumentId(), req.getQuantity(), cashValue);
        }

        return TradeResponse.from(trade);
    }

    @Transactional(readOnly = true)
    public TradeHistoryResponse getTradeHistory(String clientId) {
        List<TradeResponse> trades = tradeRepository
                .findByOrderClientClientId(clientId)
                .stream()
                .sorted(Comparator.comparing(t -> t.getExecutedAt()))
                .map(TradeResponse::from)
                .collect(Collectors.toList());
        return new TradeHistoryResponse(clientId, trades);
    }

    // ─── Robo Advisor — BUY ──────────────────────────────────────────────────

    public List<Price> suggestBuy(com.marshals.model.ClientPreferences prefs) {
        if (!prefs.isAcceptAdvisor()) {
            throw new AccessDeniedException("ADVISOR_NOT_ACCEPTED");
        }

        List<Price> allPrices = mdsClient.getAllPrices();
        int risk = prefs.getRiskTolerance();

        // Category filter: exclude categories that don't match risk tolerance
        List<Price> eligible = allPrices.stream()
                .filter(p -> {
                    String cat = resolveCategory(p.getInstrumentId(), allPrices);
                    if (risk <= 2 && ("STOCK".equals(cat) || "ETF".equals(cat))) return false;
                    if (risk >= 4 && "GOVT".equals(cat)) return false;
                    return true;
                })
                .collect(Collectors.toList());

        // Score each instrument
        List<ScoredPrice> scored = eligible.stream()
                .map(p -> new ScoredPrice(p, scoreBuy(p, prefs)))
                .sorted(Comparator.comparingDouble(ScoredPrice::score).reversed())
                .collect(Collectors.toList());

        return scored.subList(0, Math.min(5, scored.size()))
                .stream()
                .map(ScoredPrice::price)
                .collect(Collectors.toList());
    }

    // ─── Robo Advisor — SELL ─────────────────────────────────────────────────

    public List<Holding> suggestSell(com.marshals.model.ClientPreferences prefs) {
        if (!prefs.isAcceptAdvisor()) {
            throw new AccessDeniedException("ADVISOR_NOT_ACCEPTED");
        }

        String clientId = prefs.getClientId();
        List<Holding> holdings = holdingRepository.findByIdClientId(clientId);
        if (holdings.isEmpty()) return List.of();

        List<Price> prices = mdsClient.getAllPrices();
        Map<String, Price> priceMap = prices.stream()
                .collect(Collectors.toMap(Price::getInstrumentId, p -> p, (a, b) -> a));

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("CLIENT_NOT_FOUND"));

        // Compute total portfolio value for concentration check
        BigDecimal totalValue = client.getCurrBalance();
        for (Holding h : holdings) {
            Price p = priceMap.get(h.getInstrumentId());
            if (p != null) {
                totalValue = totalValue.add(p.getBidPrice().multiply(BigDecimal.valueOf(h.getQuantity())));
            }
        }

        BigDecimal total = totalValue;
        int risk = prefs.getRiskTolerance();

        // Enrich holdings and filter sell candidates
        List<ScoredHolding> candidates = holdings.stream()
                .filter(h -> priceMap.containsKey(h.getInstrumentId()))
                .map(h -> {
                    Price p = priceMap.get(h.getInstrumentId());
                    enrichHolding(h);

                    double unrealizedPLPct = unrealizedPLPercent(h, p.getBidPrice());
                    double concentration = total.compareTo(BigDecimal.ZERO) == 0 ? 0
                            : p.getBidPrice().multiply(BigDecimal.valueOf(h.getQuantity()))
                              .divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100;

                    boolean lossCandidate =
                            (unrealizedPLPct < -5.0 && risk <= 2) ||
                            (unrealizedPLPct < -10.0 && risk <= 3);

                    boolean categoryMismatch = isCategoryMismatch(h.getCategoryId(), risk);
                    boolean overConcentrated = concentration > 40.0;

                    boolean isSellCandidate = lossCandidate || categoryMismatch || overConcentrated;

                    // severity score: deeper loss and higher concentration = more urgent to sell
                    double severity = Math.abs(Math.min(unrealizedPLPct, 0)) + concentration;
                    return new ScoredHolding(h, severity, isSellCandidate);
                })
                .filter(ScoredHolding::isCandidate)
                .sorted(Comparator.comparingDouble(ScoredHolding::severity).reversed())
                .collect(Collectors.toList());

        return candidates.subList(0, Math.min(5, candidates.size()))
                .stream()
                .map(ScoredHolding::holding)
                .collect(Collectors.toList());
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private void validateOrderRequest(OrderRequest req) {
        if (req.getInstrumentId() == null || req.getInstrumentId().isBlank()) {
            throw new IllegalArgumentException("MISSING_INSTRUMENT_ID");
        }
        if (req.getQuantity() <= 0) {
            throw new IllegalArgumentException("INVALID_QUANTITY");
        }
        if (!"B".equals(req.getDirection()) && !"S".equals(req.getDirection())) {
            throw new IllegalArgumentException("INVALID_DIRECTION");
        }
        if (!"MARKET".equals(req.getOrderType()) && !"LIMIT".equals(req.getOrderType())) {
            throw new IllegalArgumentException("INVALID_ORDER_TYPE");
        }
        if ("LIMIT".equals(req.getOrderType()) && req.getTargetPrice() == null) {
            throw new IllegalArgumentException("TARGET_PRICE_REQUIRED_FOR_LIMIT");
        }
        if (req.getClientId() == null || req.getClientId().isBlank()) {
            throw new IllegalArgumentException("MISSING_CLIENT_ID");
        }
    }

    private boolean isStale(String priceTimestamp) {
        try {
            Instant priceTime = Instant.parse(priceTimestamp);
            return Duration.between(priceTime, Instant.now()).getSeconds() > STALENESS_SECONDS;
        } catch (Exception e) {
            return true;
        }
    }

    private BigDecimal resolveExecutionPrice(OrderRequest req, Price price) {
        boolean isBuy = "B".equals(req.getDirection());
        BigDecimal marketPrice = isBuy ? price.getAskPrice() : price.getBidPrice();

        if ("LIMIT".equals(req.getOrderType())) {
            // BUY limit: execute only if askPrice ≤ targetPrice
            // SELL limit: execute only if bidPrice ≥ targetPrice
            boolean conditionMet = isBuy
                    ? marketPrice.compareTo(req.getTargetPrice()) <= 0
                    : marketPrice.compareTo(req.getTargetPrice()) >= 0;
            if (!conditionMet) {
                throw new LimitOrderNotMetException("LIMIT_NOT_MET");
            }
        }

        return marketPrice;
    }

    private double scoreBuy(Price price, com.marshals.model.ClientPreferences prefs) {
        List<Price> history = mdsClient.getPriceHistory(price.getInstrumentId());
        double momentum = calcMomentumScore(history);
        double riskFit = calcRiskFitScore(history, prefs.getRiskTolerance());
        double category = calcCategoryScore(resolveCategory(price.getInstrumentId(), null), prefs);
        return 0.30 * momentum + 0.40 * riskFit + 0.30 * category;
    }

    private double calcMomentumScore(List<Price> history) {
        if (history == null || history.size() < 5) return 0.5;

        int n = history.size();
        double ma5 = history.subList(Math.max(0, n - 5), n).stream()
                .mapToDouble(p -> p.getLastPrice().doubleValue()).average().orElse(0);
        double ma20 = history.subList(Math.max(0, n - 20), n).stream()
                .mapToDouble(p -> p.getLastPrice().doubleValue()).average().orElse(0);

        if (ma20 == 0) return 0.5;
        double momentum = (ma5 - ma20) / ma20;
        // Normalize: clamp momentum from [-0.05, 0.05] to [0, 1]
        return Math.max(0.0, Math.min(1.0, (momentum + 0.05) / 0.10));
    }

    private double calcRiskFitScore(List<Price> history, int riskTolerance) {
        if (history == null || history.size() < 2) return 0.5;

        double[] prices = history.stream()
                .mapToDouble(p -> p.getLastPrice().doubleValue())
                .toArray();
        double[] returns = new double[prices.length - 1];
        for (int i = 0; i < returns.length; i++) {
            if (prices[i] != 0) returns[i] = (prices[i + 1] - prices[i]) / prices[i];
        }

        double mean = Arrays.stream(returns).average().orElse(0);
        double variance = Arrays.stream(returns)
                .map(r -> (r - mean) * (r - mean))
                .average()
                .orElse(0);
        double volatility = Math.sqrt(variance);

        double normalizedVol  = Math.min(1.0, volatility / 0.03);
        double normalizedRisk = (riskTolerance - 1) / 4.0;
        return 1.0 - Math.abs(normalizedVol - normalizedRisk);
    }

    private double calcCategoryScore(String categoryId, com.marshals.model.ClientPreferences prefs) {
        if (categoryId == null) return 0.5;
        int risk = prefs.getRiskTolerance();
        boolean isGovt = "GOVT".equals(categoryId);

        if (risk <= 2) return isGovt ? 0.8 : 0.2;
        if (risk == 3) return isGovt ? 0.4 : 0.6;
        return isGovt ? 0.1 : 0.9;
    }

    private String resolveCategory(String instrumentId, List<Price> ignored) {
        return instrumentRepository.findById(instrumentId)
                .map(i -> i.getCategoryId())
                .orElse(null);
    }

    private void enrichHolding(Holding h) {
        instrumentRepository.findById(h.getInstrumentId()).ifPresent(i -> {
            h.setInstrumentDescription(i.getDescription());
            h.setCategoryId(i.getCategoryId());
        });
    }

    private double unrealizedPLPercent(Holding h, BigDecimal bidPrice) {
        if (h.getAvgPrice().compareTo(BigDecimal.ZERO) == 0) return 0;
        return bidPrice.subtract(h.getAvgPrice())
                .divide(h.getAvgPrice(), 6, RoundingMode.HALF_UP)
                .doubleValue() * 100;
    }

    private boolean isCategoryMismatch(String categoryId, int riskTolerance) {
        if (categoryId == null) return false;
        // Conservative investor holding aggressive instruments
        boolean holdingStock = "STOCK".equals(categoryId) || "ETF".equals(categoryId);
        if (holdingStock && riskTolerance <= 2) return true;
        // Aggressive investor holding only bonds (less of a concern — not flagged)
        return false;
    }

    // ─── Inner scoring helpers ────────────────────────────────────────────────

    private record ScoredPrice(Price price, double score) {}

    private record ScoredHolding(Holding holding, double severity, boolean isCandidate) {}
}
