package com.fidelity.clientportfolio;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientPortfolioTest {
	
	ClientPortfolio clientPortfolio;
	
	List<Holding> holdings = new ArrayList<Holding>(List.of(
				new Holding("STOCK","Q123","Alphabet",2,new BigDecimal("105")),
				new Holding("GOVT","Q456","USA Bond 3",1,new BigDecimal("340"))
			));

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		clientPortfolio = null;
	}

	@Test
	void testForEqualsClientPortfolioObject() {
		clientPortfolio = new ClientPortfolio("739982664", new BigDecimal("10000"), holdings);
		assertEquals(clientPortfolio,new ClientPortfolio("739982664", new BigDecimal("10000"), holdings) , "Client Portfolio details should be equal");
	}
	
	@Test
	void testForNotEqualsOfClientPortfolioObjectWithDiffClientIDs() {
		clientPortfolio = new ClientPortfolio("739982664", new BigDecimal("10000"), holdings);
		assertNotEquals(clientPortfolio,new ClientPortfolio("1234567", new BigDecimal("10000"), holdings) , "Client Portfolio details should be equal");
	}
	
	@Test
	void testForNotEqualsOfClientPortfolioObjectWithDiffHoldings() {
		clientPortfolio = new ClientPortfolio("739982664", new BigDecimal("10000"), holdings);
		assertNotEquals(clientPortfolio,new ClientPortfolio("739982664", new BigDecimal("10000"), new ArrayList<Holding>(List.of((holdings.get(0))))) , "Client Portfolio details should not be equal");
	}
	
	@Test
	void testForNotEqualsOfClientPortfolioObjectWithDiffHoldingQuantities() {
		clientPortfolio = new ClientPortfolio("739982664", new BigDecimal("10000"), holdings);
		assertNotEquals(clientPortfolio,new ClientPortfolio("739982664", new BigDecimal("10000"), 
				new ArrayList<Holding>(List.of(
						new Holding("STOCK","Q123","Alphabet",1,new BigDecimal("100")),
						new Holding("GOVT","Q456","USA Bond 3",2,new BigDecimal("340"))))) , "Client Portfolio details should not be equal");
	}
	
	//NullPointerException
	@Test
	void testNullClientIDInitialization() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			clientPortfolio = new ClientPortfolio(null, new BigDecimal("10000"), holdings);
		});
		assertEquals("Client Portfolio Details cannot be null",e.getMessage());
	}

}
