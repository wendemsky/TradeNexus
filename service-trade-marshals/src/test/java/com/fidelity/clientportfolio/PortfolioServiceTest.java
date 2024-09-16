package com.fidelity.clientportfolio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fidelity.trade.*;

class PortfolioServiceTest {
	
	private PortfolioService portfolioService;
	private List<ClientPortfolio> clientPortfolios;

	@BeforeEach
	void setUp() throws Exception {
		
		List<Holding> holdings = new ArrayList<Holding>(List.of(
				new Holding("Q123",2,new BigDecimal("105")),
				new Holding("Q456",1,new BigDecimal("340"))
			));
		clientPortfolios = new ArrayList<ClientPortfolio>(
				List.of(
						new ClientPortfolio("1425922638", new BigDecimal("1000"), holdings),
						new ClientPortfolio("1425922634", new BigDecimal("2000"), holdings)
				)
			);
    	portfolioService = new PortfolioService();
    	portfolioService.addClientPortfolio(clientPortfolios.get(0));
    	portfolioService.addClientPortfolio(clientPortfolios.get(1));
	}

	@AfterEach
	void tearDown() throws Exception {
		portfolioService = null;
		clientPortfolios = null;
	}

	@Test
    public void testGetClientPortfolio() {
    	ClientPortfolio clientPortfolio =  portfolioService.getClientPortfolio("1425922638");
    	ClientPortfolio expected = clientPortfolios.get(0);
		assertEquals(clientPortfolio.equals(expected), true);
    }
	@Test
	void shouldNotAddForNullObject() {
		assertThrows(NullPointerException.class, () -> {
			portfolioService.addClientPortfolio(null);
		});
	}
	@Test
	void shouldNotUpdateForNullObject() {
		assertThrows(NullPointerException.class, () -> {
			portfolioService.updateClientPortfolio(null);
		});
	}
    @Test
   	void shouldNotGetForNullObject() {
   		assertThrows(NullPointerException.class, () -> {
   			portfolioService.getClientPortfolio(null);
   		});
   	}
    
    @Test
    void testUpdatePortfolioForBuyTrade() {
    	 String instrumentId = "Q123";
         BigDecimal execPrice = new BigDecimal("105");
         Integer quantity = 2;
         BigDecimal currentBalance = new BigDecimal("500.00");

         // Create Order object if needed
       
         Order order = new Order(instrumentId, quantity, new BigDecimal("105"), "B", "1425922638", "order1",1425922638);
         Trade trade = new Trade(
                 instrumentId,
                 quantity,
                 execPrice,
                 "B", // Buy direction
                 "1425922638",
                 order,
                 "trade1",
                 execPrice.multiply(new BigDecimal(quantity))
         );
         
         //Get old clientportfolio
         ClientPortfolio oldClientPortfolio =  portfolioService.getClientPortfolio("1425922638");
         BigDecimal oldCurrBalance = oldClientPortfolio.getCurrBalance();
         portfolioService.updateClientPortfolio(trade);
         ClientPortfolio newClientPortfolio =  portfolioService.getClientPortfolio("1425922638");
         BigDecimal newCurrBalance = newClientPortfolio.getCurrBalance();
       
         assertEquals(oldCurrBalance.compareTo(newCurrBalance)>0, true);
    	
    }
    
    @Test
    void testUpdatePortfolioForSellTrade() {
    	 String instrumentId = "Q123";
         BigDecimal execPrice = new BigDecimal("105");
         Integer quantity = 1;
         BigDecimal currentBalance = new BigDecimal("500.00");

         // Create Order object if needed
       
         Order order = new Order(instrumentId, quantity, new BigDecimal("105"), "S", "1425922638", "order1",1425922638);
         Trade trade = new Trade(
                 instrumentId,
                 quantity,
                 execPrice,
                 "S", // Buy direction
                 "1425922638",
                 order,
                 "trade1",
                 execPrice.multiply(new BigDecimal(quantity))
         );
         
         //Get old clientportfolio
         ClientPortfolio oldClientPortfolio =  portfolioService.getClientPortfolio("1425922638");
         BigDecimal oldCurrBalance = oldClientPortfolio.getCurrBalance();
         portfolioService.updateClientPortfolio(trade);
         ClientPortfolio newClientPortfolio =  portfolioService.getClientPortfolio("1425922638");
         BigDecimal newCurrBalance = newClientPortfolio.getCurrBalance();
       
         assertEquals(oldCurrBalance.compareTo(newCurrBalance)<0, true);
    	
    }
    
    @Test
    void testUpdatePortfolioForBuyTradeOfNewInstrument() {
    	 String instrumentId = "Q678";
         BigDecimal execPrice = new BigDecimal("105");
         Integer quantity = 1;
         BigDecimal currentBalance = new BigDecimal("500.00");

         // Create Order object if needed
       
         Order order = new Order(instrumentId, quantity, new BigDecimal("105"), "B", "1425922638", "order1",1425922638);
         Trade trade = new Trade(
                 instrumentId,
                 quantity,
                 execPrice,
                 "B", // Buy direction
                 "1425922638",
                 order,
                 "trade1",
                 execPrice.multiply(new BigDecimal(quantity))
         );
         
         //Get old clientportfolio
         ClientPortfolio oldClientPortfolio =  portfolioService.getClientPortfolio("1425922638");
         List<Holding> oldHoldings = oldClientPortfolio.getHoldings();
         int oldHoldingsLength = oldHoldings.size();
         portfolioService.updateClientPortfolio(trade);
         ClientPortfolio newClientPortfolio =  portfolioService.getClientPortfolio("1425922638");
         List<Holding> newHoldings = newClientPortfolio.getHoldings();
         int newHoldingsLength = newHoldings.size();
         assertTrue(newHoldingsLength==oldHoldingsLength+1);
    	
    }
    
    @Test
    void testUpdatePortfolioForNonExistentInstruemntSellTrade() {
    	 String instrumentId = "Q678";
         BigDecimal execPrice = new BigDecimal("105");
         Integer quantity = 1;
         BigDecimal currentBalance = new BigDecimal("500.00");

         // Create Order object if needed
       
         Order order = new Order(instrumentId, quantity, new BigDecimal("105"), "S", "1425922638", "order1",1425922638);
         Trade trade = new Trade(
                 instrumentId,
                 quantity,
                 execPrice,
                 "S", // Buy direction
                 "1425922638",
                 order,
                 "trade1",
                 execPrice.multiply(new BigDecimal(quantity))
         );
         
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
    			portfolioService.updateClientPortfolio(trade);
    		});
    	assertEquals(e.getMessage(),"Instrument not found for selling");
    }
   

}
