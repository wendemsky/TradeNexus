package com.marshals.business.services; 

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import com.marshals.business.FIPSValidatedClient;
import com.marshals.business.Instrument;
import com.marshals.business.Order;
import com.marshals.business.Price;
import com.marshals.business.Trade;
import com.marshals.integration.FIPSDao;
import com.marshals.integration.FIPSException;

class FIPSServicePOJOUnitTest {
	@Mock
	FIPSDao mockDao;

	@Autowired
	@InjectMocks
	private FIPSService service;

	//Mock data for testing
	private List<Price> priceList;
	private List<FIPSValidatedClient> validatedClients;
	private Order order;
	private Trade trade;

	@BeforeEach
	void setUp() throws Exception {
		//Mock validated clients
		validatedClients = new ArrayList<>(List.of(
				new FIPSValidatedClient("1654658070","sam@gmail.com",1654658070), //Register
				new FIPSValidatedClient("1654658069","sowmya@gmail.com",1654658069) //Login
		));
		
		//Mock price list
		priceList = new ArrayList<>();
		priceList.add(new Price(new BigDecimal("104.75"), new BigDecimal("104.25"),
				"21-AUG-19 10.00.01.042000000 AM GMT", new Instrument("N123456", "CUSIP", "46625H100", "STOCK",
						"JPMorgan Chase & Co. Capital Stock", 1000, 1)));

		priceList.add(new Price(new BigDecimal("312500"), new BigDecimal("312000"),
				"21-AUG-19 05.00.00.040000000 AM -05:00",
				new Instrument("N123789", "ISIN", "US0846707026", "STOCK", "Berkshire Hathaway Inc. Class A", 10, 1)));

		priceList.add(new Price(new BigDecimal("95.92"), new BigDecimal("95.42"), "21-AUG-19 10.00.02.042000000 AM GMT",
				new Instrument("C100", "CUSIP", "48123Y5A0", "CD", "JPMorgan Chase Bank, National Association 01/19",
						1000, 100)));

		priceList.add(new Price(new BigDecimal("1.03375"), new BigDecimal("1.03390625"),
				"21-AUG-19 10.00.02.000000000 AM GMT",
				new Instrument("T67890", "CUSIP", "9128285M8", "GOVT", "USA, Note 3.125 15nov2028 10Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("0.998125"), new BigDecimal("0.99828125"),
				"21-AUG-19 10.00.02.002000000 AM GMT",
				new Instrument("T67894", "CUSIP", "9128285Z9", "GOVT", "USA, Note 2.5 31jan2024 5Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("1"), new BigDecimal("1.00015625"),
				"21-AUG-19 10.00.02.002000000 AM GMT",
				new Instrument("T67895", "CUSIP", "9128286A3", "GOVT", "USA, Note 2.625 31jan2026 7Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("0.999375"), new BigDecimal("0.999375"),
				"21-AUG-19 10.00.02.002000000 AM GMT",
				new Instrument("T67897", "CUSIP", "9128285X4", "GOVT", "USA, Note 2.5 31jan2021 2Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("0.999375"), new BigDecimal("0.999375"),
				"21-AUG-19 10.00.02.002000000 AM GMT",
				new Instrument("T67899", "CUSIP", "9128285V8", "GOVT", "USA, Notes 2.5% 15jan2022 3Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("1.00375"), new BigDecimal("1.00375"),
				"21-AUG-19 10.00.02.002000000 AM GMT",
				new Instrument("T67880", "CUSIP", "9128285U0", "GOVT", "USA, Note 1.5 31dec2023 5Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("1.0596875"), new BigDecimal("1.0596875"),
				"21-AUG-19 10.00.02.002000000 AM GMT",
				new Instrument("T67883", "CUSIP", "912810SE9", "GOVT", "USA, Bond 3.375 15nov2048 30Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("0.9853125"), new BigDecimal("0.98546875"),
				"21-AUG-19 10.00.02.002000000 AM GMT",
				new Instrument("T67878", "CUSIP", "912810SD1", "GOVT", "USA, Bond 3 15aug2048 30Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("1162.42"), new BigDecimal("1161.42"),
				"21-AUG-19 06.52.20.350000000 PM AMERICA/NEW_YORK",
				new Instrument("Q123", "CUSIP", "02079K107", "STOCK", "Alphabet Inc. Class C Capital Stock", 1000, 1)));

		priceList.add(new Price(new BigDecimal("323.39"), new BigDecimal("322.89"),
				"21-AUG-19 06.52.20.356000000 PM AMERICA/NEW_YORK",
				new Instrument("Q456", "CUSIP", "88160R101", "STOCK", "Tesla, Inc. Common Stock", 1000, 1)));
		
		//Mock order and trade
		order = new Order("N123456", 10, new BigDecimal("100.00"), "B", "1654658069", "ORDER001", 123);
		trade = new Trade(order, new BigDecimal("110.00"), "ORDER001TR", new BigDecimal("1100.00"));
				
		// Initializing the FIPS Service with a Mock Dao
		MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void tearDown() throws Exception {
		service = null;
		priceList = null;
		validatedClients = null;
		order = null;
		trade = null;
	}

	@Test
	void shouldCreateObject() {
		assertNotNull(service);
	}
	
	/*FIPS Client Validation at Registration*/
	@Test
	void testForValidatationOfNullClientAtRegistrationThrowsException() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			service.verifyClient(null);
		});
		assertEquals("Client Email cannot be null",e.getMessage());
	}
	@Test
	void testForSuccessfulValidatationOfNewClientAtRegistration() {
		String validNewEmail = validatedClients.get(0).getEmail();
		Mockito.when(mockDao.verifyClient(validNewEmail)).thenReturn(validatedClients.get(0)); //Mocking dao to return this
		FIPSValidatedClient validatedClient = service.verifyClient(validNewEmail);
		Mockito.verify(mockDao).verifyClient(validNewEmail);
		assertEquals(validatedClient, validatedClients.get(0), "New Client should be validated");	
	}
	@Test
	void testForInvalidatationOfNewClientAtRegistration() {
		String invalidNewEmail = "invalid-email";
		String errorMessage = "FIPS couldnt validate new Client's Email";
		Mockito.doThrow(new FIPSException(errorMessage)).when(mockDao).verifyClient(invalidNewEmail); //Mocking dao to throw this
		Exception e = assertThrows(FIPSException.class, () -> {
			service.verifyClient(invalidNewEmail);
			Mockito.verify(mockDao).verifyClient(invalidNewEmail); 
		});
		assertEquals(e.getMessage(),errorMessage);
	}
	
	/*FIPS Client Validation at Login*/
	@Test
	void testForValidatationOfNullClientAtLoginThrowsException() {
		Exception e = assertThrows(NullPointerException.class, () -> {
			service.verifyClient(validatedClients.get(0).getEmail(),null);
		});
		assertEquals("Client Details cannot be null",e.getMessage());
	}
	@Test
	void testForSuccessfulValidatationOfClientAtLogin() {
		String validEmail = validatedClients.get(1).getEmail();
		String validClientId = validatedClients.get(1).getClientId();
		Mockito.when(mockDao.verifyClient(validEmail,validClientId)).thenReturn(validatedClients.get(1)); //Mocking dao to return this
		FIPSValidatedClient validatedClient = service.verifyClient(validEmail,validClientId);
		Mockito.verify(mockDao).verifyClient(validEmail,validClientId);
		assertEquals(validatedClient, validatedClients.get(1), "Logging in Client should be validated");	
	}
	@Test
	void testForInvalidatationOfIncorrectClientEmailAtLogin() {
		String invalidNewEmail = "invalid-email";
		Mockito.doThrow(new FIPSException()).when(mockDao).verifyClient(invalidNewEmail,validatedClients.get(1).getClientId()); //Mocking dao to throw this
		assertThrows(FIPSException.class, () -> {
			service.verifyClient(invalidNewEmail,validatedClients.get(1).getClientId());
			Mockito.verify(mockDao).verifyClient(invalidNewEmail,validatedClients.get(1).getClientId()); 
		});
	}
	@Test
	void testForInvalidatationOfMismatchingClientCredentialsAtLogin() {
		String email = validatedClients.get(1).getEmail();
		String mismatchedClientId = validatedClients.get(0).getClientId(); //Dont match with email
		String errorMessage = "Logging in Client's validation credentials dont match";
		Mockito.doThrow(new FIPSException(errorMessage)).when(mockDao).verifyClient(email,mismatchedClientId); //Mocking dao to throw this
		Exception e = assertThrows(FIPSException.class, () -> {
			service.verifyClient(email,mismatchedClientId);
			Mockito.verify(mockDao).verifyClient(email,mismatchedClientId); 
		});
		assertEquals(e.getMessage(),errorMessage);
	}

	/*FIPS Retrieval of Live Prices*/
	@Test
	void testForSuccessfulRetrievalOfLivePrices() {
		//Mocking dao to return live price list
		Mockito.when(mockDao.getLivePrices()).thenReturn(priceList);
		List<Price> prices = service.getLivePrices();
		assertEquals(prices,priceList,"Live Prices must be retrieved");
	}
	
	@Test
	void testForRetrievalOfEmptyLivePricesThrowsException() {
		//Mocking dao to return live price list
		Mockito.when(mockDao.getLivePrices()).thenReturn(new ArrayList<>());
		List<Price> prices = service.getLivePrices();
		Mockito.verify(mockDao).getLivePrices(); 
		assertTrue(prices.size()==0);
	}
	
	@Test
	void testForRetrievalOfLivePricesThrowsExceptionWhenFIPSThrowsException() {
		//Mocking dao to throw Exception
		Mockito.doThrow(new FIPSException()).when(mockDao).getLivePrices();
		assertThrows(FIPSException.class, () -> {
			service.getLivePrices();
			Mockito.verify(mockDao.getLivePrices()); 
		});		
	}
	
	/*FIPS Execution of Trade given Order*/
	@Test
	void testForExecutionOfNullOrderThrowsException() {
		Exception e = assertThrows(FIPSException.class, () -> {
			service.createTrade(null);
		});
		assertEquals("Order is invalid, Cannot execute trade",e.getMessage());
	}
	
	@Test
	void testForSuccessfulExecutionOfTradeGivenValidOrder() {
		//Mocking dao to return live price list
		Mockito.when(mockDao.createTrade(order)).thenReturn(trade);
		Trade executedTrade = service.createTrade(order);
		assertEquals(executedTrade,trade,"Trade must have been executed");
	}
	
	@Test
	void testForExecutionOfNullTradeFromFIPSThrowsException() {
		//Mocking dao to return null trade
		Mockito.when(mockDao.createTrade(order)).thenReturn(null);
		Exception e = assertThrows(FIPSException.class, () -> {
			service.createTrade(order);
			Mockito.verify(mockDao.createTrade(order));; 
		});	
		assertEquals(e.getMessage(),"Order is invalid, Cannot execute trade");
	}
	
	@Test
	void testForExecutionOfTradeGivenExpiredOrderThrowsException() {
		//Mocking dao to throw Exception
		String errorMessage = "User session has expired, Cannot execute Trade";
		Mockito.doThrow(new FIPSException(errorMessage)).when(mockDao).createTrade(order);
		Exception e = assertThrows(FIPSException.class, () -> {
			service.createTrade(order);
			Mockito.verify(mockDao.createTrade(order));; 
		});	
		
		assertEquals(e.getMessage(),errorMessage);
	}
	
	@Test
	void testForExecutionOfTradeWithNonExistingInstrumentThrowsException() {
		Order invalidOrder =  new Order("non-existing-instrument", 10, new BigDecimal("100.00"), "B", "1654658069", "ORDER001", 123);
		//Mocking dao to throw Exception
		String errorMessage = "Order is invalid, Cannot execute trade";
		Mockito.doThrow(new FIPSException(errorMessage)).when(mockDao).createTrade(invalidOrder);
		Exception e = assertThrows(FIPSException.class, () -> {
			service.createTrade(invalidOrder);
			Mockito.verify(mockDao.createTrade(invalidOrder));; 
		});	
		assertEquals(e.getMessage(),errorMessage);
	}
	
}