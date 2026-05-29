package com.marshals.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marshals.client.MdsClient;
import com.marshals.dto.LoginRequest;
import com.marshals.dto.MarketStatus;
import com.marshals.dto.OrderRequest;
import com.marshals.dto.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the trade execution flow.
 * Requires Docker PostgreSQL on port 5433 (docker-compose up in db-trade-nexus/).
 * MdsClient is mocked — does not require MDS to be running.
 * Run with: ./mvnw verify
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "jwt.secret=integration_test_jwt_secret_must_be_at_least_32_chars",
                "spring.datasource.url=jdbc:postgresql://localhost:5433/tradenexus?TimeZone=UTC&stringtype=unspecified",
                "spring.datasource.username=tn_user",
                "spring.datasource.password=TradeNexus_local_2024",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.flyway.enabled=false",
                "mds.url=http://localhost:3001"
        }
)
@AutoConfigureMockMvc
class TradeControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @MockBean  MdsClient mdsClient;

    String token;

    @BeforeEach
    void obtainToken() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("himanshu@gmail.com");
        loginReq.setPassword("Marsh2024");

        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginReq)))
                .andReturn().getResponse().getContentAsString();

        token = mapper.readTree(body).get("token").asText();
    }

    // ─── live-prices ─────────────────────────────────────────────────────────

    @Test
    void getLivePrices_withValidToken_returns200() throws Exception {
        when(mdsClient.getAllPrices()).thenReturn(List.of(freshPrice("AAPL")));

        mockMvc.perform(get("/trade/live-prices")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getLivePrices_withoutToken_returns403() throws Exception {
        // Spring Security returns 403 for anonymous access (no AuthenticationEntryPoint configured)
        mockMvc.perform(get("/trade/live-prices"))
                .andExpect(status().isForbidden());
    }

    // ─── execute-trade ────────────────────────────────────────────────────────

    @Test
    @Transactional
    void executeTrade_validBuyMarketOrder_returns200WithTradeResponse() throws Exception {
        MarketStatus open = new MarketStatus();
        open.setMarketOpen(true);
        Price price = freshPrice("AAPL");

        when(mdsClient.getMarketStatus()).thenReturn(open);
        when(mdsClient.getPrice("AAPL")).thenReturn(price);

        OrderRequest req = new OrderRequest();
        req.setInstrumentId("AAPL");
        req.setQuantity(1);
        req.setDirection("B");
        req.setOrderType("MARKET");
        req.setClientId("541107416");

        mockMvc.perform(post("/trade/execute-trade")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instrumentId").value("AAPL"))
                .andExpect(jsonPath("$.direction").value("B"))
                .andExpect(jsonPath("$.cashValue").isNumber());
    }

    @Test
    void executeTrade_marketClosed_returns409() throws Exception {
        // MarketClosedException maps to 409 in GlobalExceptionHandler
        MarketStatus closed = new MarketStatus();
        closed.setMarketOpen(false);

        when(mdsClient.getMarketStatus()).thenReturn(closed);

        OrderRequest req = new OrderRequest();
        req.setInstrumentId("AAPL");
        req.setQuantity(1);
        req.setDirection("B");
        req.setOrderType("MARKET");
        req.setClientId("541107416");

        mockMvc.perform(post("/trade/execute-trade")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("MARKET_CLOSED"));
    }

    @Test
    void executeTrade_nonAdminUserAccessingOtherClientData_returns403() throws Exception {
        // Login as John Doe (non-admin, client 739982664)
        LoginRequest johnLogin = new LoginRequest();
        johnLogin.setEmail("john.doe@gmail.com");
        johnLogin.setPassword("Marsh2024");

        String johnBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(johnLogin)))
                .andReturn().getResponse().getContentAsString();
        String johnToken = mapper.readTree(johnBody).get("token").asText();

        // Try to execute a trade for himanshu's account using John's token
        OrderRequest req = new OrderRequest();
        req.setInstrumentId("AAPL");
        req.setQuantity(1);
        req.setDirection("B");
        req.setOrderType("MARKET");
        req.setClientId("541107416"); // himanshu's clientId — John is not authorized

        mockMvc.perform(post("/trade/execute-trade")
                        .header("Authorization", "Bearer " + johnToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ─── activity report endpoints ────────────────────────────────────────────

    @Test
    void getHoldings_ownData_returns200() throws Exception {
        mockMvc.perform(get("/activity-report/holdings/541107416")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getTradeHistory_ownData_returns200() throws Exception {
        mockMvc.perform(get("/activity-report/trades/541107416")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("541107416"));
    }

    @Test
    void getPL_ownData_returns200() throws Exception {
        when(mdsClient.getAllPrices()).thenReturn(List.of());

        mockMvc.perform(get("/activity-report/pl/541107416")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ─── client preferences ──────────────────────────────────────────────────

    @Test
    void getPreferences_ownData_returns200() throws Exception {
        mockMvc.perform(get("/client-preferences/541107416")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("541107416"));
    }

    // ─── instruments ─────────────────────────────────────────────────────────

    @Test
    void getAllInstruments_withValidToken_returns200With12Instruments() throws Exception {
        mockMvc.perform(get("/instrument")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Price freshPrice(String instrumentId) {
        Price p = new Price();
        p.setInstrumentId(instrumentId);
        p.setAskPrice(new BigDecimal("200.0000"));
        p.setBidPrice(new BigDecimal("199.5000"));
        p.setLastPrice(new BigDecimal("199.7500"));
        p.setPriceTimestamp(Instant.now().toString());
        return p;
    }
}
