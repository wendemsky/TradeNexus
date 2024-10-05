package com.marshals.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.marshals.dao.ClientTradeDao;
import com.marshals.integration.DatabaseException;
import com.marshals.models.Order;
import com.marshals.models.Trade;
import com.marshals.models.TradeHistory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


class TradeHistoryServiceTest {
	
	@Mock ClientTradeDao mockDao;
	@InjectMocks TradeHistoryService service;
    
	@BeforeEach
	void setUp() throws Exception {
		 //tradeHistoryService = new TradeHistoryService(mockDao); 
	     MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void tearDown() throws Exception {
		service = null;
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
		TradeHistory actualTradeHistory =  service.getClientTradeHistory(clientId);
		//Verifying that the corresponding mockDao methods were called
		Mockito.verify(mockDao).getClientTradeHistory(clientId); 
		assertEquals(actualTradeHistory,tradeHistory);
    }
	
	
	 @Test
	  public void testGetClientTradeHistoryThrowsNullPointerException() {        
		 Exception e = assertThrows(NullPointerException.class, () -> service.getClientTradeHistory(null));
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
        TradeHistory actualTradeHistory =  service.getClientTradeHistory(clientId);
		//Verifying that the corresponding mockDao methods were called
		Mockito.verify(mockDao).getClientTradeHistory(clientId); 
			assertTrue(actualTradeHistory.getTrades().size() <= 100);
	 }

	

}
