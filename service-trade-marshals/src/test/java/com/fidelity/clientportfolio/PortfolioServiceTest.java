package com.fidelity.clientportfolio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortfolioServiceTest {
	
	private PortfolioService portfolioService;
	private List<ClientPortfolio> clientPortfolios;

	@BeforeEach
	void setUp() throws Exception {
		
		List<Holding> holdings = new ArrayList<Holding>(List.of(
				new Holding("Q123",2,new BigDecimal("105")),
				new Holding("Q456",1,new BigDecimal("340"))
			));
		clientPortfolios = new ArrayList<ClientPortfolio>(
				List.of(
						new ClientPortfolio("1425922638", new BigDecimal("1000"), holdings),
						new ClientPortfolio("1425922634", new BigDecimal("2000"), holdings)
				)
			);
    	portfolioService = new PortfolioService();
    	portfolioService.addClientPortfolio(clientPortfolios.get(0));
    	portfolioService.addClientPortfolio(clientPortfolios.get(1));
	}

	@AfterEach
	void tearDown() throws Exception {
		portfolioService = null;
		clientPortfolios = null;
	}

	@Test
    public void testGetClientPortfolio() {
    	ClientPortfolio clientPortfolio =  portfolioService.getClientPortfolio("1425922638");
    	ClientPortfolio expected = clientPortfolios.get(0);
		assertEquals(clientPortfolio.equals(expected), true);
    }
	
	@Test
	void shouldNotUpdateForNullObject() {
		assertThrows(NullPointerException.class, () -> {
			portfolioService.updateClientPortfolio(null);
		});
	}
    @Test
   	void shouldNotGetForNullObject() {
   		assertThrows(NullPointerException.class, () -> {
   			portfolioService.getClientPortfolio(null);
   		});
   	}

}
