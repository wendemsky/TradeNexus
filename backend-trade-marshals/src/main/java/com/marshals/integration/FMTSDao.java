package com.marshals.integration;

import java.util.List;

import com.marshals.business.FMTSValidatedClient;
import com.marshals.business.Order;
import com.marshals.business.Price;
import com.marshals.business.Trade;

public interface FMTSDao {

	FMTSValidatedClient verifyClient(String email); //For login
	FMTSValidatedClient verifyClient(String email, String clientId); //For registration
	
	List<Price> getLivePrices(); //For getting live prices
	 Trade createTrade(Order order); 
	
}
