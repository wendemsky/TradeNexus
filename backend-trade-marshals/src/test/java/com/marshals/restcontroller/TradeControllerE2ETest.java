package com.marshals.restcontroller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;

import com.marshals.business.ClientPreferences;
import com.marshals.business.Order;
import com.marshals.business.Price;
import com.marshals.business.Trade;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Transactional
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"},
     executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class TradeControllerE2ETest {
    @Autowired
    private TestRestTemplate restTemplate; // for executing rest endpoints

    @Autowired
    private JdbcTemplate jdbcTemplate;  // for executing SQL queries

    @Test
    void testExecuteTrade() {
		Order order = new Order("N123456", 10, new BigDecimal("104.75"), "B", "541107416", "ABC123", 540983960);

        ResponseEntity<Trade> response = restTemplate.postForEntity("/trade/execute-trade", order, Trade.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Trade trade = response.getBody();
        assertNotNull(trade);
        assertEquals(order.getQuantity(), trade.getQuantity());
        assertEquals(order.getInstrumentId(), trade.getInstrumentId());
    }
    
    @Test
    void testExecuteTrade_NullOrder() {
        ResponseEntity<Trade> response = restTemplate.postForEntity("/trade/execute-trade", null, Trade.class);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
    }

    @Test
    void testExecuteTrade_InvalidDirection() {
    	String invalidDirection = "X";
		Order order = new Order("N123456", 10, new BigDecimal("104.75"), invalidDirection, "541107416", "ABC123", 540983960);


        ResponseEntity<Trade> response = restTemplate.postForEntity("/trade/execute-trade", order, Trade.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testExecuteTrade_InsufficientBalance() {
		Order order = new Order("N123456", 1000, new BigDecimal("1040000.75"), "B", "541107416", "ABC123", 540983960);

        ResponseEntity<Trade> response = restTemplate.postForEntity("/trade/execute-trade", order, Trade.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testExecuteTrade_InsufficientQuantityToSell() {
		Order order = new Order("N123456", 1000, new BigDecimal("104.75"), "S", "541107416", "ABC123", 540983960);

        ResponseEntity<Trade> response = restTemplate.postForEntity("/trade/execute-trade", order, Trade.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testExecuteTrade_InstrumentNotInHoldings() {
		Order order = new Order("NonExistingInstrument", 10, new BigDecimal("104.75"), "S", "541107416", "ABC123", 540983960);


        ResponseEntity<Trade> response = restTemplate.postForEntity("/trade/execute-trade", order, Trade.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testExecuteTrade_NullTradeReturnedFromFMTS() {
		Order order = new Order("N123456", 10, new BigDecimal("1000.75"), "B", "541107416", "ABC123", 540983960);

        ResponseEntity<Trade> response = restTemplate.postForEntity("/trade/execute-trade", order, Trade.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
