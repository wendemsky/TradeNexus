package com.marshals.restcontroller;

import com.marshals.business.Holding;
import com.marshals.business.Trade;
import com.marshals.business.TradeHistory;
import com.marshals.business.services.ActivityReportService;
import com.marshals.integration.DatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class ActivityReportControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ActivityReportController activityReportController;

    @MockBean
    private ActivityReportService activityReportService;

    @MockBean
    private Logger logger;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(activityReportController).build();
    }

    @Test
    void testGenerateHoldingsReport_Success() throws Exception {
        String clientId = "test-client";
        List<Holding> holdings = Collections.singletonList(new Holding());
        
        when(activityReportService.generateHoldingsReport(clientId)).thenReturn(holdings);
        
        ResponseEntity<List<Holding>> response = activityReportController.generateHoldingsReport(clientId);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(holdings, response.getBody());
    }

    @Test
    void testGenerateHoldingsReport_NoContent() throws Exception {
        String clientId = "test-client";
        
        when(activityReportService.generateHoldingsReport(clientId)).thenReturn(Collections.emptyList());
        
        ResponseEntity<List<Holding>> response = activityReportController.generateHoldingsReport(clientId);
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGenerateHoldingsReport_DatabaseException() throws Exception {
        String clientId = "test-client";
        
        when(activityReportService.generateHoldingsReport(clientId)).thenThrow(new DatabaseException("Database error"));
        
        ResponseEntity<List<Holding>> response = activityReportController.generateHoldingsReport(clientId);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGenerateTradeReport_Success() throws Exception {
        String clientId = "test-client";
        TradeHistory tradeHistory = new TradeHistory();
        tradeHistory.setTrades(Collections.singletonList(new Trade()));
        
        when(activityReportService.generateTradeReport(clientId)).thenReturn(tradeHistory);
        
        ResponseEntity<TradeHistory> response = activityReportController.generateTradeReport(clientId);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tradeHistory, response.getBody());
    }

    @Test
    void testGenerateTradeReport_NoContent() throws Exception {
        String clientId = "test-client";
        TradeHistory tradeHistory = new TradeHistory();
        tradeHistory.setTrades(Collections.emptyList());
        
        when(activityReportService.generateTradeReport(clientId)).thenReturn(tradeHistory);
        
        ResponseEntity<TradeHistory> response = activityReportController.generateTradeReport(clientId);
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGenerateTradeReport_DatabaseException() throws Exception {
        String clientId = "test-client";
        
        when(activityReportService.generateTradeReport(clientId)).thenThrow(new DatabaseException("Database error"));
        
        ResponseEntity<TradeHistory> response = activityReportController.generateTradeReport(clientId);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGeneratePLReport_Success() throws Exception {
        String clientId = "test-client";
        Map<String, BigDecimal> profitLossMap = Map.of("Profit", BigDecimal.valueOf(100));
        
        when(activityReportService.generatePLReport(clientId)).thenReturn(profitLossMap);
        
        ResponseEntity<Map<String, BigDecimal>> response = activityReportController.generatePLReport(clientId);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profitLossMap, response.getBody());
    }

    @Test
    void testGeneratePLReport_NoContent() throws Exception {
        String clientId = "test-client";
        
        when(activityReportService.generatePLReport(clientId)).thenReturn(Collections.emptyMap());
        
        ResponseEntity<Map<String, BigDecimal>> response = activityReportController.generatePLReport(clientId);
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGeneratePLReport_DatabaseException() throws Exception {
        String clientId = "test-client";
        
        when(activityReportService.generatePLReport(clientId)).thenThrow(new DatabaseException("Database error"));
        
        ResponseEntity<Map<String, BigDecimal>> response = activityReportController.generatePLReport(clientId);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
