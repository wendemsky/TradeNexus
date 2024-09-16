package com.fidelity.trade;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TradeServiceTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		//fail("Not yet implemented");
	}

    private final TradeService tradeService = new TradeService();

    @Test
    void testExecuteTrade() {
        Order order = new Order(
            "instr1", 10, new BigDecimal("100.00"), "BUY", "client1", "order1", 12345
        );

        Trade trade = tradeService.executeTrade(order);

        assertNotNull(trade);
        assertEquals("N123456", trade.getInstrumentId());
        assertEquals(10, trade.getQuantity());
        assertEquals(new BigDecimal("104.25"), trade.getExecutionPrice());
        assertEquals("S", trade.getDirection());
        assertEquals("541107416", trade.getClientId());
        assertEquals(order, trade.getOrder());
        assertEquals("aw6rqg2ee1q-pn1jh9yhg3s-ea6xxmv06bj", trade.getTradeId());
        assertEquals(new BigDecimal("1052.925"), trade.getCashValue());
    }
    
    @Test
    void testExecuteTrade_throwsExceptionForNullOrder() {
        Order order = null;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
        	tradeService.executeTrade(order);
        });
        assertEquals("order cannot be null", thrown.getMessage());
    }

}