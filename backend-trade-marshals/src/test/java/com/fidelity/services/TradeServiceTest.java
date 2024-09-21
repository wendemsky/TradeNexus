package com.fidelity.services;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


//Importing models
import com.fidelity.models.Order;
import com.fidelity.models.Price;
import com.fidelity.models.Trade;
import com.fidelity.models.ClientPreferences;
import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.Holding;

//Importing utils for Robo Advisor
import com.fidelity.utils.PriceScorer;


public class TradeServiceTest {

    private TradeService tradeService;
    private List<Price> prices;

    @BeforeEach
    public void setUp() throws Exception {
        tradeService = new TradeService(); 
        prices = tradeService.getPriceList();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
    	tradeService = null;
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
   

    @Test
    public void testExecuteTradeInvalidOrderDirection() {
        Order order = new Order("N123456", 10, new BigDecimal("104.75"), "X", "client1", "order1", 123);
        assertThrows(IllegalArgumentException.class, () -> tradeService.executeTrade(order));
    }
    
   
    @Test
    public void testExecuteTradeShouldThrowExceptionForNullOrder() {
    	Exception e = assertThrows(NullPointerException.class, () -> {
    		tradeService.executeTrade(null);
    	});
    	assertEquals("order cannot be null", e.getMessage());
    	
    }
    
    @Test
    public void testExecuteTradeBuy() {
    	Order order = new Order("N123456", 10, new BigDecimal("10.75"), "B", "1425922638", "order1", 123);
    	Trade trade = tradeService.executeTrade(order);
    	assertTrue(trade != null);
    }
    
    @Test
    public void testExecuteTradeSell() {
    	Order order = new Order("N123456", 1, new BigDecimal("104.75"), "S", "1425922638", "order1", 123);
    	Trade trade = tradeService.executeTrade(order);
    	assertTrue(trade != null);
    }
    
    @Test
    public void testExecuteTradeThrowExceptionForInvalidDirection() {
    	Order order = new Order("N123456", 1, new BigDecimal("104.75"), "X", "1425922638", "order1", 123);
    	Exception e = assertThrows(IllegalArgumentException.class, () -> {
    		tradeService.executeTrade(order);
    	});
    	assertEquals("Order direction is invalid", e.getMessage());
    	
    }
    
    @Test
    public void testExecuteTradeThrowExceptionForNonExistingInstrument() {
    	Order order = new Order("NonExistingInstrument", 10, new BigDecimal("104.75"), "B", "1425922638", "order1", 123);
    	Exception e = assertThrows(IllegalArgumentException.class, () -> {
    		tradeService.executeTrade(order);
    	});
    	assertEquals("Instrument is not present in the platform", e.getMessage());
    	
    }
    
//    -----------------TESTS FOR ROBO ADVISOR---------------------------
    
    @Test
    void testPriceScorerClass() {
    	ClientPreferences prefs = new ClientPreferences(
    	        "1",
    	        "Retirement",
    	        "VHIG",
    	        "Long",
    	        "Tier3",
    	         2, 
    	         true
    	        );
    	PriceScorer score = new PriceScorer(prefs);
    	
    	assertEquals(score.calculateScore(), 15);
    }
    
    @Test
    public void testRoboAdvisorBuyTradesWhenAcceptAdvisorIsFalse() {
    	//Sample Client Portfolio and Preferences
    	ClientPortfolio clientPortfolio = new ClientPortfolio("1425922638",new BigDecimal("1000"),new ArrayList<>());
    	ClientPreferences prefs = new ClientPreferences(
    	        "1425922638",
    	        "Retirement",
    	        "VHIG",
    	        "Long",
    	        "Tier3",
    	         2, 
    	         false
    	        );
        Exception e = assertThrows(UnsupportedOperationException.class, () -> {
        	List<Price> topBuys = tradeService.recommendTopBuyInstruments(prefs,clientPortfolio.getCurrBalance());
        });
        assertEquals(e.getMessage(),"Cannot recommend with robo advisor without accepting to it");
    }
    
    @Test
    public void testRoboAdvisorSellTradesWhenAcceptAdvisorIsFalse() {
    	Holding holding = new Holding(
    			"N123456",
    			5,
    			new BigDecimal(100));
    	List<Holding> clientHoldings = new ArrayList<Holding>();
    	clientHoldings.add(holding);
    	//Sample Client Portfolio and Preferences
    	ClientPortfolio clientPortfolio = new ClientPortfolio("1425922638",new BigDecimal("1000"),clientHoldings);
    	ClientPreferences prefs = new ClientPreferences(
    	        "1425922638",
    	        "Retirement",
    	        "VHIG",
    	        "Long",
    	        "Tier3",
    	         2, 
    	         false
    	        );
        Exception e = assertThrows(UnsupportedOperationException.class, () -> {
        	List<Price> topSells = tradeService.recommendTopSellInstruments(prefs,clientPortfolio.getHoldings());
        });
        assertEquals(e.getMessage(),"Cannot recommend with robo advisor without accepting to it");
    }
    
    @Test
    void testRoboAdvisorBuyTrades(){
    	//Sample Client Portfolio and Preferences
    	ClientPortfolio clientPortfolio = new ClientPortfolio("1425922638",new BigDecimal("1000"),new ArrayList<>());
    	ClientPreferences prefs = new ClientPreferences(
    	        "1425922638",
    	        "Retirement",
    	        "VHIG",
    	        "Long",
    	        "Tier3",
    	         2, 
    	         true
    	        );
    	List<Price> topBuys = tradeService.recommendTopBuyInstruments(prefs,clientPortfolio.getCurrBalance());
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
    	assertEquals(tradeService.recommendTopSellInstruments(prefs,clientPortfolio.getHoldings()).size(), 3);
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
    	assertEquals(tradeService.recommendTopSellInstruments(prefs,clientPortfolio.getHoldings()).size(), 5);
    }
}
