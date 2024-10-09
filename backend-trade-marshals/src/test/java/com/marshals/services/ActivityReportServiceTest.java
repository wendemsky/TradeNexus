package com.marshals.services;
 
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.marshals.integration.ClientActivityReportDao;
import com.marshals.integration.DatabaseException;
import com.marshals.models.Holding;
import com.marshals.models.Order;
import com.marshals.models.Trade;
import com.marshals.models.TradeHistory;
 
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:beans.xml")
class ActivityReportServiceTest {
	
	@Mock ClientActivityReportDao mockDao;
	@Mock TradeHistoryService mockTradeHistoryService;
	
	@Autowired
	@InjectMocks
	ActivityReportService service;

	Holding holding1 = new Holding("C100", 1000, new BigDecimal("95.67"));
	Holding holding2 = new Holding("T67890", 10, new BigDecimal("1.0338"));
	List<Holding> mockHoldings = new ArrayList<Holding>(
				List.of(
					holding1,
					holding2
				)
			);


	String clientId = "541107416";
    List<Trade> mockTradeList = new ArrayList<>();
    Order order1 = new Order("instrument1", 10, new BigDecimal("100.00"), "B", clientId, "ORDER001", 123);
	Order order2 = new Order("instrument2", 15, new BigDecimal("120.00"), "B", clientId, "ORDER002", 123);
	Order order3 = new Order("instrument3", 5, new BigDecimal("120.00"), "S", clientId, "ORDER003", 123);
    Trade trade1 = new Trade(order1,new BigDecimal("100.00"),"TRADE001", new BigDecimal("1000.00"));
	Trade trade2 = new Trade(order2,new BigDecimal("110.00"),"TRADE002", new BigDecimal("1000.00"));
	Trade trade3 = new Trade(order3,new BigDecimal("110.00"),"TRADE003", new BigDecimal("5000.00"));
	
	TradeHistory mockTradeHistory = new TradeHistory(clientId, mockTradeList);
 
	@BeforeEach
	void setUp() throws Exception {
//		 mockDao = mock(ClientActivityReportDao.class);
//	     mockTradeHistoryService = mock(TradeHistoryService.class);
//	     service = new ActivityReportService(mockDao,mockTradeHistoryService);
		
		
		 MockitoAnnotations.openMocks(this);
	     mockTradeList.add(trade1);
	 	 mockTradeList.add(trade2);
	 	 mockTradeList.add(trade3);
	}
 
	@AfterEach
	void tearDown() throws Exception {
		mockDao = null;
		mockTradeHistoryService = null;
		service = null;
		mockTradeList = null;
		mockTradeHistory = null;
	}
	
	@Test
	void testGenerateClientHoldingsForValidClient() {
		String clientId = "541107416";
		Mockito.when(mockDao.getClientHoldings(clientId))
			.thenReturn(mockHoldings);
		service.generateHoldingsReport(clientId);
		Mockito.verify(mockDao).getClientHoldings(clientId);
	}
	
	@Test
	void testGenerateClientHoldingsShouldHandleNullClientId() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			service.generateHoldingsReport(null);
		});
		assertEquals(e.getMessage(),"Client Id should not be null for Holdings");
	}
 
	@Test
	void testGenerateClientHoldingShouldHandleForNonExistentClientId() {
		String clientId = "541107416";
		Mockito.doThrow(new DatabaseException("Client has no holdings")).when(mockDao).getClientHoldings(clientId);
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.generateHoldingsReport(clientId);
			//Verifying that the corresponding mockDao methods were called
			Mockito.verify(mockDao).getClientHoldings(clientId);
		});
		assertEquals(e.getMessage(), "Client has no holdings");
	}
	
	@Test
	void testGenerateTradeReportForValidClient() {
		String clientId = "541107416";
		Mockito.when(mockTradeHistoryService.getClientTradeHistory(clientId))
			.thenReturn(mockTradeHistory);
		TradeHistory expected = service.generateTradeReport(clientId);
		Mockito.verify(mockTradeHistoryService).getClientTradeHistory(clientId);
		assertEquals(expected.getClientId(), clientId);
	}
	
	@Test
	void testGenerateTradeReportForNullClientId() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			service.generateTradeReport(null);
		});
		assertEquals(e.getMessage(),"Client Id should not be null for Trade History");
	}
	
	@Test
	void testGenerateTradeReportShouldHandleForNonExistentClientId() {
		String clientId = "541107400";
		Mockito.doThrow(new DatabaseException("Client has no Trade Report")).when(mockTradeHistoryService).getClientTradeHistory(clientId);
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.generateTradeReport(clientId);
			//Verifying that the corresponding mockDao methods were called
			Mockito.verify(mockTradeHistoryService).getClientTradeHistory(clientId);
		});
		assertEquals(e.getMessage(), "Client has no Trade Report");
	}
	
	@Test
	void testGeneratePLReportForValidClient() {
		String clientId = "541107416";
		Mockito.when(mockTradeHistoryService.getClientTradeHistory(clientId))
		.thenReturn(mockTradeHistory);
		Map<String, BigDecimal> expected = service.generatePLReport(clientId);
		Mockito.verify(mockTradeHistoryService).getClientTradeHistory(clientId);
		assertNotNull(expected);
	}
	
	@Test
	void testGeneratePLReportForNullClient() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			service.generatePLReport(null);
		});
		assertEquals(e.getMessage(),"Client Id should not be null to calculate Profit Loss");
	}
	
	@Test
	void testGeneratePLReportForInvalidClient() {
		String clientId = "541107400";
		Mockito.doThrow(new DatabaseException("Client has no Trade History")).when(mockTradeHistoryService).getClientTradeHistory(clientId);
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.generatePLReport(clientId);
			//Verifying that the corresponding mockDao methods were called
			Mockito.verify(mockTradeHistoryService).getClientTradeHistory(clientId);
		});
		assertEquals(e.getMessage(), "Client has no Trade History");
	}
}