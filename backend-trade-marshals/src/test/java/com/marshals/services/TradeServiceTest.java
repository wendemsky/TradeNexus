package com.marshals.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import com.marshals.fmts.FMTSService;
import com.marshals.integration.ClientDao;
import com.marshals.integration.ClientTradeDao;
import com.marshals.integration.ClientTradeDaoImpl;
import com.marshals.integration.DatabaseException;
import com.marshals.integration.PoolableDataSource;
import com.marshals.integration.TransactionManager;
import com.marshals.models.Client;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.ClientPreferences;
import com.marshals.models.Holding;
import com.marshals.models.Order;
import com.marshals.models.Price;
import com.marshals.models.Trade;
import com.marshals.services.PortfolioService;
import com.marshals.services.TradeService;
import com.marshals.utils.PriceScorer;


public class TradeServiceTest {
	
	ClientTradeDao mockDao;
	PortfolioService mockPortfolioService;
	FMTSService mockFMTSService;
	
	@Autowired
	TradeService service;
	
//	//Mock of fmtsService
//	static MockedStatic<FMTSService> mockFMTSService;
	
	List<Holding> holdingsOf1425922638 = new ArrayList<Holding>(List.of(
			new Holding("C100",100,new BigDecimal("95.67")),
			new Holding("Q456",1,new BigDecimal("340"))
		));
	//Test client portfolios
	List<ClientPortfolio> clientPortfolios = new ArrayList<ClientPortfolio>(
			List.of(
					new ClientPortfolio("1425922638", new BigDecimal("10000"), holdingsOf1425922638), //Client portfolio with sufficient balance
					new ClientPortfolio("1425922638", new BigDecimal("20"), holdingsOf1425922638) //Client portfolio with insufficient balance
			)
		);

    private List<Price> prices;


    @BeforeEach
    public void setUp() throws Exception {
      //Initializing the Trade Service with Mock Dao and Mock PortfolioService
      mockDao = mock(ClientTradeDao.class);
      mockPortfolioService = mock(PortfolioService.class);
      mockFMTSService = mock(FMTSService.class);

      service = new TradeService(mockDao, mockPortfolioService, mockFMTSService);
      prices = service.getPriceList();
      System.out.println("Price: "+prices);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
    	service = null;
    	mockDao = null;
    	mockPortfolioService = null;
    	prices = null;
	}

    @Test
    public void testGetAllPricesShouldReturn13Prices() {
        assertEquals(13, prices.size(),"Should return 13 instrument prices");
    }
    
    @Test
    public void testGetAllPricesFirstPriceShouldBeAsExpected() {
    	Price price = prices.get(0);
        assertEquals(new BigDecimal("104.75"), price.getAskPrice());
        assertEquals(new BigDecimal("104.25"), price.getBidPrice());
        assertEquals("21-AUG-19 10.00.01.042000000 AM GMT", price.getPriceTimestamp());
        assertEquals("N123456", price.getInstrument().getInstrumentId());
    }
   

    /*TESTING WITH MOCKITO - ADD TRADE TESTS*/
    
     @Test
	 public void testAddTrades() {
		 String clientId = "1654658069";
		 Order order = mock(Order.class); //new Order("instrument1", 10, new BigDecimal("100.00"), "B", clientId, "ORDER001", 123);
		 Trade trade = mock(Trade.class);
		 service.addTrade(trade);
		 Mockito.verify(mockDao).addTrade(trade);
		 
	 }
     
     @Test
	 public void testAddExistingTradeThrowsException() {
		 String clientId = "1654658069";
		 Order order = mock(Order.class); //new Order("instrument1", 10, new BigDecimal("100.00"), "B", clientId, "ORDER001", 123);
		 Trade trade = mock(Trade.class);
		 //Mocking my dao to throw an exception
		 Mockito.doThrow(new DatabaseException()).when(mockDao).addTrade(trade);
		 assertThrows(DatabaseException.class, () -> {
			 	service.addTrade(trade);
			 	Mockito.verify(mockDao).addTrade(trade);
			});
	 }
	 
	 @Test
	 public void testAddTradeThrowsExceptionForNullTrade() {
		 Trade trade = null;
		 Exception e = assertThrows(NullPointerException.class, () -> {
				service.addTrade(trade);
			});
		 assertEquals(e.getMessage(), "Trade must not be null");
		 
	 }
	 
	 /*EXECUTE TRADE TESTS*/ 
  
	 @Test
	  public void testExecuteTradeShouldThrowExceptionForNullOrder() {
	    	Exception e = assertThrows(NullPointerException.class, () -> {
	    		service.executeTrade(null);
	    	});
	    	assertEquals("order cannot be null", e.getMessage());	
	    }
	 
    @Test
    public void testExecuteTradeInvalidOrderDirection() {
    	String clientId = "1654658069";
    	UUID uuid=UUID.randomUUID();
    	String orderId = uuid.toString();
        Order order = new Order("N123456", 10, new BigDecimal("104.75"), "X", clientId, orderId, 123);
        
        //Mocking portfolio service fns
    	Mockito.when(mockPortfolioService.getClientPortfolio(clientId)).thenReturn(clientPortfolios.get(0));
        
        Exception e = assertThrows(IllegalArgumentException.class, () -> 
        service.executeTrade(order));
        assertEquals("Order direction is invalid", e.getMessage());
    }
    
	@Test
	public void testExecuteTradeThrowExceptionForNonExistingInstrument() {
		String clientId = "1654658069";
	  	UUID uuid=UUID.randomUUID();
	  	String orderId = uuid.toString();
	  	Order order = new Order("NonExistingInstrument", 10, new BigDecimal("104.75"), "B", clientId, orderId, 123);
	  	
	  	//Mocking portfolio service fns
    	Mockito.when(mockPortfolioService.getClientPortfolio(clientId)).thenReturn(clientPortfolios.get(0));
    	
	  	Exception e = assertThrows(IllegalArgumentException.class, () -> {
	  		service.executeTrade(order);
	  	});
	  	assertEquals("Instrument is not present in the platform", e.getMessage());
	  }
   
	 
	  
	  /*TESTING WITH MOCKITO - EXECUTE TRADE TESTS*/
	  
	  @Test
	  public void testExecuteSuccessfulBuyTrade() {
		  	String existingClientId = "1425922638";
	    	UUID uuid=UUID.randomUUID();
	    	String orderId = uuid.toString();
	    	Order order = new Order("N123456", 10, new BigDecimal("10.75"), "B", existingClientId, orderId, 123);
	    	
//	    	Trade trade = new Trade(order, new BigDecimal("10.75"), "T"+orderId, new BigDecimal("107.5"));
//	    	//Mocking fmts service and mock it to return trade
//	    	mockFMTSService = mockStatic(FMTSService.class);
//	    	mockFMTSService.when(()-> FMTSService.createTrade(order)).thenReturn(trade);
	    	
	    	//Mocking portfolio service fns
	    	Mockito.when(mockPortfolioService.getClientPortfolio(existingClientId)).thenReturn(clientPortfolios.get(0));
	    	//Executing trade
	    	Trade trade = service.executeTrade(order);
	    	//Verifying if mock portfolio service to update portfolio is called
	    	Mockito.verify(mockPortfolioService).updateClientPortfolio(trade);
	    	//Verifying if add Trade dao is called
	    	Mockito.verify(mockDao).addTrade(trade);
	    }
 
		@Test
		public void testExecuteTradeSell() {
			String existingClientId = "1425922638";
			UUID uuid=UUID.randomUUID();
			String orderId = uuid.toString();
			Order order = new Order("C100", 10, new BigDecimal("104.75"), "S", existingClientId, orderId, 123);
			
			//Mocking portfolio service fns
	    	Mockito.when(mockPortfolioService.getClientPortfolio(existingClientId)).thenReturn(clientPortfolios.get(0));
	    	
	    	//Executing trade
			Trade trade = service.executeTrade(order);
			//Verifying if mock portfolio service to update portfolio is called
	    	Mockito.verify(mockPortfolioService).updateClientPortfolio(trade);
	    	//Verifying if add Trade dao is called
	    	Mockito.verify(mockDao).addTrade(trade);
		}   
		
	  @Test
	  public void testExecuteBuyTradeWithInsufficientBalanceThrowsException() {
		  	String existingClientId = "1425922638";
	    	UUID uuid=UUID.randomUUID();
	    	String orderId = uuid.toString();
	    	Order order = new Order("N123456", 10, new BigDecimal("10.75"), "B", existingClientId, orderId, 123);
	    	
	    	//Mocking portfolio service fns
	    	Mockito.when(mockPortfolioService.getClientPortfolio(existingClientId)).thenReturn(clientPortfolios.get(1));
	    	
	    	Exception e = assertThrows(IllegalArgumentException.class, () -> {
		  		service.executeTrade(order);
		  	});
		  	assertEquals("Insufficient balance! Cannot buy the instrument", e.getMessage());
	    }
    
    @Test
    public void testExecuteSellTradeForNonExistentInstrument() {
    	 String nonExistentInstrumentId = "N123456";
         String existingClientId = "1425922638"; //This client doesnt have above instrument
         UUID uuid=UUID.randomUUID();
         String orderId = uuid.toString();
         
         Order order = new Order(nonExistentInstrumentId, 10, new BigDecimal("105"), "S", existingClientId, orderId, 1425922638);
         
         //Mocking portfolio service fns
	    Mockito.when(mockPortfolioService.getClientPortfolio(existingClientId)).thenReturn(clientPortfolios.get(1));
	    
	    Exception e = assertThrows(IllegalArgumentException.class, () -> {
	  		service.executeTrade(order);
	  	});
	  	assertEquals("Instrument not part of holdings! Cannot sell the instrument", e.getMessage());
        
    }
    
    @Test
    public void testExecuteSellTradeForInsufficientQuantity() {
    	 String existentInstrumentId = "C100";
    	 int exceededQuantity = 1000;
         String existingClientId = "1425922638"; //This client doesnt have above instrument
         UUID uuid=UUID.randomUUID();
         String orderId = uuid.toString();
         
         Order order = new Order(existentInstrumentId, exceededQuantity, new BigDecimal("105"), "S", existingClientId, orderId, 1425922638);
         
         //Mocking portfolio service fns
	    Mockito.when(mockPortfolioService.getClientPortfolio(existingClientId)).thenReturn(clientPortfolios.get(1));
	    
	    Exception e = assertThrows(IllegalArgumentException.class, () -> {
	  		service.executeTrade(order);
	  	});
	  	assertEquals("Insufficient quantity in holdings to sell the instrument", e.getMessage());
        
    }
 
    
//    -----------------TESTS FOR ROBO ADVISOR---------------------------
    
    @Test
    public void testRoboAdvisorBuyTradesWhenAcceptAdvisorIsFalse() {
    	//Sample Client Portfolio and Preferences
    	String existingClientId = "1425922638";
    	ClientPortfolio clientPortfolio = clientPortfolios.get(0);
    	ClientPreferences prefs = new ClientPreferences(
    			existingClientId,
    	        "Retirement",
    	        "VHIG",
    	        "Long",
    	        "Tier3",
    	         2, 
    	         false
    	        );
        Exception e = assertThrows(UnsupportedOperationException.class, () -> {
        	List<Price> topBuys = service.recommendTopBuyInstruments(prefs,clientPortfolio.getCurrBalance());
        });
        assertEquals(e.getMessage(),"Cannot recommend with robo advisor without accepting to it");
    }
    
    @Test
    public void testRoboAdvisorSellTradesWhenAcceptAdvisorIsFalse() {
    	String existingClientId = "1425922638";
    	ClientPortfolio clientPortfolio = clientPortfolios.get(0);
    	ClientPreferences prefs = new ClientPreferences(
    			existingClientId,
    	        "Retirement",
    	        "VHIG",
    	        "Long",
    	        "Tier3",
    	         2, 
    	         false
    	        );
        Exception e = assertThrows(UnsupportedOperationException.class, () -> {
        	List<Price> topSells = service.recommendTopSellInstruments(prefs,clientPortfolio.getHoldings());
        });
        assertEquals(e.getMessage(),"Cannot recommend with robo advisor without accepting to it");
    }
//    
    @Test
    void testRoboAdvisorBuyTrades(){
    	//Sample Client Portfolio and Preferences
    	String existingClientId = "1425922638";
    	ClientPortfolio clientPortfolio = clientPortfolios.get(0);
    	ClientPreferences prefs = new ClientPreferences(
    	        existingClientId,
    	        "Retirement",
    	        "VHIG",
    	        "Long",
    	        "Tier3",
    	         2, 
    	         true
    	        );
    	List<Price> topBuys = service.recommendTopBuyInstruments(prefs,clientPortfolio.getCurrBalance());
    	assertNotEquals(topBuys.equals(null), true);
    	assertEquals(topBuys.size(), 5);
    }
    
    @Test
    void testRoboAdvisorSellTradesLessThanFiveHoldings() {
    	//Consider a client portfolio with the following holdings
    	Holding holding1 = new Holding(
    			"N123456",
    			5,
    			new BigDecimal(100));
    	Holding holding2 = new Holding(
    			"N123789",
    			10,
    			new BigDecimal(1000));
    	Holding holding3 = new Holding(
    			"C100",
    			15,
    			new BigDecimal(100));
    	List<Holding> clientHoldings = new ArrayList<Holding>();
    	clientHoldings.add(holding1);
    	clientHoldings.add(holding2);
    	clientHoldings.add(holding3);
    	//Sample Client Portfolio and Preferences
    	ClientPortfolio clientPortfolio = new ClientPortfolio("1425922638",new BigDecimal("1000"),clientHoldings);
     	ClientPreferences prefs = new ClientPreferences(
    	        "1425922638",
    	        "Retirement",
    	        "VHIG",
    	        "Long",
    	        "Tier3",
    	         2, 
    	         true
    	        );
    	assertEquals(service.recommendTopSellInstruments(prefs,clientPortfolio.getHoldings()).size(), 3);
    }
    
    @Test
    void testRoboAdvisorSellTradesMoreThanFiveHoldings() {
    	//Consider a client portfolio with the following holdings
    	Holding holding1 = new Holding(
    			"N123456",
    			5,
    			new BigDecimal(100));
    	Holding holding2 = new Holding(
    			"N123789",
    			10,
    			new BigDecimal(1000));
    	Holding holding3 = new Holding(
    			"C100",
    			15,
    			new BigDecimal(100));
    	Holding holding4 = new Holding(
    			"T67890",
    			25,
    			new BigDecimal(2));
    	Holding holding5 = new Holding(
    			"T67894",
    			20,
    			new BigDecimal(1));
    	Holding holding6 = new Holding(
    			"T67899",
    			25,
    			new BigDecimal(1));
    	Holding holding7 = new Holding(
    			"T67880",
    			25,
    			new BigDecimal(1));
    	List<Holding> clientHoldings = new ArrayList<Holding>();
    	clientHoldings.add(holding1);
    	clientHoldings.add(holding2);
    	clientHoldings.add(holding3);
    	clientHoldings.add(holding4);
    	clientHoldings.add(holding5);
    	clientHoldings.add(holding6);
    	clientHoldings.add(holding7);
    	//Sample Client Portfolio and Preferences
    	ClientPortfolio clientPortfolio = new ClientPortfolio("1425922638",new BigDecimal("1000"),clientHoldings);
     	ClientPreferences prefs = new ClientPreferences(
    	        "1425922638",
    	        "Retirement",
    	        "VHIG",
    	        "Long",
    	        "Tier3",
    	         2, 
    	         true
    	        );
    	assertEquals(service.recommendTopSellInstruments(prefs,clientPortfolio.getHoldings()).size(), 5);
    }
}
