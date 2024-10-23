package com.marshals.restcontroller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.hamcrest.Matchers;
import org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.marshals.business.Order;
import com.marshals.business.Trade;
import com.marshals.business.TradeHistory;
import com.marshals.business.services.FMTSService;
import com.marshals.business.services.TradeService;
import com.marshals.integration.DatabaseException;

@WebMvcTest(controllers = { TradeController.class })
class TradeControllerWebLayerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private TradeService mockTradeService;

	@MockBean
	private FMTSService mockFMTSService;

//	Test client trade-history details
	List<Trade> tradeObjectList = List.of(
			new Trade(new Order("N123456", 20, new BigDecimal("1.03375"), "B", "541107416", "ORDER004", 4),
					new BigDecimal("1.035"), "TRADE004", new BigDecimal("20.7")),
			new Trade(new Order("T67894", 50, new BigDecimal("95.92"), "B", "541107416", "ORDER003", 3),
					new BigDecimal("96"), "TRADE003", new BigDecimal("4800")),
			new Trade(new Order("T67890", 10000, new BigDecimal("1.03375"), "S", "541107416", "ORDER002", 4),
					new BigDecimal("1.03375"), "TRADE002", new BigDecimal("10337.5")));

	TradeHistory clientTradeHistoryList = new TradeHistory("541107416", tradeObjectList);

	// Smoke Test for Mock MVC object
	@Test
	void testForMockMvcInstantiationForTradeController() {
		assertNotNull(mockMvc);
	}

//		Test for GET mapping for client trade-history
	@Test
	void testForGetClientTradeHistoryRespond200() throws Exception {
		String id = "541107416";
		when(mockTradeService.getClientTradeHistory(id)).thenReturn(clientTradeHistoryList);

		String expectedFirstTradeJson = """
					   	{
					"instrumentId": "N123456",
					"quantity": 20,
					"executionPrice": 1.035,
					"direction": "B",
					"clientId": "541107416",
					"order": {
						"instrumentId": "N123456",
						"quantity": 20,
						"targetPrice": 1.03375,
						"direction": "B",
						"clientId": "541107416",
						"orderId": "ORDER004",
						"token": 4
					},
					"tradeId": "TRADE004",
					"cashValue": 20.7
				}
					    """;

		mockMvc.perform(get("/trade/trade-history/" + id)).andExpect(status().isOk())
				.andExpect(jsonPath("$.clientId").value(id)).andExpect(jsonPath("$.trades").isArray())
				.andExpect(jsonPath("$.trades.length()").value(3)) // Change to the expected number of trades
				.andExpect(jsonPath("$.trades[0].instrumentId").value("N123456"))
				.andExpect(jsonPath("$.trades[1].executionPrice").value(96))
				.andExpect(jsonPath("$.trades[2].direction").value("S"))
				.andExpect(jsonPath("$.trades[2].cashValue").value(10337.5));
//				.andExpect(content().json(expectedFirstTradeJson));

	}

	@Test
	void testForGetClientTradeHistoryRespond204() throws Exception {
		String id = "12345678";

		when(mockTradeService.getClientTradeHistory(id)).thenThrow(DatabaseException.class);

		mockMvc.perform(get("/trade/trade-history/" + id)).andExpect(status().isBadRequest())
				.andExpect(content().string(is(emptyOrNullString())));
	}

	@Test
	void testForGetClientTradeHistoryRespond400ForRandomClientIdString() throws Exception {
		String id = "invalid-cliend-id";

		when(mockTradeService.getClientTradeHistory(id)).thenThrow(IllegalArgumentException.class);

		mockMvc.perform(get("/trade/trade-history/" + id)).andExpect(status().isNotAcceptable())
				.andExpect(content().string(is(emptyOrNullString())));
		;
	}
	
	@Test
	void testForGetClientTradeHistoryRespond406ForInvalidClientId() throws Exception {
		String id = "-10000";
		when(mockTradeService.getClientTradeHistory(id)).thenThrow(IllegalArgumentException.class);
		
		mockMvc.perform(get("/trade/trade-history/" + id))
			.andExpect(status().isNotAcceptable())
			.andExpect(content().string(is(emptyOrNullString())));;
	}

	@Test
	void testForGetClientTradeHistoryRespond500() throws Exception {
		String id = "541107416";
		when(mockTradeService.getClientTradeHistory(id)).thenThrow(RuntimeException.class);
		
		mockMvc.perform(get("/trade/trade-history/" + id))
			.andExpect(status().isInternalServerError())
			.andExpect(content().string(is(emptyOrNullString())));;
	}
	
	@Test
	void testExecuteTradeSuccess() throws Exception {
	    // Arrange
	    Order order = new Order("ORDER9876", 10, new BigDecimal("104.75"), "B", "541107416", "N123456", 540983960);
	    Trade expectedTrade = new Trade(
	        order,
	        new BigDecimal("104.75"), // executionPrice
	        "f5ffmsn2rep-etjqfvr7an4-2g1137sdpvf", // tradeId
	        new BigDecimal("1057.975") // cashValue
	    );
 
	    when(mockTradeService.executeTrade(order)).thenReturn(expectedTrade);
 
	    // Act & Assert
	    mockMvc.perform(post("/trade/execute-trade")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("""
	                {
	                    "orderId": "ORDER9876",
	                    "quantity": 10,
	                    "targetPrice": 104.75,
	                    "direction": "B",
	                    "clientId": "541107416",
	                    "instrumentId": "N123456",
	                    "token": 540983960
	                }
	            """))
	            .andExpect(status().isOk())
	            .andDo(print());
	}
}