package com.fidelity.services;
 
import static org.junit.jupiter.api.Assertions.*;
 
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
 
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
 
import com.fidelity.integration.ClientActivityReportDao;
import com.fidelity.integration.ClientDao;
import com.fidelity.integration.DatabaseException;
import com.fidelity.models.Holding;
import com.fidelity.models.Order;
import com.fidelity.models.Trade;
 
class ActivityReportServiceTest {
	@Mock ClientActivityReportDao mockDao;
	@InjectMocks TradeHistoryService tradeHistoryService;
	@InjectMocks ActivityReportService service;

	Holding holding1 = new Holding("C100", 1000, new BigDecimal("95.67"));
	Holding holding2 = new Holding("T67890", 10, new BigDecimal("1.0338"));
	List<Holding> mockHoldings = new ArrayList<Holding>(
				List.of(
					holding1,
					holding2
				)
			);
 
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
	}
 
	@AfterEach
	void tearDown() throws Exception {
	}
	@Test
	void testGenerateClientHoldings() {
		String clientId = "541107416";
		Mockito.when(mockDao.getClientHoldings(clientId))
			.thenReturn(mockHoldings);
		service.generateHoldingsReport(clientId);
		Mockito.verify(mockDao).getClientHoldings(clientId);
	}
	@Test
	void testGenerateClientHoldingsShouldHandleNullClientId() {
		assertThrows(NullPointerException.class, () -> {
			service.generateHoldingsReport(null);
		});
	}
 
	@Test
	void testGenerateClientHoldingShouldHandleForNonExistentClientId() {
		String clientId = "541107416";
		Mockito.doThrow(new DatabaseException("Client has no holdings")).when(mockDao).getClientHoldings(clientId);
		Exception e = assertThrows(DatabaseException.class, () -> {
			service.generateHoldingsReport(clientId);
			//Verifying that the corresponding mockDao methods were called
			Mockito.verify(mockDao).getClientHoldings(clientId);
		});
		assertEquals(e.getMessage(), "Client has no holdings");
	}
}