package com.marshals.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTableWhere;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

import com.marshals.models.ClientPortfolio;
import com.marshals.models.Order;
import com.marshals.models.Trade;
import com.marshals.models.TradeHistory;
import com.marshals.services.PortfolioService;
import com.marshals.services.TradeHistoryService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:beans.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class TradeHistoryServiceDaoIntegrationTest {

	@Autowired
	private TradeHistoryService service;

	@Autowired
	private JdbcTemplate testJdbcTemplate;

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	public void testGetClientTradeHistory() {
		String clientId = "541107416";

		TradeHistory actualTradeHistory = service.getClientTradeHistory(clientId);
		assertTrue(actualTradeHistory.getTrades().size() >= 1);
	}

	@Test
	public void testGetClientTradeHistoryThrowsNullPointerException() {
		Exception e = assertThrows(NullPointerException.class, () -> service.getClientTradeHistory(null));
		assertEquals(e.getMessage(), "Client ID must not be null");
	}

	@Test
	public void testGetClientTradeHistoryThrowsExceptionForNonExistingClient() {
		String clientId = "nonexistentClient";
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.getClientTradeHistory(clientId);
		});
		assertEquals(e.getMessage(), "Client ID does not exist");
	}

	@Test
	public void testGetTradesByClientIdShouldNotReturnMoreThan100Trades() {
		String clientId = "541107416";
		TradeHistory actualTradeHistory = service.getClientTradeHistory(clientId);
		assertTrue(actualTradeHistory.getTrades().size() <= 100);
	}

}
