package com.marshals.business;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FMTSValidatedClientTest {
	
	private FMTSValidatedClient validatedClient;

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		validatedClient = null;
	}

	@Test
	void testForEqualsValidatedClientObject() {
		validatedClient = new FMTSValidatedClient("john.doe@gmail.com","1234567890", new BigDecimal("207156590").setScale(0) );
		assertEquals(validatedClient, new FMTSValidatedClient("john.doe@gmail.com","1234567890", new BigDecimal("207156590").setScale(0) ) , 
				"Validated Client details should be equal");
	}
	
	@Test
	void testForNotEqualsOfValidatedClientObjectWithDiffTokens() {
		validatedClient = new FMTSValidatedClient("john.doe@gmail.com","1234567890", new BigDecimal("207156590").setScale(0) );
		assertNotEquals(validatedClient, new FMTSValidatedClient("john.doe@gmail.com","1234567890", new BigDecimal("123456789").setScale(0) ) , 
				"Validated Client details should not be equal");
	}
	
	//NullPointerException
	@Test
	void testNullClientIDInitialization() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			validatedClient = new FMTSValidatedClient("john.doe@gmail.com",null, new BigDecimal("207156590").setScale(0) );
		});
		assertEquals("Validated Client Details cannot be null",e.getMessage());
	}
	

}
