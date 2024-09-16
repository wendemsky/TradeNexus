package com.fidelity.clientportfolio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PortfolioServiceTest {

	 private PortfolioService portfolioService;

    @BeforeEach
    public void setUp() {
    	portfolioService = new PortfolioService();
    }
    
	@AfterEach
	void tearDown() throws Exception {
		portfolioService = null;
	}

    @Test
    public void testGetClientPortfolio() {
        String clientId = "123";
        ClientPortfolio mockPortfolio = new ClientPortfolio(clientId, 1000.0, null);

        portfolioService.setMockPortfolio(mockPortfolio);
        ClientPortfolio result = portfolioService.getClientPortfolio(clientId);
        assertEquals(clientId, result.getClientId());
        assertEquals(1000.0, result.getCurrBalance());
    }

    @Test
    public void testUpdateClientPortfolio() {
        ClientPortfolio updatedPortfolio = new ClientPortfolio("123", 1500.0, null);

        portfolioService.setMockPortfolio(updatedPortfolio);

        ClientPortfolio result = portfolioService.updateClientPortfolio(updatedPortfolio);
        assertEquals(1500.0, result.getCurrBalance());
    }
    
    @Test
    public void testGetClientPortfolioThrowsNullPointerException() {
        
    	Exception e = assertThrows(NullPointerException.class, () -> portfolioService.getClientPortfolio(null));
        
        assertEquals(e.getMessage(),"Client ID must not be null");
	}
    
    @Test
    public void testUpdateClientPortfolioThrowsNullPointerException() {
        
    	Exception e = assertThrows(NullPointerException.class, () -> portfolioService.updateClientPortfolio(null));
        
        assertEquals(e.getMessage(),"Client portfolio must not be null");
	}
    
}

