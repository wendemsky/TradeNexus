package com.fidelity.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fidelity.integration.ClientDao;
import com.fidelity.integration.ClientTradeDao;
import com.fidelity.integration.DatabaseException;
import com.fidelity.models.Client;
//Importing models
import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.Holding;
import com.fidelity.models.Order;
import com.fidelity.models.Trade;

class PortfolioServiceTest {
	@Mock ClientTradeDao mockDao;
	@InjectMocks PortfolioService service;
	
	private List<ClientPortfolio> clientPortfolios;

	@BeforeEach
	void setUp() throws Exception {
	
		List<Holding> holdingsOf1654658069 = new ArrayList<Holding>(List.of(
				new Holding("Q123",2,new BigDecimal("105")),
				new Holding("Q456",1,new BigDecimal("340"))
			));
		List<Holding> holdingsOf541107416 = new ArrayList<Holding>(List.of(
				new Holding("C100", 1000, new BigDecimal("95.67")),
				new Holding("T67890",10, new BigDecimal("1.033828125"))
			));
		//Test client portfolios
		clientPortfolios = new ArrayList<ClientPortfolio>(
				List.of(
						new ClientPortfolio("1654658069", new BigDecimal("10000"), holdingsOf1654658069),
						new ClientPortfolio("541107416", new BigDecimal("20000"), holdingsOf541107416)
				)
			);
    	
    	//Initializing the Portfolio Service with a Mock Dao
    	MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void tearDown() throws Exception {
		service = null;
		clientPortfolios = null;
	}

	@Test
    void testGetClientPortfolio() {
		String clientId = "541107416"; //Client with a portfolio
		ClientPortfolio expected = clientPortfolios.get(1);
		//Mocking the dao method
		Mockito.when(mockDao.getClientPortfolio(clientId)).thenReturn(expected);
  
		ClientPortfolio clientPortfolio  = service.getClientPortfolio(clientId);
		assertEquals(clientPortfolio,expected, "Must return client portfolio");
    }
	
	@Test
    void testGetClientPortfolioOfNonExistentClientShouldThrowException() {
		String clientId = "541107416"; //Client with a portfolio
		String errorMsg = "Client ID does not exist";
		//Mock the behavior of Dao method to throw exception
		Mockito.doThrow(new DatabaseException(errorMsg)).when(mockDao).getClientPortfolio(clientId);
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.getClientPortfolio(clientId);
		});
		assertEquals(e.getMessage(),errorMsg);
    }
	
	 @Test
	   	void shouldNotGetForNullObject() {
	   		assertThrows(NullPointerException.class, () -> {
	   			service.getClientPortfolio(null);
	   		});
	   	}
	
	@Test
	void shouldNotUpdateForNullObject() {
		assertThrows(NullPointerException.class, () -> {
			service.updateClientPortfolio(null);
		});
	}
   
    
    @Test
    void testUpdatePortfolioForBuyTrade() {
    	 String instrumentId = "Q123";
         BigDecimal execPrice = new BigDecimal("105");
         Integer quantity = 2;
         BigDecimal currentBalance = new BigDecimal("500.00");
         
         String existingClientId = "541107416"; 

         // Create Order object ie passed
         Order order = new Order(instrumentId, quantity, new BigDecimal("105"), "B", existingClientId, "order1",1425922638);
         Trade trade = new Trade(
        		 order,
                 execPrice,
                 "trade1",
                 execPrice.multiply(new BigDecimal(quantity))
         );
         
         //Get old clientportfolio
         ClientPortfolio oldClientPortfolio =  service.getClientPortfolio(existingClientId);
         BigDecimal oldCurrBalance = oldClientPortfolio.getCurrBalance();
         //Mockito.verify(mockDao).verifyClientEmail(validEmail);
         service.updateClientPortfolio(trade);
         
         ClientPortfolio newClientPortfolio =  service.getClientPortfolio(existingClientId);
         BigDecimal newCurrBalance = newClientPortfolio.getCurrBalance();
         assertEquals(oldCurrBalance.compareTo(newCurrBalance)>0, true);
    	
    }
    
//    @Test
//    void testUpdatePortfolioForSellTrade() {
//    	 String instrumentId = "Q123";
//         BigDecimal execPrice = new BigDecimal("105");
//         Integer quantity = 1;
//         BigDecimal currentBalance = new BigDecimal("500.00");
//
//         // Create Order object if needed
//       
//         Order order = new Order(instrumentId, quantity, new BigDecimal("105"), "S", "1425922638", "order1",1425922638);
//         Trade trade = new Trade(
//        		 order,
//                 execPrice,
//                 "trade1",
//                 execPrice.multiply(new BigDecimal(quantity))
//         );
//         
//         //Get old clientportfolio
//         ClientPortfolio oldClientPortfolio =  portfolioService.getClientPortfolio("1425922638");
//         BigDecimal oldCurrBalance = oldClientPortfolio.getCurrBalance();
//         portfolioService.updateClientPortfolio(trade);
//         ClientPortfolio newClientPortfolio =  portfolioService.getClientPortfolio("1425922638");
//         BigDecimal newCurrBalance = newClientPortfolio.getCurrBalance();
//       
//         assertEquals(oldCurrBalance.compareTo(newCurrBalance)<0, true);
//    	
//    }
//    
//    @Test
//    void testUpdatePortfolioForBuyTradeOfNewInstrument() {
//    	 String instrumentId = "Q678";
//         BigDecimal execPrice = new BigDecimal("105");
//         Integer quantity = 1;
//         BigDecimal currentBalance = new BigDecimal("500.00");
//
//         // Create Order object if needed
//       
//         Order order = new Order(instrumentId, quantity, new BigDecimal("105"), "B", "1425922638", "order1",1425922638);
//         Trade trade = new Trade(
//        		 order,
//                 execPrice,
//                 "trade1",
//                 execPrice.multiply(new BigDecimal(quantity))
//         );
//         
//         //Get old clientportfolio
//         ClientPortfolio oldClientPortfolio =  portfolioService.getClientPortfolio("1425922638");
//         List<Holding> oldHoldings = oldClientPortfolio.getHoldings();
//         int oldHoldingsLength = oldHoldings.size();
//         portfolioService.updateClientPortfolio(trade);
//         ClientPortfolio newClientPortfolio =  portfolioService.getClientPortfolio("1425922638");
//         List<Holding> newHoldings = newClientPortfolio.getHoldings();
//         int newHoldingsLength = newHoldings.size();
//         assertTrue(newHoldingsLength==oldHoldingsLength+1);
//    	
//    }
//    
//    @Test
//    void testUpdatePortfolioForNonExistentInstruemntSellTrade() {
//    	 String instrumentId = "Q678";
//         BigDecimal execPrice = new BigDecimal("105");
//         Integer quantity = 1;
//         BigDecimal currentBalance = new BigDecimal("500.00");
//
//         // Create Order object if needed
//       
//         Order order = new Order(instrumentId, quantity, new BigDecimal("105"), "S", "1425922638", "order1",1425922638);
//         Trade trade = new Trade(
//        		 order,
//                 execPrice,
//                 "trade1",
//                 execPrice.multiply(new BigDecimal(quantity))
//         );
//         
//        Exception e = assertThrows(IllegalArgumentException.class, () -> {
//    			portfolioService.updateClientPortfolio(trade);
//    		});
//    	assertEquals(e.getMessage(),"Instrument not found for selling");
//    }
//   

}
