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

class TradeServiceTest {

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

    private final TradeService tradeService = new TradeService();

    @Test
    void testExecuteTrade() {
        Order order = new Order(
            "instr1", 10, new BigDecimal("100.00"), "BUY", "client1", "order1", 12345
        );

        Trade trade = tradeService.executeTrade(order);

        assertNotNull(trade);
        assertEquals("N123456", trade.getInstrumentId());
        assertEquals(10, trade.getQuantity());
        assertEquals(new BigDecimal("104.25"), trade.getExecutionPrice());
        assertEquals("S", trade.getDirection());
        assertEquals("541107416", trade.getClientId());
        assertEquals(order, trade.getOrder());
        assertEquals("aw6rqg2ee1q-pn1jh9yhg3s-ea6xxmv06bj", trade.getTradeId());
        assertEquals(new BigDecimal("1052.925"), trade.getCashValue());
    }
    
    @Test
    void testExecuteTrade_throwsExceptionForNullOrder() {
        Order order = null;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
        	tradeService.executeTrade(order);
        });
        assertEquals("order cannot be null", thrown.getMessage());
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
    			"2",
    			"Fidelity International",
    			25,
    			new BigDecimal(100));
    	Holding holding2 = new Holding(
    			"1",
    			"2",
    			"Fidelity International",
    			25,
    			new BigDecimal(100));
    	Holding holding3 = new Holding(
    			"1",
    			"2",
    			"Fidelity International",
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
    			"2",
    			"Fidelity International1",
    			25,
    			new BigDecimal(100));
    	Holding holding2 = new Holding(
    			"1",
    			"2",
    			"Fidelity International2",
    			25,
    			new BigDecimal(100));
    	Holding holding3 = new Holding(
    			"1",
    			"2",
    			"Fidelity International3",
    			25,
    			new BigDecimal(100));
    	Holding holding4 = new Holding(
    			"1",
    			"2",
    			"Fidelity International4",
    			25,
    			new BigDecimal(100));
    	Holding holding5 = new Holding(
    			"1",
    			"2",
    			"Fidelity International5",
    			25,
    			new BigDecimal(100));
    	Holding holding6 = new Holding(
    			"1",
    			"2",
    			"Fidelity International6",
    			25,
    			new BigDecimal(100));
    	Holding holding7 = new Holding(
    			"1",
    			"2",
    			"Fidelity International7",
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

}