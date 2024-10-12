package com.marshals.business.services;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.marshals.business.FMTSValidatedClient;
import com.marshals.business.Order;
import com.marshals.business.Trade;

@SpringBootTest
class FMTSServicePOJOUnitTest {

	@Autowired
	private FMTSService fmtsService;
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	void testForNonValidatationOfNullClientAtRegistration() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			FMTSValidatedClient validatedClient = fmtsService.verifyClient(null);
		});
		assertEquals("Client Email cannot be null",e.getMessage());
	}
	
	@Test
	void testForNonValidatationOfNullClientAtLogin() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			FMTSValidatedClient validatedClient = fmtsService.verifyClient(null,null);
		});
		assertEquals("Client Details cannot be null",e.getMessage());
	}
	
	@Test
	void testForValidatationOfNewClientAtRegistration() {
		FMTSValidatedClient validatedClient = fmtsService.verifyClient("john.doe@gmail.com");
		FMTSValidatedClient expectedValidatedClient = new FMTSValidatedClient("john.doe@gmail.com","739982664",new BigDecimal("739859208").setScale(0));
		assertEquals(validatedClient, expectedValidatedClient, "New Client should be validated");	
	}
	
	@Test
	void testForValidatationOfExistingClientAtLogin() {
		FMTSValidatedClient validatedClient = fmtsService.verifyClient("john.doe@gmail.com","739982664");
		FMTSValidatedClient expectedValidatedClient = new FMTSValidatedClient("john.doe@gmail.com","739982664",new BigDecimal("739859208").setScale(0));
		assertEquals(validatedClient, expectedValidatedClient, "Existing Client should be validated");	
	}
	
	 @Test
    public void testCreateTradeValidOrder() {
        Order order = new Order("N123456", 10, new BigDecimal("104.75"), "B", "client1", "order1", 123);
        Trade trade = fmtsService.createTrade(order);

        assertNotNull(trade);
        assertEquals(order.getInstrumentId(), trade.getInstrumentId());
        assertEquals(order,trade.getOrder());
    }

    @Test
    public void testShouldNotCreateTradeForNullOrder() {
        assertThrows(NullPointerException.class, () -> fmtsService.createTrade(null), "order cannot be null");
    }

}
