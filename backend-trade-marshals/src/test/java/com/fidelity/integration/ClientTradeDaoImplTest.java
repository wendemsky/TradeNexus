package com.fidelity.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.Holding;
import com.fidelity.models.Order;
import com.fidelity.models.Trade;
import com.fidelity.models.TradeHistory;

class ClientTradeDaoImplTest {
	
	static PoolableDataSource dataSource;
	ClientTradeDao dao;
	TransactionManager transactionManager;
	Connection connection = null;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		dataSource = new PoolableDataSource();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		dataSource.shutdown();
	}

	@BeforeEach
	void setUp() throws Exception {
		dao = new ClientTradeDaoImpl(dataSource);
		transactionManager = new TransactionManager(dataSource);
		transactionManager.startTransaction();
		
		connection = dataSource.getConnection();
	}

	@AfterEach
	void tearDown() throws Exception {
		transactionManager.rollbackTransaction();
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
	
	/*TRADES*/
	@Test
	void testGetClientTradeHistory() {
		String clientId = "541107416";
		TradeHistory tradeHistory = dao.getClientTradeHistory(clientId);
		assertNotNull(tradeHistory);
		assertTrue(tradeHistory.getTrades().size()>1);
	}
	
	@Test
	void testAddTrade() throws SQLException {
		Order newOrder = new Order("NT123456", 1000, new BigDecimal(45.678), "B", "739982664", "ORDER020", 20 );
		
		Trade newTrade = new Trade(newOrder, new BigDecimal(45.678), "TRADE020", new BigDecimal(45678));
		int oldSize = DbTestUtils.countRowsInTable(dataSource.getConnection(), "CLIENT_TRADE");
		dao.addTrade(newTrade);
		int newSize = DbTestUtils.countRowsInTable(dataSource.getConnection(), "CLIENT_TRADE");
		assertEquals(newSize, oldSize+1);
	}
	@Test
	void testAddTradeExistingOrderId() {
		Order newOrder = new Order("NT123456", 1000, new BigDecimal(45.678), "B", "739982664", "ORDER001", 20 );
		Trade newTrade = new Trade(newOrder, new BigDecimal(45.678), "TRADE010", new BigDecimal(45678));
 
		assertThrows(DatabaseException.class, ()->{
			dao.addTrade(newTrade);
		});
	}
	
	
	/*UPDATE CLIENT PORTFOLIO*/
	//Updating current balance
	@Test
	void testSuccessfulUpdationOfClientBalance() throws SQLException {
		String clientId = "541107416"; //Existing client
		BigDecimal currBalance = new BigDecimal("10453").setScale(4);
		String whereCondition = "client_id = '541107416' and curr_balance = "+currBalance;
		dao.updateClientBalance(clientId, currBalance);
		int newSize =  DbTestUtils.countRowsInTableWhere(dataSource.getConnection(), "client", whereCondition);
		assertTrue(newSize == 1);
	}
	
	@Test
	void testUpdationOfBalanceOfNonExistentClientThrowsException() throws SQLException {
		String clientId = "nonexistent";
		assertThrows(DatabaseException.class, ()->{
			dao.updateClientBalance(clientId, new BigDecimal("10653"));
		});
	}
	
	//Adding holdings
	@Test
	void testSuccessfulAddClientHoldingOfClientWithHolding() throws SQLException {
		String clientId = "541107416"; //Has holdings
		Holding holding = new Holding("N123456", 1, new BigDecimal("104.50"));
		int oldSize =  DbTestUtils.countRowsInTableWhere(dataSource.getConnection(), "holdings", "client_id = "+clientId);
		dao.addClientHoldings(clientId, holding);
		int newSize =  DbTestUtils.countRowsInTableWhere(dataSource.getConnection(), "holdings", "client_id = "+clientId);
		assertTrue(newSize == oldSize+1);
	}
	
	@Test
	void testSuccessfulAddClientHoldingOfClientWithNoHoldings() throws SQLException {
		String clientId = "1654658069"; //Has no holdings
		Holding holding = new Holding("N123456", 1, new BigDecimal("104.50"));
		dao.addClientHoldings(clientId, holding);
		int newSize =  DbTestUtils.countRowsInTableWhere(dataSource.getConnection(), "holdings", "client_id = "+clientId);
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
		String whereCondition = "client_id = '541107416' and instrument_id = 'C100' and quantity = "+holding.getQuantity();
		dao.updateClientHoldings(clientId, holding);
		int newSize =  DbTestUtils.countRowsInTableWhere(dataSource.getConnection(), "holdings", whereCondition);
		assertTrue(newSize == 1);
	}
	
	@Test
	void testUpdationOfNonExistentClientHoldingsThrowsException() throws SQLException {
		String clientId = "541107416"; //Has holdings
		Holding holding = new Holding("N123456", 10, new BigDecimal("104.50"));
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
