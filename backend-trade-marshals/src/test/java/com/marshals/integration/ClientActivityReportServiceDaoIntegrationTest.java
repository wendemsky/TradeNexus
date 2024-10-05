package com.marshals.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import com.marshals.models.Holding;
import com.marshals.services.ActivityReportService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:beans.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class ClientActivityReportServiceDaoIntegrationTest {
	
	@Autowired
	private Logger logger;
	
	@Autowired
	private ActivityReportService service;
	
	@Autowired
	@Qualifier("testJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Test
	void testGetClientHoldingsForClientWithHoldings() {
		String clientId = "541107416";
		List<Holding> holdings = service.generateHoldingsReport(clientId);
		assertTrue(holdings.size() >= 1);
	}

	@Test
	void testGetClientHoldingsForClientWithNoHoldings() {
		String clientId = "1654658069";
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.generateHoldingsReport(clientId);
		});
		assertEquals(e.getMessage(), "client has no holdings");
	}

	@Test
	void testGetClientHoldingsThrowsExceptionForNonExistingClientID() {
		String clientId = "nonExistingClientId";
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.generateHoldingsReport(clientId);
		});
	}

	@Test
	void testGetClientHoldingsThrowsNullPointerExceptionForNullClientId() {
		String clientId = null;
		Exception e = assertThrows(NullPointerException.class, () -> {
			service.generateHoldingsReport(clientId);
		});
	}
}
