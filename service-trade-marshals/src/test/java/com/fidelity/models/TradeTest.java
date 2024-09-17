package com.fidelity.models;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TradeTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		//fail("Not yet implemented");
	}

    @Test
    void testValidTradeCreation() {
        Order order = new Order("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", "order1", 123);
        Trade trade = new Trade("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", order, "trade1", new BigDecimal("1000.00"));

        assertEquals("instrument1", trade.getInstrumentId());
        assertEquals(10, trade.getQuantity());
        assertEquals(new BigDecimal("100.00"), trade.getExecutionPrice());
        assertEquals("BUY", trade.getDirection());
        assertEquals("client1", trade.getClientId());
        assertEquals(order, trade.getOrder());
        assertEquals("trade1", trade.getTradeId());
        assertEquals(new BigDecimal("1000.00"), trade.getCashValue());
    }

    @Test
    void testInstrumentIdCannotBeNull() {
        Order order = new Order("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", "order1", 123);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Trade(null, 10, new BigDecimal("100.00"), "BUY", "client1", order, "trade1", new BigDecimal("1000.00")));
        assertEquals("instrumentId cannot be null or empty", exception.getMessage());
    }

    @Test
    void testQuantityMustBePositive() {
        Order order = new Order("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", "order1", 123);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Trade("instrument1", 0, new BigDecimal("100.00"), "BUY", "client1", order, "trade1", new BigDecimal("1000.00")));
        assertEquals("quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void testExecutionPriceMustBePositive() {
        Order order = new Order("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", "order1", 123);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Trade("instrument1", 10, new BigDecimal("-1.00"), "BUY", "client1", order, "trade1", new BigDecimal("1000.00")));
        assertEquals("executionPrice must be greater than 0", exception.getMessage());
    }

    @Test
    void testDirectionCannotBeNull() {
        Order order = new Order("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", "order1", 123);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Trade("instrument1", 10, new BigDecimal("100.00"), null, "client1", order, "trade1", new BigDecimal("1000.00")));
        assertEquals("direction cannot be null or empty", exception.getMessage());
    }

    @Test
    void testClientIdCannotBeEmpty() {
        Order order = new Order("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", "order1", 123);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Trade("instrument1", 10, new BigDecimal("100.00"), "BUY", "", order, "trade1", new BigDecimal("1000.00")));
        assertEquals("clientId cannot be null or empty", exception.getMessage());
    }

    @Test
    void testTradeIdCannotBeEmpty() {
        Order order = new Order("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", "order1", 123);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Trade("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", order, "", new BigDecimal("1000.00")));
        assertEquals("tradeId cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCashValueCannotBeNegative() {
        Order order = new Order("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", "order1", 123);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Trade("instrument1", 10, new BigDecimal("100.00"), "BUY", "client1", order, "trade1", new BigDecimal("-1000.00")));
        assertEquals("cashValue cannot be null and must be non-negative", exception.getMessage());
    }
}
