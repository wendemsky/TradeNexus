package com.marshals.service;

import com.marshals.dto.ClientPortfolioResponse;
import com.marshals.model.Client;
import com.marshals.model.Holding;
import com.marshals.model.Instrument;
import com.marshals.repository.ClientRepository;
import com.marshals.repository.HoldingRepository;
import com.marshals.repository.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock HoldingRepository holdingRepository;
    @Mock ClientRepository clientRepository;
    @Mock InstrumentRepository instrumentRepository;
    @InjectMocks PortfolioService portfolioService;

    static final String CLIENT_ID = "541107416";
    static final String INSTR_ID  = "AAPL";

    Client client;
    Instrument instrument;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId(CLIENT_ID);
        client.setCurrBalance(new BigDecimal("10000.0000"));

        instrument = new Instrument();
        instrument.setInstrumentId(INSTR_ID);
        instrument.setCategoryId("STOCK");
        instrument.setDescription("Apple Inc.");
    }

    // ─── getPortfolio ─────────────────────────────────────────────────────────

    @Test
    void getPortfolio_clientWithHoldings_enrichesHoldingsAndReturnsBalance() {
        Holding holding = new Holding(CLIENT_ID, INSTR_ID, 10, new BigDecimal("180.0000"));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of(holding));
        when(instrumentRepository.findById(INSTR_ID)).thenReturn(Optional.of(instrument));

        ClientPortfolioResponse response = portfolioService.getPortfolio(CLIENT_ID);

        assertThat(response.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(response.getCurrBalance()).isEqualByComparingTo("10000.0000");
        assertThat(response.getHoldings()).hasSize(1);
        assertThat(response.getHoldings().get(0).getInstrumentDescription()).isEqualTo("Apple Inc.");
        assertThat(response.getHoldings().get(0).getCategoryId()).isEqualTo("STOCK");
    }

    @Test
    void getPortfolio_clientNotFound_throws() {
        when(clientRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.getPortfolio("unknown"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("CLIENT_NOT_FOUND");
    }

    @Test
    void getPortfolio_noHoldings_returnsEmptyList() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(holdingRepository.findByIdClientId(CLIENT_ID)).thenReturn(List.of());

        ClientPortfolioResponse response = portfolioService.getPortfolio(CLIENT_ID);

        assertThat(response.getHoldings()).isEmpty();
    }

    // ─── applyBuy ─────────────────────────────────────────────────────────────

    @Test
    void applyBuy_noExistingHolding_createsNewHoldingWithCostBasis() {
        // cashValue = 10 × 200 × 1.001 = 2002 → avgPrice = 2002 / 10 = 200.2000
        BigDecimal cashValue = new BigDecimal("2002.0000");

        when(holdingRepository.findByIdClientIdAndIdInstrumentId(CLIENT_ID, INSTR_ID))
                .thenReturn(Optional.empty());
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        portfolioService.applyBuy(CLIENT_ID, INSTR_ID, 10, cashValue);

        verify(holdingRepository).save(argThat(h ->
                h.getQuantity() == 10 &&
                h.getAvgPrice().compareTo(new BigDecimal("200.2000")) == 0
        ));
        verify(clientRepository).save(argThat(c ->
                c.getCurrBalance().compareTo(new BigDecimal("7998.0000")) == 0
        ));
    }

    @Test
    void applyBuy_existingHolding_updatesWeightedAverageCostBasis() {
        // existing: qty=10, avgPrice=180 → cost = 1800
        // buying:   qty=5,  cashValue=1010 (5 × 202)
        // new avg:  (1800 + 1010) / 15 = 2810 / 15 = 187.3333
        Holding existing = new Holding(CLIENT_ID, INSTR_ID, 10, new BigDecimal("180.0000"));
        BigDecimal cashValue = new BigDecimal("1010.0000");

        when(holdingRepository.findByIdClientIdAndIdInstrumentId(CLIENT_ID, INSTR_ID))
                .thenReturn(Optional.of(existing));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        portfolioService.applyBuy(CLIENT_ID, INSTR_ID, 5, cashValue);

        verify(holdingRepository).save(argThat(h ->
                h.getQuantity() == 15 &&
                h.getAvgPrice().compareTo(new BigDecimal("187.3333")) == 0
        ));
        verify(clientRepository).save(argThat(c ->
                c.getCurrBalance().compareTo(new BigDecimal("8990.0000")) == 0
        ));
    }

    @Test
    void applyBuy_deductsExactCashValueFromBalance() {
        BigDecimal cashValue = new BigDecimal("500.0000");
        when(holdingRepository.findByIdClientIdAndIdInstrumentId(CLIENT_ID, INSTR_ID))
                .thenReturn(Optional.empty());
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        portfolioService.applyBuy(CLIENT_ID, INSTR_ID, 2, cashValue);

        verify(clientRepository).save(argThat(c ->
                c.getCurrBalance().compareTo(new BigDecimal("9500.0000")) == 0
        ));
    }

    // ─── applySell ────────────────────────────────────────────────────────────

    @Test
    void applySell_partialSell_reducesQuantity() {
        Holding holding = new Holding(CLIENT_ID, INSTR_ID, 10, new BigDecimal("180.0000"));
        BigDecimal cashValue = new BigDecimal("996.5025"); // 5 × 199.5 × 0.999

        when(holdingRepository.findByIdClientIdAndIdInstrumentId(CLIENT_ID, INSTR_ID))
                .thenReturn(Optional.of(holding));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        portfolioService.applySell(CLIENT_ID, INSTR_ID, 5, cashValue);

        verify(holdingRepository).save(argThat(h -> h.getQuantity() == 5));
        verify(clientRepository).save(argThat(c ->
                c.getCurrBalance().compareTo(new BigDecimal("10996.5025")) == 0
        ));
    }

    @Test
    void applySell_fullSell_deletesHolding() {
        Holding holding = new Holding(CLIENT_ID, INSTR_ID, 5, new BigDecimal("180.0000"));
        BigDecimal cashValue = new BigDecimal("996.5025");

        when(holdingRepository.findByIdClientIdAndIdInstrumentId(CLIENT_ID, INSTR_ID))
                .thenReturn(Optional.of(holding));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        portfolioService.applySell(CLIENT_ID, INSTR_ID, 5, cashValue);

        verify(holdingRepository).delete(holding);
        verify(holdingRepository, never()).save(any());
    }

    @Test
    void applySell_holdingNotFound_throws() {
        when(holdingRepository.findByIdClientIdAndIdInstrumentId(CLIENT_ID, INSTR_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.applySell(CLIENT_ID, INSTR_ID, 5,
                new BigDecimal("996.5025")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INSUFFICIENT_HOLDINGS");
    }

    @Test
    void applySell_addsExactCashValueToBalance() {
        Holding holding = new Holding(CLIENT_ID, INSTR_ID, 10, new BigDecimal("180.0000"));
        BigDecimal cashValue = new BigDecimal("1500.0000");

        when(holdingRepository.findByIdClientIdAndIdInstrumentId(CLIENT_ID, INSTR_ID))
                .thenReturn(Optional.of(holding));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        portfolioService.applySell(CLIENT_ID, INSTR_ID, 5, cashValue);

        verify(clientRepository).save(argThat(c ->
                c.getCurrBalance().compareTo(new BigDecimal("11500.0000")) == 0
        ));
    }
}
