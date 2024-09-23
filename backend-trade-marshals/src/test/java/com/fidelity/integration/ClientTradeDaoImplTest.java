package com.fidelity.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fidelity.models.ClientPortfolio;
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

	@Test
	void testGetClientPortfolioNotNull() {
		String clientId = "541107416";
		ClientPortfolio clientPortfolio = dao.getClientPortfolio(clientId);
		assertNotNull(clientPortfolio);
	}
	
	@Test
	void testGetClientPortfolioHoldings() {
		String clientId = "541107416";
		ClientPortfolio clientPortfolio = dao.getClientPortfolio(clientId);
		assertTrue(clientPortfolio.getHoldings().size() > 4);
	}
	
	@Test
	void testGetClientPortfolioThrowsExceptionForInvalidClientId() {
		String clientId = "nonExistingClientId";
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.getClientPortfolio(clientId);
		});
		assertEquals("Client ID does not exist", e.getMessage());
	}
	
	@Test
	void testGetClientTradeHistory() {
		String clientId = "1654658069";
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
		Order newOrder = new Order("NT123456", 1000, new BigDecimal(45.678), "B", "739982664", "ORDER010", 20 );
		Trade newTrade = new Trade(newOrder, new BigDecimal(45.678), "TRADE010", new BigDecimal(45678));
 
		assertThrows(DatabaseException.class, ()->{
			dao.addTrade(newTrade);
		});
	}
	
	

}
