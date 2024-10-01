package com.marshals.fmts;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.marshals.fmts.ValidatedClient;

class ValidatedClientTest {
	
	private ValidatedClient validatedClient;

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		validatedClient = null;
	}

	@Test
	void testForEqualsValidatedClientObject() {
		validatedClient = new ValidatedClient("john.doe@gmail.com","1234567890", new BigDecimal("207156590").setScale(0) );
		assertEquals(validatedClient, new ValidatedClient("john.doe@gmail.com","1234567890", new BigDecimal("207156590").setScale(0) ) , 
				"Validated Client details should be equal");
	}
	
	@Test
	void testForNotEqualsOfValidatedClientObjectWithDiffTokens() {
		validatedClient = new ValidatedClient("john.doe@gmail.com","1234567890", new BigDecimal("207156590").setScale(0) );
		assertNotEquals(validatedClient, new ValidatedClient("john.doe@gmail.com","1234567890", new BigDecimal("123456789").setScale(0) ) , 
				"Validated Client details should not be equal");
	}
	
	//NullPointerException
	@Test
	void testNullClientIDInitialization() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			validatedClient = new ValidatedClient("john.doe@gmail.com",null, new BigDecimal("207156590").setScale(0) );
		});
		assertEquals("Validated Client Details cannot be null",e.getMessage());
	}
	

}
