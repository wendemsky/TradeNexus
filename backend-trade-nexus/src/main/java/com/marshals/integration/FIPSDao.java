package com.marshals.integration;

import java.util.List;

import com.marshals.business.FIPSValidatedClient;
import com.marshals.business.Order;
import com.marshals.business.Price;
import com.marshals.business.Trade;

public interface FIPSDao {

	//Related to client validation
	FIPSValidatedClient verifyClient(String email);  //For registration
	FIPSValidatedClient verifyClient(String email, String clientId); //For login
	
	//Related to trades
	List<Price> getLivePrices(); //For getting live prices
	Trade createTrade(Order order); //For executing trade
	
}
