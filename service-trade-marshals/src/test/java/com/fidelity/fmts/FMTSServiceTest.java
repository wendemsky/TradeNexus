package com.fidelity.fmts;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fidelity.models.Order;
import com.fidelity.models.Trade;

class FMTSServiceTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	void testForNonValidatationOfNullClientAtRegistration() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			ValidatedClient validatedClient = FMTSService.verifyClient(null);
		});
		assertEquals("Client Email cannot be null",e.getMessage());
	}
	
	@Test
	void testForNonValidatationOfNullClientAtLogin() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			ValidatedClient validatedClient = FMTSService.verifyClient(null,null);
		});
		assertEquals("Client Details cannot be null",e.getMessage());
	}
	
	@Test
	void testForValidatationOfNewClientAtRegistration() {
		ValidatedClient validatedClient = FMTSService.verifyClient("john.doe@gmail.com");
		ValidatedClient expectedValidatedClient = new ValidatedClient("john.doe@gmail.com","739982664",new BigDecimal("739859208").setScale(0));
		assertEquals(validatedClient, expectedValidatedClient, "New Client should be validated");	
	}
	
	@Test
	void testForValidatationOfExistingClientAtLogin() {
		ValidatedClient validatedClient = FMTSService.verifyClient("john.doe@gmail.com","739982664");
		ValidatedClient expectedValidatedClient = new ValidatedClient("john.doe@gmail.com","739982664",new BigDecimal("739859208").setScale(0));
		assertEquals(validatedClient, expectedValidatedClient, "Existing Client should be validated");	
	}
	
	 @Test
    public void testCreateTradeValidOrder() {
        Order order = new Order("N123456", 10, new BigDecimal("104.75"), "B", "client1", "order1", 123);
        Trade trade = FMTSService.createTrade(order);

        assertNotNull(trade);
        assertEquals(order.getInstrumentId(), trade.getInstrumentId());
        assertEquals(order,trade.getOrder());
    }

    @Test
    public void testShouldNotCreateTradeForNullOrder() {
        assertThrows(NullPointerException.class, () -> FMTSService.createTrade(null), "order cannot be null");
    }

}
