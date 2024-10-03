package com.marshals.integration;
 
import static org.junit.jupiter.api.Assertions.*;
 
 
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
 
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTableWhere;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
 
import com.marshals.integration.ClientTradeDao;
import com.marshals.integration.ClientTradeDaoImpl;
import com.marshals.integration.DatabaseException;
import com.marshals.integration.TransactionManager;
import com.marshals.integration.mapper.ClientTradeMapper;
import com.marshals.models.ClientPortfolio;
import com.marshals.models.Holding;
import com.marshals.models.Order;
import com.marshals.models.Trade;
import com.marshals.models.TradeHistory;
 
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:beans.xml")
@Transactional
class ClientTradeDaoImplTest {
	@Autowired
	private Logger logger;
	@Autowired
	@Qualifier("clientTradeDao")
	private ClientTradeDao dao;
	@Autowired
	@Qualifier("testJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
 
	@Test
	void verifyDaoNotNull() {
		assertNotNull(dao);
	}

	/*GET CLIENT PORTFOLIO*/
	@Test
	void testGetClientPortfolioNotNull() {
		String clientId = "541107416";
		ClientPortfolio clientPortfolio = dao.getClientPortfolio(clientId);
		assertNotNull(clientPortfolio);
	}
	@Test
	void testGetClientPortfolioHoldingsForClientWithoutHoldings() {
		String clientId = "1425922638";
		ClientPortfolio clientPortfolio = dao.getClientPortfolio(clientId);
		assertTrue(clientPortfolio.getHoldings().size() == 0);
	}
	@Test
	void testGetClientPortfolioHoldingsForClientWithHoldings() {
		String clientId = "541107416";
		ClientPortfolio clientPortfolio = dao.getClientPortfolio(clientId);
		assertTrue(clientPortfolio.getHoldings().size() >= 1);
	}
	@Test
	void testGetClientPortfolioThrowsExceptionForInvalidClientId() {
		String clientId = "nonExistingClientId";
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.getClientPortfolio(clientId);
		});
		assertEquals("Client ID does not exist", e.getMessage());
	}
	/*UPDATE CLIENT PORTFOLIO*/
	//Updating current balance
	@Test
	void testSuccessfulUpdationOfClientBalance() throws SQLException {
		String clientId = "541107416"; //Existing client
		BigDecimal currBalance = new BigDecimal("10453").setScale(4);
		String whereCondition = "client_id = '541107416' and curr_balance = "+currBalance;
		dao.updateClientBalance(clientId, currBalance);
		int newSize = countRowsInTableWhere(jdbcTemplate, "client", whereCondition);
		assertTrue(newSize == 1);
	}
	
//	Get Client Trade History Tests
	@Test
	void testGetClientTradeHistoryNotNull() {
		String clientId = "541107416";
		TradeHistory tradeHistory = dao.getClientTradeHistory(clientId);
		assertNotNull(tradeHistory);
	}
	@Test
	void testGetClientTradeHistory() {
		String clientId = "541107416";
		TradeHistory tradeHistory = dao.getClientTradeHistory(clientId);
		assertTrue(tradeHistory.getTrades().size()>1);
	}
	@Test
	void testGetClientTradeHistoryFirstTrade() {
		String clientId = "541107416";
		TradeHistory tradeHistory = dao.getClientTradeHistory(clientId);
		assertNotNull(tradeHistory);
		for(Trade t: tradeHistory.getTrades()) {
			System.out.println("Trades -> " + t);
		}
		assertEquals(tradeHistory.getTrades().get(0).getInstrumentId(), "T67890");
	}
	@Test
	void testGetClientTradeHistoryThrowsExceptionForInvalidClientId() {
		String clientId = "nonExistingClientId";
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.getClientTradeHistory(clientId);
		});
		assertEquals("Client ID does not exist", e.getMessage());
	}
//	Add Trade tests
	@Test
	void testAddTradeCheckIfBothOrderAndTradeTablesAreUpdated() throws SQLException {
		var rowCount = countRowsInTable(jdbcTemplate, "client_trade");
		// new order and trade to be inserted into the database
		Order newOrder = new Order("NT123456", 1000, new BigDecimal(45.678), "B", "739982664", "ORDER020", 20 );
		Trade newTrade = new Trade(newOrder, new BigDecimal(45.678), "TRADE020", new BigDecimal(45678));
		dao.addTrade(newTrade);
		// verify that the number of department rows increased by 1
		assertEquals(rowCount + 1, countRowsInTable(jdbcTemplate, "client_order"));
		assertEquals(rowCount + 1, countRowsInTable(jdbcTemplate, "client_trade"));
		// Verify that all Department properties were inserted correctly.
		assertEquals(1, countRowsInTableWhere(jdbcTemplate, "CLIENT_ORDER", """
								 INSTRUMENT_ID = 'NT123456'
							 and ORDER_ID = 'ORDER020'
						 """));
		assertEquals(1, countRowsInTableWhere(jdbcTemplate, "CLIENT_TRADE", """
				 TRADE_ID = 'TRADE020'
		 """));
	}
	@Test
	void testAddTradeThrowsExceptionForExistingOrderId() {
		Order newOrder = new Order("T67890", 10000, new BigDecimal("1.03375"), "S", "541107416", "ORDER002", 4 );
		Trade newTrade = new Trade(newOrder, new BigDecimal("1.03375"), "TRADE002", new BigDecimal("10337.5"));
		assertThrows(DatabaseException.class, ()->{
			dao.addTrade(newTrade);
		});
	}
	

	
	//Adding holdings
	@Test
	void testSuccessfulAddClientHoldingOfClientWithHolding() throws SQLException {
		String clientId = "541107416"; //Has holdings
		Holding holding = new Holding("N123456", 1, new BigDecimal("104.50"));
		int oldSize =  countRowsInTableWhere(jdbcTemplate, "holdings", "client_id = "+clientId);
		dao.addClientHoldings(clientId, holding);
		int newSize =  countRowsInTableWhere(jdbcTemplate, "holdings", "client_id = "+clientId);
		assertTrue(newSize == oldSize+1);
	}
	@Test
	void testSuccessfulAddClientHoldingOfClientWithNoHoldings() throws SQLException {
		String clientId = "1654658069"; //Has no holdings
		Holding holding = new Holding("N123456", 1, new BigDecimal("104.50"));
		dao.addClientHoldings(clientId, holding);
		int newSize =  countRowsInTableWhere(jdbcTemplate, "holdings", """
				client_id = '1654658069'
				""");
		assertTrue(newSize == 1);
	}
	@Test
	void testAddClientHoldingOfNonExistentClientThrowsException() throws SQLException {
		String clientId = "nonexistent";
		Holding holding = new Holding("N123456", 1, new BigDecimal("104.50"));
		assertThrows(DatabaseException.class, ()->{
			dao.addClientHoldings(clientId, holding);
		});
	}
	//Updating existing holdings
	@Test
	void testSuccessfulUpdationOfClientHoldings() throws SQLException {
		String clientId = "541107416"; //Has holdings
		Holding holding = new Holding("C100", 10, new BigDecimal("104.50"));
 
		dao.updateClientHoldings(clientId, holding);
		int newSize = countRowsInTableWhere(jdbcTemplate, "holdings", """
				 instrument_id = 'C100'
				 and quantity = '10'
		 """);
		assertTrue(newSize == 1);
	}
	@Test
	void testUpdationOfNonExistentClientHoldingsThrowsException() throws SQLException {
		String clientId = "541107416"; //Has holdings
		Holding holding = new Holding("nonexistent", 10, new BigDecimal("104.50"));
		assertThrows(DatabaseException.class, ()->{
			dao.updateClientHoldings(clientId, holding);
		});
	}
	@Test
	void testUpdationOfClientHoldingsOfNonExistentClientThrowsException() throws SQLException {
		String clientId = "nonexistent";
		Holding holding = new Holding("N123456", 10, new BigDecimal("104.50"));
		assertThrows(DatabaseException.class, ()->{
			dao.updateClientHoldings(clientId, holding);
		});
	}
}