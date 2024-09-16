package com.fidelity.trade;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fidelity.client.ClientPreferences;
import com.fidelity.clientportfolio.Holding;
import com.fidelity.clientportfolio.PortfolioService;
import com.fidelity.clientportfolio.ClientPortfolio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class TradeServiceTest {

    private TradeService tradeService;
//    private PortfolioService portfolioService;
//    private ClientPortfolio clientPortfolio;

    @BeforeEach
    public void setUp() {
        tradeService = new TradeService(); 
//        portfolioService = new PortfolioService();
//        List<Holding> holdings = new ArrayList<>();
//        clientPortfolio = new ClientPortfolio("1425922638", new BigDecimal("1000"), holdings);
//        portfolioService.addClientPortfolio(clientPortfolio);
    }

    @Test
    public void testGetAllPricesShouldReturn13Prices() {
        List<Price> prices = tradeService.getAllPrices();

        assertEquals(13, prices.size());
    }
    
    @Test
    public void testGetAllPricesFirstPriceShouldBeAsExpected() {
    	List<Price> prices = tradeService.getAllPrices();
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
    	         "true"
    	        );
    	PriceScorer score = new PriceScorer(prefs);
    	
    	assertEquals(score.calculateScore(), 15);
    }
    
    @Test
    void testRoboAdvisorBuyTrades(){
    	List<Price> topBuys = new ArrayList<Price>();
    	List<Price> trades = new ArrayList<Price>();
    	ClientPreferences prefs = new ClientPreferences(
    	        "1",
    	        "Retirement",
    	        "VHIG",
    	        "Long",
    	        "Tier3",
    	         2, 
    	         "true"
    	        );
    	
    	try {
			trades = tradeService.getAllPrices();
			topBuys = tradeService.recommendTopTrades(trades, prefs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	assertNotEquals(topBuys.equals(null), true);
    	assertEquals(topBuys.size(), 5);
    }
    
 
    @Test
    void testRoboAdvisorSellTradesLessThanFiveHoldings() {
    	Holding holding1 = new Holding(
    			"1",
    			25,
    			new BigDecimal(100));
    	Holding holding2 = new Holding(
    			"1",
    			25,
    			new BigDecimal(100));
    	Holding holding3 = new Holding(
    			"1",
    			25,
    			new BigDecimal(100));
    	List<Holding> clientHoldings = new ArrayList<Holding>();
    	clientHoldings.add(holding1);
    	clientHoldings.add(holding2);
    	clientHoldings.add(holding3);
    	assertEquals(tradeService.recommendTopSellTrades(clientHoldings).size(), 3);
    }
    
    @Test
    void testRoboAdvisorSellTradesMoreThanFiveHoldings() {
    	Holding holding1 = new Holding(
    			"1",
    			25,
    			new BigDecimal(100));
    	Holding holding2 = new Holding(
    			"1",
    			25,
    			new BigDecimal(100));
    	Holding holding3 = new Holding(
    			"1",
    			25,
    			new BigDecimal(100));
    	Holding holding4 = new Holding(
    			"1",
    			25,
    			new BigDecimal(100));
    	Holding holding5 = new Holding(
    			"1",
    			25,
    			new BigDecimal(100));
    	Holding holding6 = new Holding(
    			"1",
    			25,
    			new BigDecimal(100));
    	Holding holding7 = new Holding(
    			"1",
    			25,
    			new BigDecimal(100));
    	List<Holding> clientHoldings = new ArrayList<Holding>();
    	clientHoldings.add(holding1);
    	clientHoldings.add(holding2);
    	clientHoldings.add(holding3);
    	clientHoldings.add(holding4);
    	clientHoldings.add(holding5);
    	clientHoldings.add(holding6);
    	clientHoldings.add(holding7);
    	assertEquals(tradeService.recommendTopSellTrades(clientHoldings).size(), 5);
    }
    
    
    @Test
    public void testCreateTradeValidOrder() {
        Order order = new Order("N123456", 10, new BigDecimal("104.75"), "B", "client1", "order1", 123);

        Trade trade = tradeService.createTrade(order);

        assertNotNull(trade);
        assertEquals(order.getInstrumentId(), trade.getInstrumentId());
        assertEquals(order.getQuantity(), trade.getQuantity());
        assertEquals(order.getTargetPrice(), trade.getExecutionPrice());
        assertEquals(order.getDirection(), trade.getDirection());
        assertEquals(order.getClientId(), trade.getClientId());
        assertEquals("id", trade.getTradeId());
    }

    @Test
    public void testCreateTradeNullOrder() {
        assertThrows(NullPointerException.class, () -> tradeService.createTrade(null), "order cannot be null");
    }

    @Test
    public void testCreateTradeInvalidOrderData() {
        // Testing with an order that has invalid or edge-case data
        Order order = new Order("N123456", 5, new BigDecimal("200.00"), "B", "clientId123", "order1", 123);

        Trade trade = tradeService.createTrade(order);

        assertNotNull(trade);
        assertEquals(order.getInstrumentId(), trade.getInstrumentId());
        assertEquals(order.getQuantity(), trade.getQuantity());
        assertEquals(order.getTargetPrice(), trade.getExecutionPrice());
        assertEquals(order.getDirection(), trade.getDirection());
        assertEquals(order.getClientId(), trade.getClientId());
        assertEquals("id", trade.getTradeId());
    }
    
    
    
    @Test
    public void testExecuteTradeShouldThrowExceptionForNullOrder() {
    	Order order = null;
    	Exception e = assertThrows(IllegalArgumentException.class, () -> {
    		tradeService.executeTrade(order);
    	});
    	assertEquals("order cannot be null", e.getMessage());
    	
    }
    
    @Test
    public void testExecuteTradeBuy() {
    	Order order = new Order("N123456", 10, new BigDecimal("104.75"), "B", "1425922638", "order1", 123);
    	Trade trade = tradeService.executeTrade(order);
    	assertTrue(trade != null);
    }
    
    @Test
    public void testExecuteTradeSell() {
    	Order order = new Order("N123456", 10, new BigDecimal("104.75"), "S", "1425922638", "order1", 123);
    	Trade trade = tradeService.executeTrade(order);
    	assertTrue(trade != null);
    }
    
    @Test
    public void testExecuteTradeThrowExceptionForInvalidDirection() {
    	Order order = new Order("N123456", 10, new BigDecimal("104.75"), "X", "1425922638", "order1", 123);
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


}
