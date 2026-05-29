package com.marshals.service;

import com.marshals.client.MdsClient;
import com.marshals.dto.Price;
import com.marshals.dto.TradePL;
import com.marshals.dto.TradeHistoryResponse;
import com.marshals.model.Client;
import com.marshals.model.ClientOrder;
import com.marshals.model.ClientTrade;
import com.marshals.model.Holding;
import com.marshals.model.Instrument;
import com.marshals.repository.ClientTradeRepository;
import com.marshals.repository.HoldingRepository;
import com.marshals.repository.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityReportServiceTest {

    @Mock HoldingRepository holdingRepository;
    @Mock ClientTradeRepository tradeRepository;
    @Mock InstrumentRepository instrumentRepository;
    @Mock MdsClient mdsClient;
    @InjectMocks ActivityReportService activityReportService;

    static final String CLIENT_ID = "541107416";
    static final String INSTR_ID  = "AAPL";

    Instrument instrument;

    @BeforeEach
    void setUp() {
        instrument = new Instrument();
        instrument.setInstrumentId(INSTR_ID);
        instrument.setCategoryId("STOCK");
        instrument.setDescription("Apple Inc.");
    }

    // ─── getHoldings ──────────────────────────────────────────────────────────

    @Test
    void getHoldings_enrichesHoldingsWithInstrumentData() {
        Holding holding = new Holding(CLIENT_ID, INSTR_ID, 10, new BigDecimal("180.0000"));
        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));

        List<Holding> result = activityReportService.getHoldings(CLIENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInstrumentDescription()).isEqualTo("Apple Inc.");
        assertThat(result.get(0).getCategoryId()).isEqualTo("STOCK");
    }

    @Test
    void getHoldings_noHoldings_returnsEmptyList() {
        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of());

        List<Holding> result = activityReportService.getHoldings(CLIENT_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getHoldings_instrumentNotFound_holdingStillReturnedWithoutEnrichment() {
        Holding holding = new Holding(CLIENT_ID, INSTR_ID, 5, new BigDecimal("100.0000"));
        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.empty());

        List<Holding> result = activityReportService.getHoldings(CLIENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInstrumentDescription()).isNull();
    }

    // ─── getTradeHistory ──────────────────────────────────────────────────────

    @Test
    void getTradeHistory_returnsSortedByExecutionTime() {
        Client client = buildClient();
        ClientTrade older = buildTrade("t1", "B", OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC), client);
        ClientTrade newer = buildTrade("t2", "S", OffsetDateTime.of(2024, 2, 1, 10, 0, 0, 0, ZoneOffset.UTC), client);

        when(tradeRepository.findByOrderClientClientId(CLIENT_ID)).thenReturn(List.of(newer, older));

        TradeHistoryResponse result = activityReportService.getTradeHistory(CLIENT_ID);

        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(result.getTrades()).hasSize(2);
        assertThat(result.getTrades().get(0).getTradeId()).isEqualTo("t1");
        assertThat(result.getTrades().get(1).getTradeId()).isEqualTo("t2");
    }

    @Test
    void getTradeHistory_noTrades_returnsEmptyList() {
        when(tradeRepository.findByOrderClientClientId(CLIENT_ID)).thenReturn(List.of());

        TradeHistoryResponse result = activityReportService.getTradeHistory(CLIENT_ID);

        assertThat(result.getTrades()).isEmpty();
    }

    // ─── getPLReport ──────────────────────────────────────────────────────────

    @Test
    void getPLReport_noTrades_returnsOnlyUnrealizedFromHoldings() {
        // Holding: qty=10, avgPrice=180, bidPrice=190 → unrealized = (190-180) × 10 = 100
        Holding holding = new Holding(CLIENT_ID, INSTR_ID, 10, new BigDecimal("180.0000"));
        Price livePrice = buildLivePrice(INSTR_ID, "190.0000");

        when(tradeRepository.findByOrderClientClientId(CLIENT_ID)).thenReturn(List.of());
        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of(holding));
        when(mdsClient.getAllPrices()).thenReturn(List.of(livePrice));
        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));

        List<TradePL> result = activityReportService.getPLReport(CLIENT_ID);

        assertThat(result).hasSize(1);
        TradePL pl = result.get(0);
        assertThat(pl.getRealizedPL()).isEqualByComparingTo("0");
        assertThat(pl.getUnrealizedPL()).isEqualByComparingTo("100.0000");
        assertThat(pl.getTotalPL()).isEqualByComparingTo("100.0000");
    }

    @Test
    void getPLReport_buyThenSell_realizedPLCalculatedCorrectly() {
        // BUY 10 @ cashValue 2002 → avgPrice = 200.2
        // SELL 10 @ cashValue 1990 → realizedPL = 1990 - (200.2 × 10) = 1990 - 2002 = -12
        Client client = buildClient();
        ClientTrade buy  = buildTrade("t1", "B", OffsetDateTime.now().minusDays(2), client);
        buy.setCashValue(new BigDecimal("2002.0000"));
        buy.getOrder().setQuantity(10);

        ClientTrade sell = buildTrade("t2", "S", OffsetDateTime.now().minusDays(1), client);
        sell.setCashValue(new BigDecimal("1990.0000"));
        sell.getOrder().setQuantity(10);

        when(tradeRepository.findByOrderClientClientId(CLIENT_ID)).thenReturn(List.of(buy, sell));
        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of());
        when(mdsClient.getAllPrices()).thenReturn(List.of());

        List<TradePL> result = activityReportService.getPLReport(CLIENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRealizedPL()).isEqualByComparingTo("-12.0000");
        assertThat(result.get(0).getUnrealizedPL()).isEqualByComparingTo("0");
    }

    @Test
    void getPLReport_openPosition_hasBothRealizedAndUnrealized() {
        // BUY 20 @ cashValue 4004 → avgPrice = 200.2
        // SELL 10 @ cashValue 1990 → realizedPL = 1990 - (200.2 × 10) = -12
        // remaining: qty=10, avgPrice=200.2, bidPrice=210 → unrealized = (210-200.2) × 10 = 98
        Client client = buildClient();
        ClientTrade buy = buildTrade("t1", "B", OffsetDateTime.now().minusDays(3), client);
        buy.setCashValue(new BigDecimal("4004.0000"));
        buy.getOrder().setQuantity(20);

        ClientTrade sell = buildTrade("t2", "S", OffsetDateTime.now().minusDays(1), client);
        sell.setCashValue(new BigDecimal("1990.0000"));
        sell.getOrder().setQuantity(10);

        Holding remaining = new Holding(CLIENT_ID, INSTR_ID, 10, new BigDecimal("200.2000"));
        Price livePrice = buildLivePrice(INSTR_ID, "210.0000");

        when(tradeRepository.findByOrderClientClientId(CLIENT_ID)).thenReturn(List.of(buy, sell));
        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of(remaining));
        when(mdsClient.getAllPrices()).thenReturn(List.of(livePrice));
        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));

        List<TradePL> result = activityReportService.getPLReport(CLIENT_ID);

        assertThat(result).hasSize(1);
        TradePL pl = result.get(0);
        assertThat(pl.getRealizedPL()).isEqualByComparingTo("-12.0000");
        assertThat(pl.getUnrealizedPL()).isEqualByComparingTo("98.0000");
        assertThat(pl.getTotalPL()).isEqualByComparingTo("86.0000");
    }

    @Test
    void getPLReport_noTradesNoHoldings_returnsEmpty() {
        when(tradeRepository.findByOrderClientClientId(CLIENT_ID)).thenReturn(List.of());
        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of());
        when(mdsClient.getAllPrices()).thenReturn(List.of());

        List<TradePL> result = activityReportService.getPLReport(CLIENT_ID);

        assertThat(result).isEmpty();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Client buildClient() {
        Client c = new Client();
        c.setClientId(CLIENT_ID);
        c.setCurrBalance(new BigDecimal("10000.0000"));
        return c;
    }

    private ClientTrade buildTrade(String tradeId, String direction, OffsetDateTime executedAt, Client client) {
        Instrument instr = new Instrument();
        instr.setInstrumentId(INSTR_ID);
        instr.setCategoryId("STOCK");
        instr.setDescription("Apple Inc.");

        ClientOrder order = new ClientOrder();
        order.setOrderId("order-" + tradeId);
        order.setInstrument(instr);
        order.setClient(client);
        order.setDirection(direction);
        order.setOrderType("MARKET");
        order.setQuantity(5);
        order.setToken("");

        ClientTrade trade = new ClientTrade();
        trade.setTradeId(tradeId);
        trade.setOrder(order);
        trade.setExecutionPrice(new BigDecimal("200.0000"));
        trade.setCashValue(new BigDecimal("1002.5000"));
        trade.setExecutedAt(executedAt);
        return trade;
    }

    private Price buildLivePrice(String instrumentId, String bidPrice) {
        Price p = new Price();
        p.setInstrumentId(instrumentId);
        p.setBidPrice(new BigDecimal(bidPrice));
        p.setAskPrice(new BigDecimal(bidPrice));
        p.setLastPrice(new BigDecimal(bidPrice));
        p.setPriceTimestamp(Instant.now().toString());
        return p;
    }
}
