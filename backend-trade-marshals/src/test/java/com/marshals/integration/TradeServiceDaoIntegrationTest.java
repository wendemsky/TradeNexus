package com.marshals.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTableWhere;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.marshals.fmts.FMTSService;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.Holding;
import com.marshals.models.Order;
import com.marshals.models.Price;
import com.marshals.models.Trade;
import com.marshals.services.ClientService;
import com.marshals.services.PortfolioService;
import com.marshals.services.TradeService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:beans.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class TradeServiceDaoIntegrationTest {

	@Autowired
	private TradeService service;

	@Autowired
	private JdbcTemplate testJdbcTemplate;

	List<Holding> holdingsOf1425922638 = new ArrayList<Holding>(
			List.of(new Holding("C100", 100, new BigDecimal("95.67")), new Holding("Q456", 1, new BigDecimal("340"))));
	// Test client portfolios
	List<ClientPortfolio> clientPortfolios = new ArrayList<ClientPortfolio>(
			List.of(new ClientPortfolio("1425922638", new BigDecimal("10000"), holdingsOf1425922638), // Client
																										// portfolio
																										// with
																										// sufficient
																										// balance
					new ClientPortfolio("1425922638", new BigDecimal("20"), holdingsOf1425922638) // Client portfolio
																									// with insufficient
																									// balance
			));

	private List<Price> prices;

	@BeforeEach
	void setUp() throws Exception {
		prices = service.getPriceList();
	}

	@AfterEach
	void tearDown() throws Exception {
		service = null;
	}

	@Test
	public void testGetAllPricesShouldReturn13Prices() {
		assertEquals(13, prices.size(), "Should return 13 instrument prices");
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
	public void testAddTradesToInsertTradeAndOrderTable() {
		String clientId = "1654658069";
		Order order = new Order("NT123456", 10, new BigDecimal("100.00"), "B", clientId, "ORDER100", 123453);
		Trade trade = new Trade(order, new BigDecimal("110.00"), "ORDER100TR", new BigDecimal("1100.00"));
		service.addTrade(trade);
		assertEquals(1, countRowsInTableWhere(testJdbcTemplate, "CLIENT_ORDER", """
					 INSTRUMENT_ID = 'NT123456'
				 and ORDER_ID = 'ORDER100'
				"""));
		assertEquals(1, countRowsInTableWhere(testJdbcTemplate, "CLIENT_TRADE", """
					 TRADE_ID = 'ORDER100TR'
				 and ORDER_ID = 'ORDER100'
				"""));

	}

	@Test
	void testAddTradeThrowsExceptionForExistingOrderId() {
		Order newOrder = new Order("T67890", 10000, new BigDecimal("1.03375"), "S", "541107416", "ORDER002", 4);
		Trade newTrade = new Trade(newOrder, new BigDecimal("1.03375"), "TRADE002", new BigDecimal("10337.5"));
		assertThrows(DatabaseException.class, () -> {
			service.addTrade(newTrade);
		});
	}
	
	/*EXECUTE TRADE*/
	@Test
	 public void testExecuteSuccessfulBuyTradeUpdatesTradeTable() {
		  	String existingClientId = "1425922638";
	    	UUID uuid=UUID.randomUUID();
	    	String orderId = uuid.toString();
	    	Order order = new Order("N123456", 10, new BigDecimal("10.75"), "B", existingClientId, orderId, 123);
	    	Trade trade = service.executeTrade(order);
	    	assertEquals(1, countRowsInTableWhere(testJdbcTemplate, "CLIENT_TRADE", "TRADE_ID = '"+trade.getTradeId()+"' and ORDER_ID = '"+orderId+"'"));
	 }
	
	@Test
	 public void testExecuteSuccessfulSellTradeUpdatesTradeTable() {
		  	String existingClientId = "541107416";
	    	UUID uuid=UUID.randomUUID();
	    	String orderId = uuid.toString();
	    	Order order = new Order("C100", 10, new BigDecimal("104.75"), "S", existingClientId, orderId, 123);
	    	Trade trade = service.executeTrade(order);
	    	assertEquals(1, countRowsInTableWhere(testJdbcTemplate, "CLIENT_TRADE", "TRADE_ID = '"+trade.getTradeId()+"' and ORDER_ID = '"+orderId+"'"));
	 }
	
	@Test
	 public void testExecuteSellTradeForNonExistentHoldingThrowsException() {
		  	String existingClientId = "1425922638";
	    	UUID uuid=UUID.randomUUID();
	    	String orderId = uuid.toString();
	    	Order order = new Order("C100", 10, new BigDecimal("104.75"), "S", existingClientId, orderId, 123);
	    	Exception e = assertThrows(IllegalArgumentException.class, () -> {
				service.executeTrade(order);
			});
	    	assertEquals(e.getMessage(),"Instrument not part of holdings! Cannot sell the instrument");
	    	
	 }

}
