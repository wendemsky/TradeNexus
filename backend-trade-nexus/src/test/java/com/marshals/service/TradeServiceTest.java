package com.marshals.service;

import com.marshals.client.MdsClient;
import com.marshals.dto.MarketStatus;
import com.marshals.dto.OrderRequest;
import com.marshals.dto.Price;
import com.marshals.dto.TradeHistoryResponse;
import com.marshals.dto.TradeResponse;
import com.marshals.exception.InsufficientBalanceException;
import com.marshals.exception.LimitOrderNotMetException;
import com.marshals.exception.MarketClosedException;
import com.marshals.exception.PriceDataStaleException;
import com.marshals.model.Client;
import com.marshals.model.ClientPreferences;
import com.marshals.model.Holding;
import com.marshals.model.Instrument;
import com.marshals.repository.ClientOrderRepository;
import com.marshals.repository.ClientRepository;
import com.marshals.repository.ClientTradeRepository;
import com.marshals.repository.HoldingRepository;
import com.marshals.repository.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock ClientRepository clientRepository;
    @Mock InstrumentRepository instrumentRepository;
    @Mock ClientOrderRepository orderRepository;
    @Mock ClientTradeRepository tradeRepository;
    @Mock HoldingRepository holdingRepository;
    @Mock MdsClient mdsClient;
    @Mock PortfolioService portfolioService;
    @InjectMocks TradeService tradeService;

    static final String CLIENT_ID = "541107416";
    static final String INSTR_ID  = "AAPL";

    Client client;
    Instrument instrument;
    MarketStatus openMarket;
    Price freshPrice;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId(CLIENT_ID);
        client.setCurrBalance(new BigDecimal("50000.0000"));

        instrument = new Instrument();
        instrument.setInstrumentId(INSTR_ID);
        instrument.setCategoryId("STOCK");
        instrument.setDescription("Apple Inc.");
        instrument.setMinQuantity(1);
        instrument.setMaxQuantity(1000);

        openMarket = new MarketStatus();
        openMarket.setMarketOpen(true);

        freshPrice = new Price();
        freshPrice.setInstrumentId(INSTR_ID);
        freshPrice.setAskPrice(new BigDecimal("200.0000"));
        freshPrice.setBidPrice(new BigDecimal("199.5000"));
        freshPrice.setLastPrice(new BigDecimal("199.7500"));
        freshPrice.setPriceTimestamp(Instant.now().toString());
    }

    // ─── executeTrade: BUY MARKET ─────────────────────────────────────────────

    @Test
    void executeTrade_buyMarket_computesCorrectCashValueAndCallsPortfolio() {
        OrderRequest req = buyMarketOrder(10);

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        // cashValue = 10 × 200.0000 × 1.001 = 2002.0000
        TradeResponse result = tradeService.executeTrade(req);

        assertThat(result.getInstrumentId()).isEqualTo(INSTR_ID);
        assertThat(result.getDirection()).isEqualTo("B");
        assertThat(result.getCashValue()).isEqualByComparingTo("2002.0000");
        assertThat(result.getExecutionPrice()).isEqualByComparingTo("200.0000");
        verify(portfolioService).applyBuy(eq(CLIENT_ID), eq(INSTR_ID), eq(10),
                argThat(v -> v.compareTo(new BigDecimal("2002.0000")) == 0));
    }

    @Test
    void executeTrade_buyMarket_insufficientBalance_throws() {
        client.setCurrBalance(new BigDecimal("100.0000")); // too low for 10 × 200 × 1.001 = 2002
        OrderRequest req = buyMarketOrder(10);

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("INSUFFICIENT_BALANCE");
    }

    // ─── executeTrade: SELL MARKET ────────────────────────────────────────────

    @Test
    void executeTrade_sellMarket_computesCorrectCashValueAndCallsPortfolio() {
        OrderRequest req = sellMarketOrder(5);
        Holding holding = new Holding(CLIENT_ID, INSTR_ID, 10, new BigDecimal("180.0000"));

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(holdingRepository.findByIdClientIdAndIdInstrumentId(CLIENT_ID, INSTR_ID))
                .thenReturn(Optional.of(holding));

        // cashValue = 5 × 199.5000 × 0.999 = 996.5025
        TradeResponse result = tradeService.executeTrade(req);

        assertThat(result.getDirection()).isEqualTo("S");
        assertThat(result.getCashValue()).isEqualByComparingTo("996.5025");
        verify(portfolioService).applySell(eq(CLIENT_ID), eq(INSTR_ID), eq(5),
                argThat(v -> v.compareTo(new BigDecimal("996.5025")) == 0));
    }

    @Test
    void executeTrade_sellMoreThanHeld_throws() {
        OrderRequest req = sellMarketOrder(20); // only holds 10
        Holding holding = new Holding(CLIENT_ID, INSTR_ID, 10, new BigDecimal("180.0000"));

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(holdingRepository.findByIdClientIdAndIdInstrumentId(CLIENT_ID, INSTR_ID))
                .thenReturn(Optional.of(holding));

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INSUFFICIENT_HOLDINGS");
    }

    @Test
    void executeTrade_sellWithNoHolding_throws() {
        OrderRequest req = sellMarketOrder(5);

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(holdingRepository.findByIdClientIdAndIdInstrumentId(CLIENT_ID, INSTR_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INSUFFICIENT_HOLDINGS");
    }

    // ─── executeTrade: LIMIT orders ──────────────────────────────────────────

    @Test
    void executeTrade_buyLimit_priceMetAtTarget_executes() {
        OrderRequest req = buyLimitOrder(5, new BigDecimal("205.0000")); // askPrice 200 ≤ target 205 → met

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        TradeResponse result = tradeService.executeTrade(req);

        assertThat(result.getExecutionPrice()).isEqualByComparingTo("200.0000");
    }

    @Test
    void executeTrade_buyLimit_priceAboveTarget_throws() {
        OrderRequest req = buyLimitOrder(5, new BigDecimal("195.0000")); // askPrice 200 > target 195 → not met

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(LimitOrderNotMetException.class)
                .hasMessage("LIMIT_NOT_MET");
    }

    @Test
    void executeTrade_sellLimit_priceBelowTarget_throws() {
        OrderRequest req = sellLimitOrder(5, new BigDecimal("205.0000")); // bidPrice 199.5 < target 205 → not met

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(LimitOrderNotMetException.class)
                .hasMessage("LIMIT_NOT_MET");
    }

    // ─── executeTrade: market state checks ──────────────────────────────────

    @Test
    void executeTrade_marketClosed_throws() {
        MarketStatus closed = new MarketStatus();
        closed.setMarketOpen(false);
        OrderRequest req = buyMarketOrder(1);

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(closed);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(MarketClosedException.class)
                .hasMessage("MARKET_CLOSED");
    }

    @Test
    void executeTrade_marketStatusNull_throws() {
        OrderRequest req = buyMarketOrder(1);

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(null);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(MarketClosedException.class);
    }

    @Test
    void executeTrade_stalePrice_throws() {
        freshPrice.setPriceTimestamp(Instant.now().minusSeconds(120).toString()); // 2 minutes old
        OrderRequest req = buyMarketOrder(1);

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(PriceDataStaleException.class)
                .hasMessage("PRICE_DATA_STALE");
    }

    @Test
    void executeTrade_instrumentNotFound_throws() {
        OrderRequest req = buyMarketOrder(1);

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("INSTRUMENT_NOT_FOUND");
    }

    // ─── executeTrade: quantity constraints ──────────────────────────────────

    @Test
    void executeTrade_qtyBelowMinimum_throws() {
        instrument.setMinQuantity(10);
        OrderRequest req = buyMarketOrder(5); // qty 5 < min 10

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ORDER_QTY_BELOW_MIN");
    }

    @Test
    void executeTrade_qtyAboveMaximum_throws() {
        instrument.setMaxQuantity(100);
        OrderRequest req = buyMarketOrder(200); // qty 200 > max 100

        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));
        when(mdsClient.getMarketStatus()).thenReturn(openMarket);
        when(mdsClient.getPrice(INSTR_ID)).thenReturn(freshPrice);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ORDER_QTY_ABOVE_MAX");
    }

    // ─── executeTrade: request validation ────────────────────────────────────

    @Test
    void executeTrade_missingInstrumentId_throws() {
        OrderRequest req = buyMarketOrder(1);
        req.setInstrumentId(null);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("MISSING_INSTRUMENT_ID");
    }

    @Test
    void executeTrade_zeroQuantity_throws() {
        OrderRequest req = buyMarketOrder(0);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_QUANTITY");
    }

    @Test
    void executeTrade_invalidDirection_throws() {
        OrderRequest req = buyMarketOrder(1);
        req.setDirection("X");

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_DIRECTION");
    }

    @Test
    void executeTrade_invalidOrderType_throws() {
        OrderRequest req = buyMarketOrder(1);
        req.setOrderType("STOP");

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_ORDER_TYPE");
    }

    @Test
    void executeTrade_limitOrderWithNullTargetPrice_throws() {
        OrderRequest req = buyMarketOrder(1);
        req.setOrderType("LIMIT");
        req.setTargetPrice(null);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_TARGET_PRICE");
    }

    @Test
    void executeTrade_limitOrderWithZeroTargetPrice_throws() {
        OrderRequest req = buyMarketOrder(1);
        req.setOrderType("LIMIT");
        req.setTargetPrice(BigDecimal.ZERO);

        assertThatThrownBy(() -> tradeService.executeTrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_TARGET_PRICE");
    }

    // ─── getTradeHistory ─────────────────────────────────────────────────────

    @Test
    void getTradeHistory_emptyHistory_returnsEmptyList() {
        when(tradeRepository.findByOrderClientClientId(CLIENT_ID)).thenReturn(List.of());

        TradeHistoryResponse result = tradeService.getTradeHistory(CLIENT_ID);

        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(result.getTrades()).isEmpty();
    }

    // ─── suggestBuy ──────────────────────────────────────────────────────────

    @Test
    void suggestBuy_advisorNotAccepted_throws() {
        ClientPreferences prefs = makePrefs(CLIENT_ID, (short) 3, false);

        assertThatThrownBy(() -> tradeService.suggestBuy(prefs))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("ADVISOR_NOT_ACCEPTED");
    }

    @Test
    void suggestBuy_acceptsAdvisor_returnsAtMostFiveResults() {
        ClientPreferences prefs = makePrefs(CLIENT_ID, (short) 3, true);

        List<Price> allPrices = buildPriceList();
        when(mdsClient.getAllPrices()).thenReturn(allPrices);
        when(mdsClient.getPriceHistory(anyString())).thenReturn(allPrices);
        when(instrumentRepository.findById(anyString())).thenReturn(Optional.of(instrument));

        List<Price> result = tradeService.suggestBuy(prefs);

        assertThat(result).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    void suggestBuy_conservativeInvestor_filtersOutStocksAndEtfs() {
        ClientPreferences prefs = makePrefs(CLIENT_ID, (short) 2, true); // risk ≤ 2 → no STOCK/ETF

        Price stockPrice = makePrice("AAPL", "STOCK");
        Price bondPrice  = makePrice("US10Y", "GOVT");

        Instrument stock = new Instrument(); stock.setInstrumentId("AAPL"); stock.setCategoryId("STOCK");
        Instrument bond  = new Instrument(); bond.setInstrumentId("US10Y"); bond.setCategoryId("GOVT");

        when(mdsClient.getAllPrices()).thenReturn(List.of(stockPrice, bondPrice));
        when(mdsClient.getPriceHistory(anyString())).thenReturn(List.of());
        when(instrumentRepository.findById("AAPL")).thenReturn(Optional.of(stock));
        when(instrumentRepository.findById("US10Y")).thenReturn(Optional.of(bond));

        List<Price> result = tradeService.suggestBuy(prefs);

        assertThat(result).noneMatch(p -> "AAPL".equals(p.getInstrumentId()));
        assertThat(result).anyMatch(p -> "US10Y".equals(p.getInstrumentId()));
    }

    // ─── suggestSell ─────────────────────────────────────────────────────────

    @Test
    void suggestSell_advisorNotAccepted_throws() {
        ClientPreferences prefs = makePrefs(CLIENT_ID, (short) 3, false);

        assertThatThrownBy(() -> tradeService.suggestSell(prefs))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("ADVISOR_NOT_ACCEPTED");
    }

    @Test
    void suggestSell_noHoldings_returnsEmptyList() {
        ClientPreferences prefs = makePrefs(CLIENT_ID, (short) 3, true);
        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of());

        List<Holding> result = tradeService.suggestSell(prefs);

        assertThat(result).isEmpty();
    }

    @Test
    void suggestSell_holdingWithDeepLoss_isIncluded() {
        ClientPreferences prefs = makePrefs(CLIENT_ID, (short) 2, true); // risk ≤ 2, threshold -5%

        // avgPrice 200, bidPrice 180 → loss = (180-200)/200 = -10% → exceeds -5% threshold
        Holding h = new Holding(CLIENT_ID, INSTR_ID, 10, new BigDecimal("200.0000"));
        Price p = makePrice(INSTR_ID, "STOCK");
        p.setBidPrice(new BigDecimal("180.0000"));

        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of(h));
        when(mdsClient.getAllPrices()).thenReturn(List.of(p));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));

        List<Holding> result = tradeService.suggestSell(prefs);

        assertThat(result).isNotEmpty();
    }

    @Test
    void suggestSell_profitableHolding_isExcluded() {
        ClientPreferences prefs = makePrefs(CLIENT_ID, (short) 3, true); // risk 3, threshold -10%

        // avgPrice 100, bidPrice 120 → gain → not a loss candidate
        Holding h = new Holding(CLIENT_ID, INSTR_ID, 5, new BigDecimal("100.0000"));
        Price p = makePrice(INSTR_ID, "STOCK");
        p.setBidPrice(new BigDecimal("120.0000"));

        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of(h));
        when(mdsClient.getAllPrices()).thenReturn(List.of(p));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));

        List<Holding> result = tradeService.suggestSell(prefs);

        assertThat(result).isEmpty();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private OrderRequest buyMarketOrder(int qty) {
        OrderRequest req = new OrderRequest();
        req.setInstrumentId(INSTR_ID);
        req.setQuantity(qty);
        req.setDirection("B");
        req.setOrderType("MARKET");
        req.setClientId(CLIENT_ID);
        return req;
    }

    private OrderRequest sellMarketOrder(int qty) {
        OrderRequest req = new OrderRequest();
        req.setInstrumentId(INSTR_ID);
        req.setQuantity(qty);
        req.setDirection("S");
        req.setOrderType("MARKET");
        req.setClientId(CLIENT_ID);
        return req;
    }

    private OrderRequest buyLimitOrder(int qty, BigDecimal targetPrice) {
        OrderRequest req = buyMarketOrder(qty);
        req.setOrderType("LIMIT");
        req.setTargetPrice(targetPrice);
        return req;
    }

    private OrderRequest sellLimitOrder(int qty, BigDecimal targetPrice) {
        OrderRequest req = sellMarketOrder(qty);
        req.setOrderType("LIMIT");
        req.setTargetPrice(targetPrice);
        return req;
    }

    private ClientPreferences makePrefs(String clientId, short riskTolerance, boolean acceptAdvisor) {
        ClientPreferences prefs = new ClientPreferences();
        prefs.setClientId(clientId);
        prefs.setRiskTolerance(riskTolerance);
        prefs.setAcceptAdvisor(acceptAdvisor);
        prefs.setInvestmentPurpose("Retirement");
        prefs.setIncomeCategory("MIG");
        prefs.setLengthOfInvestment("Long");
        prefs.setPercentageOfSpend("Tier2");
        return prefs;
    }

    private Price makePrice(String instrumentId, String category) {
        Price p = new Price();
        p.setInstrumentId(instrumentId);
        p.setAskPrice(new BigDecimal("200.0000"));
        p.setBidPrice(new BigDecimal("199.5000"));
        p.setLastPrice(new BigDecimal("199.7500"));
        p.setPriceTimestamp(Instant.now().toString());
        return p;
    }

    private List<Price> buildPriceList() {
        return List.of(
                makePrice("AAPL", "STOCK"),
                makePrice("MSFT", "STOCK"),
                makePrice("GOOGL", "STOCK"),
                makePrice("JPM", "STOCK"),
                makePrice("TSLA", "STOCK"),
                makePrice("SPY", "ETF"),
                makePrice("US10Y", "GOVT")
        );
    }
}
