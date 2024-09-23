package com.fidelity.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fidelity.integration.ClientDao;
import com.fidelity.integration.ClientTradeDao;
import com.fidelity.integration.ClientTradeDaoImpl;
import com.fidelity.integration.DatabaseException;
import com.fidelity.models.ClientPreferences;
//Importing models
import com.fidelity.models.Order;
import com.fidelity.models.Trade;
import com.fidelity.models.TradeHistory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


class TradeHistoryServiceTest {
	
	@Mock ClientTradeDao mockDao;
	@InjectMocks TradeHistoryService service;

    private TradeHistoryService tradeHistoryService;
    private Order order;
    private Trade trade;
    
	@BeforeEach
	void setUp() throws Exception {
		 tradeHistoryService = new TradeHistoryService(mockDao); 
	     MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void tearDown() throws Exception {
		order = null;
		trade = null;
	}

	@Test
	public void testGetClientTradeHistory() {
		String clientId = "1654658069";
        List<Trade> expected = new ArrayList<>();
        Order order1 = new Order("instrument1", 10, new BigDecimal("100.00"), "B", clientId, "ORDER001", 123);
    	Order order2 = new Order("instrument2", 15, new BigDecimal("120.00"), "B", clientId, "ORDER002", 123);
        Trade trade1 = new Trade(order1,new BigDecimal("100.00"),"TRADE001", new BigDecimal("1000.00"));
    	Trade trade2 = new Trade(order2,new BigDecimal("110.00"),"TRADE002", new BigDecimal("1000.00"));
    	expected.add(trade1);
    	expected.add(trade2);
        TradeHistory tradeHistory = new TradeHistory(clientId, expected);
        Mockito.when(mockDao.getClientTradeHistory(clientId))
			.thenReturn(tradeHistory);
		List<Trade> actual =  service.getClientTradeHistory(clientId);
		//Verifying that the corresponding mockDao methods were called
		Mockito.verify(mockDao).getClientTradeHistory(clientId); 
			assertEquals(actual.equals(tradeHistory.getTrades()), true);
    }
	
	
	 @Test
	  public void testGetClientTradeHistoryThrowsNullPointerException() {        
		 Exception e = assertThrows(NullPointerException.class, () -> tradeHistoryService.getClientTradeHistory(null));
	     assertEquals(e.getMessage(),"Client ID must not be null");
	}
	 
	 @Test
	    public void testGetClientTradeHistoryThrowsExceptionForNonExistingClient() {
		 String clientId = "nonexistentClient";
		 Mockito.doThrow(new DatabaseException("No trades found for client ID")).when(mockDao).getClientTradeHistory(clientId);
		 Exception e = assertThrows(DatabaseException.class, () -> {
				service.getClientTradeHistory(clientId);
			});
		 assertEquals(e.getMessage(),"No trades found for client ID");   
	 }
	 
	 @Test
	 public void testAddTrades() {
		 String clientId = "1654658069";
		 Order order1 = new Order("instrument1", 10, new BigDecimal("100.00"), "B", clientId, "ORDER001", 123);
		 Trade trade1 = new Trade(order1,new BigDecimal("100.00"),"TRADE001", new BigDecimal("1000.00"));
		 service.addTrade(trade1);
		 Mockito.verify(mockDao).addTrade(trade1);
		 
	 }
	 
	 
	 @Test
	 public void testAddTradeThrowsExceptionForNullTrade() {
		 Trade trade = null;
		 Exception e = assertThrows(NullPointerException.class, () -> {
				service.addTrade(trade);
			});
		 assertEquals(e.getMessage(), "Trade must not be null");
		 
	 }
	 
	 @Test
	 public void testGetTradesByClientIdShouldNotReturnMoreThan100Trades() {
		 String clientId = "1654658069";
        List<Trade> expected = new ArrayList<>();
        Order order1 = new Order("instrument1", 10, new BigDecimal("100.00"), "B", clientId, "ORDER001", 123);
    	Order order2 = new Order("instrument2", 15, new BigDecimal("120.00"), "B", clientId, "ORDER002", 123);
        Trade trade1 = new Trade(order1,new BigDecimal("100.00"),"TRADE001", new BigDecimal("1000.00"));
    	Trade trade2 = new Trade(order2,new BigDecimal("110.00"),"TRADE002", new BigDecimal("1000.00"));
    	expected.add(trade1);
    	expected.add(trade2);
        TradeHistory tradeHistory = new TradeHistory(clientId, expected);
        Mockito.when(mockDao.getClientTradeHistory(clientId))
			.thenReturn(tradeHistory);
		List<Trade> actual =  service.getClientTradeHistory(clientId);
		//Verifying that the corresponding mockDao methods were called
		Mockito.verify(mockDao).getClientTradeHistory(clientId); 
			assertTrue(actual.size() <= 100);
	 }

	

}
