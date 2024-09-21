package com.fidelity.integration;

import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.Trade;

public interface ClientTradeDao {
	
	//Client Portoflio
	ClientPortfolio getClientPortfolio(String clientId); 
	void addClientPortfolio(ClientPortfolio clientPortfolio); 
	void updateClientPortfolio(ClientPortfolio clientPortfolio); 
	
	//Trade history 
	void getClientTradeHistory(String clientId); //Change return type to TradeHistory once the model is created
	void addTrade(Trade trade);

}
