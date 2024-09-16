package com.fidelity.clientportfolio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fidelity.trade.*;

import java.math.BigDecimal;
import java.util.List;


class TradeHistoryServiceTest {

    private TradeHistoryService tradeHistoryService;
    private Order order;
    private Trade trade;
    
	@BeforeEach
	void setUp() throws Exception {
		 tradeHistoryService = new TradeHistoryService();
	        // Setup initial data
	     order = new Order("inst1", 100, new BigDecimal("50"), "B", "client1", "order1",123);
	     trade = new Trade("inst1", 100, new BigDecimal("50"), "B", "client1", order, "trade1", new BigDecimal("5000"));
	     tradeHistoryService.addTrade(trade);
	}

	@AfterEach
	void tearDown() throws Exception {
		order = null;
		trade = null;
	}

	@Test
	public void testGetClientTradeHistory() {
        List<Trade> trades = tradeHistoryService.getClientTradeHistory("client1");
        assertNotNull(trades);
        assertFalse(trades.isEmpty());
        assertEquals(1, trades.size());
    }
	
	@Test
    public void testUpdateTradeSuccessfully() {
        // Initial trade details
		order = new Order("inst1", 100, new BigDecimal("50"), "B", "client1", "order1",123);
		Trade initialTrade = new Trade("inst1", 100, new BigDecimal("50"), "B", "client1", order, "trade1", new BigDecimal("5000"));
        tradeHistoryService.addTrade(initialTrade);

        // Updated trade details
        Order updatedOrder = new Order("inst1", 150, new BigDecimal("55"), "S", "client1", "order2", 456);
        Trade updatedTrade = new Trade("inst1", 150, new BigDecimal("55"), "S", "client1", updatedOrder, "trade1", new BigDecimal("8250"));

        // Update trade
        tradeHistoryService.updateTrade(updatedTrade);

        // Retrieve and verify updated trade
        Trade trade = tradeHistoryService.getClientTradeHistory("client1").get(0);
        assertNotNull(trade, "Trade should not be null after update.");
        assertEquals("inst1", trade.getInstrumentId(), "Instrument ID should match.");
        assertEquals(150, trade.getQuantity(), "Quantity should be updated.");
        assertEquals(new BigDecimal("55"), trade.getExecutionPrice(), "Execution Price should be updated.");
        assertEquals("S", trade.getDirection(), "Direction should be updated.");
        assertEquals(new BigDecimal("8250"), trade.getCashValue(), "Cash Value should be updated.");
    }
	
	 @Test
	  public void testGetClientTradeHistoryThrowsNullPointerException() {        
		 Exception e = assertThrows(NullPointerException.class, () -> tradeHistoryService.getClientTradeHistory(null));
	     assertEquals(e.getMessage(),"Client ID must not be null");
	}
	 
	 @Test
	    public void testGetClientTradeHistoryThrowsExceptionForNoTrades() {
		 Exception e =assertThrows(RuntimeException.class, () -> tradeHistoryService.getClientTradeHistory("nonexistentClient"));
		 assertEquals(e.getMessage(),"No trades found for client ID");   
	 }
	 
	 @Test
	 public void testAddTradeThrowsExceptionForNullTrade() {
		 Exception e = assertThrows(NullPointerException.class, () -> tradeHistoryService.addTrade(null));
	      assertEquals(e.getMessage(),"Trade must not be null");
	 }

	 @Test
	 public void testUpdateTradeThrowsExceptionForNullTrade() {
		 Exception e = assertThrows(NullPointerException.class, () -> tradeHistoryService.updateTrade(null));
		 assertEquals(e.getMessage(),"Updated trade must not be null");
	 }

	 @Test
	 public void testUpdateTradeThrowsExceptionForNonExistentTrade() {
		 Trade updatedTrade = new Trade("inst2", 200, new BigDecimal("60"), "S", "client1", 
				 				new Order("inst2", 200, new BigDecimal("60"), "S", "client1", "order2", 456), "nonexistentTradeId", new BigDecimal("12000"));
	    assertThrows(RuntimeException.class, () -> tradeHistoryService.updateTrade(updatedTrade));
    }

}
