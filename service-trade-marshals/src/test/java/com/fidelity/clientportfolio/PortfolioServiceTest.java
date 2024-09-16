package com.fidelity.clientportfolio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fidelity.client.Client;
import com.fidelity.client.ClientIdentification;
import com.fidelity.client.ClientPreferences;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class PortfolioServiceTest {

	 private PortfolioService portfolioService;
	 private List<ClientPortfolio> clientPortfolios;

    @BeforeEach
    public void setUp() {
		
    	clientPortfolios = new ArrayList<ClientPortfolio>(
				List.of(
						new ClientPortfolio("1425922638", new BigDecimal("1000"), null),
						new ClientPortfolio("1425922634", new BigDecimal("2000"), null)
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
    public void testUpdateClientPortfolio() {
    	
    }
    
    @Test
	void shouldNotUpdateForNullObject() {
		assertThrows(NullPointerException.class, () -> {
			portfolioService.updateClientPortfolio("1", null);
		});
	}
    
    @Test
   	void shouldNotGetForNullObject() {
   		assertThrows(NullPointerException.class, () -> {
   			portfolioService.getClientPortfolio(null);
   		});
   	}
    
}

